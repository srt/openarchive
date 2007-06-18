package com.reucon.openfire.plugin.archive.impl;

import com.reucon.openfire.plugin.archive.IndexManager;
import com.reucon.openfire.plugin.archive.PersistenceManager;
import com.reucon.openfire.plugin.archive.ArchivedMessageConsumer;
import com.reucon.openfire.plugin.archive.model.ArchivedMessage;
import com.reucon.openfire.plugin.archive.model.Conversation;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.DateTools;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.search.*;
import org.apache.lucene.store.FSDirectory;
import org.jivesoftware.openfire.XMPPServer;
import org.jivesoftware.openfire.roster.Roster;
import org.jivesoftware.openfire.roster.RosterItem;
import org.jivesoftware.openfire.roster.RosterManager;
import org.jivesoftware.openfire.user.UserManager;
import org.jivesoftware.openfire.user.UserNotFoundException;
import org.jivesoftware.util.Log;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Implementation of IndexManager using Lucene.
 */
public class LuceneIndexManager implements IndexManager, Runnable
{
    private static final String FIELD_DOCTYPE = "doctype";
    private static final String DOCTYPE_MESSAGE = "message";
    private static final String DOCTYPE_ROSTER_ITEM = "rosterItem";
    private static final String FIELD_MESSAGE_ID = "messageId";
    private static final String FIELD_CONVERSATION_ID = "conversationId";
    private static final String FIELD_TIME = "time";
    private static final String FIELD_OWNER_JID = "ownerJid";
    private static final String FIELD_WITH_JID = "withJid";
    private static final String FIELD_PARTICIPANT = "participant";
    private static final String FIELD_TYPE = "type";
    private static final String FIELD_SUBJECT = "subject";
    private static final String FIELD_BODY = "body";
    private static final String FIELD_NICK = "nick";
    private static final String FIELD_JID = "jid";
    private static final long INDEX_INTERVAL = 10000l; // 10 seconds
    private final File indexDir;
    private final PersistenceManager persistenceManager;
    private final Analyzer analyzer;
    private final BlockingQueue<Object> queue;
    private final Thread indexThread;
    private IndexSearcher indexSearcher;
    private boolean paused;
    private boolean die;

    public LuceneIndexManager(PersistenceManager persistenceManager, String indexDir) throws IOException
    {
        this.indexDir = new File(indexDir);
        this.persistenceManager = persistenceManager;
        this.indexSearcher = createIndexSearcher();
        System.out.println("Index opened.");

        this.analyzer = new StandardAnalyzer();
        this.queue = new LinkedBlockingQueue<Object>();
        this.indexThread = new Thread(this);
        this.indexThread.start();
        System.out.println("Index thread started.");
    }

    private void closeIndex() throws IOException
    {
        indexSearcher.close();
    }

    private IndexWriter createIndexWriter() throws IOException
    {
        if (!indexDir.exists())
        {
            indexDir.mkdirs();
            return new IndexWriter(FSDirectory.getDirectory(indexDir, false), analyzer, true);
        }
        else
        {
            return new IndexWriter(FSDirectory.getDirectory(indexDir, false), analyzer, false);
        }
    }

    private IndexSearcher createIndexSearcher() throws IOException
    {
        if (!indexDir.exists())
        {
            createIndexWriter().close();
        }
        return new IndexSearcher(FSDirectory.getDirectory(indexDir, false));
    }

    private void reopenIndexSearcher()
    {
        try
        {
            indexSearcher.close();
            indexSearcher = createIndexSearcher();
        }
        catch (IOException e)
        {
            Log.error("Unable to reopen index searcher.", e);
        }
    }

    public boolean indexObject(Object object)
    {
        if (die)
        {
            return false;
        }

        return queue.add(object);
    }

    public void run()
    {
        Collection<Object> objects;

        objects = new ArrayList<Object>(200);
        while (!die)
        {
            if (paused)
            {
                try
                {
                    Thread.sleep(1000);
                }
                catch (InterruptedException e)
                {
                    // wake up
                }
                continue;
            }

            try
            {
                Thread.sleep(INDEX_INTERVAL);
            }
            catch (InterruptedException e)
            {
                continue;
            }

            queue.drainTo(objects);
            if (objects.isEmpty())
            {
                continue;
            }


            IndexWriter indexWriter;
            try
            {
                indexWriter = createIndexWriter();
            }
            catch (IOException e)
            {
                Log.error("Unable to create index writer.", e);
                queue.addAll(objects);
                continue;
            }

            for (Object object : objects)
            {
                doIndex(indexWriter, object);
            }
            objects.clear();

            try
            {
                indexWriter.close();
                reopenIndexSearcher();
            }
            catch (IOException e)
            {
                Log.error("Unable to close index writer and reopen index searcher.", e);
            }

        }
        System.out.println("Index thread stopped.");
    }

    private boolean doIndex(IndexWriter indexWriter, Object object)
    {
        if (object instanceof ArchivedMessage)
        {
            return doIndexMessage(indexWriter, (ArchivedMessage) object);
        }
        if (object instanceof RosterItem)
        {
            return doIndexRosterItem(indexWriter, (RosterItem) object);
        }

        return false;
    }

    private boolean doIndexMessage(IndexWriter indexWriter, ArchivedMessage message)
    {
        try
        {
            indexWriter.addDocument(createDocument(message));
            System.out.println("Indexed " + message);
        }
        catch (IOException e)
        {
            Log.error("Unable to add message to lucene index.", e);
            return false;
        }

        return true;
    }

    public boolean doIndexRosterItem(IndexWriter indexWriter, RosterItem rosterItem)
    {
        try
        {
            indexWriter.addDocument(createDocument(rosterItem));
        }
        catch (IOException e)
        {
            Log.error("Unable to add rosterItem to lucene index.", e);
            return false;
        }

        return true;
    }

    public int rebuildIndex()
    {
        final IndexWriter indexWriter;
        int numMessages;

        if (this.paused)
        {
            return -1;
        }

        this.paused = true;
        System.out.println("Rebuilding index started");
        // allow current write operations to finish
        try
        {
            Thread.sleep(2000);
        }
        catch (InterruptedException e)
        {
            // swallow
        }

        try
        {
            deleteDirectory(indexDir);
            indexWriter = createIndexWriter();
        }
        catch (IOException e)
        {
            Log.error("Unable to clean lucene index: Indexing has been stopped.", e);
            this.die = true;
            return -1;
        }

        numMessages = rebuildMessageIndex(indexWriter);
        rebuildRosterIndex(indexWriter);

        try
        {
            indexWriter.close();
        }
        catch (IOException e)
        {
            Log.error("Unable to close index writer.", e);
        }

        reopenIndexSearcher();

        System.out.println("Rebuilding index finished");
        this.paused = false;
        return numMessages;
    }

    private int rebuildMessageIndex(final IndexWriter indexWriter)
    {
        return persistenceManager.selectAllMessages(new ArchivedMessageConsumer()
        {
            public boolean consume(ArchivedMessage message)
            {
                return doIndexMessage(indexWriter, message);
            }
        });
    }

    private int rebuildRosterIndex(final IndexWriter indexWriter)
    {
        int numRosterItems;
        UserManager userManager;
        RosterManager rosterManager;


        numRosterItems = 0;
        userManager = XMPPServer.getInstance().getUserManager();
        rosterManager = XMPPServer.getInstance().getRosterManager();

        for (String username : userManager.getUsernames())
        {
            Roster roster;

            try
            {
                roster = rosterManager.getRoster(username);
            }
            catch (UserNotFoundException e)
            {
                // won't happen
                continue;
            }

            for (RosterItem rosterItem : roster.getRosterItems())
            {
                doIndexRosterItem(indexWriter, rosterItem);
            }
        }

        return numRosterItems;
    }

    public Collection<String> searchParticipant(String token)
    {
        Collection<String> result;

        result = new TreeSet<String>();

        try
        {
            Query query = new QueryParser(FIELD_JID, analyzer).parse(token + "*");
            Hits hits = indexSearcher.search(query);
            System.out.println("Query: " + query.toString(FIELD_JID) + " Hits: " + hits.length());
            for (int i = 0; i < hits.length(); i++)
            {
                Document document = hits.doc(i);
                if (document.getField(FIELD_JID) != null)
                {
                    result.add(document.getField(FIELD_JID).stringValue());
                }
            }
        }
        catch (Exception e)
        {
            System.err.println("Unable to search: " + e);
        }

        return result;
    }

    public Collection<Conversation> findConversations(String[] participants, Date startDate, Date endDate, String keywords)
    {
        Collection<Long> conversationIds;

        conversationIds = new HashSet<Long>();

        try
        {
            BooleanQuery query = new BooleanQuery();
            query.add(new TermQuery(new Term(FIELD_DOCTYPE, DOCTYPE_MESSAGE)), BooleanClause.Occur.MUST);
            for (String participant : participants)
            {
                TermQuery termQuery;

                if (participant == null || participant.length() == 0)
                {
                    continue;
                }
                termQuery = new TermQuery(new Term(FIELD_PARTICIPANT, participant));
                query.add(termQuery, BooleanClause.Occur.MUST);
            }
            if (startDate != null || endDate != null)
            {
                RangeQuery rangeQuery;
                Term lowerTerm;
                Term upperTerm;

                lowerTerm = new Term(FIELD_TIME, dateToString(startDate == null ? new Date(0) : startDate));
                upperTerm = new Term(FIELD_TIME, dateToString(endDate == null ? new Date() : endDate));
                rangeQuery = new RangeQuery(lowerTerm, upperTerm, true);
                query.add(rangeQuery, BooleanClause.Occur.MUST);
            }
            if (keywords != null && keywords.length() != 0)
            {
                query.add(new QueryParser(FIELD_BODY, analyzer).parse(keywords), BooleanClause.Occur.MUST);
            }

            Hits hits = indexSearcher.search(query);
            System.out.println("Query: " + query.toString(FIELD_JID) + " Hits: " + hits.length());
            for (int i = 0; i < hits.length(); i++)
            {
                Document document = hits.doc(i);
                String conversationIdString = document.get(FIELD_CONVERSATION_ID);
                if (conversationIdString != null)
                {
                    conversationIds.add(Long.valueOf(conversationIdString));
                }
            }
        }
        catch (Exception e)
        {
            System.err.println("Unable to search: " + e);
        }

        return persistenceManager.getConversations(conversationIds);
    }

    /**
     * Recursivly deletes the given directory.
     *
     * @param path the directory to delete.
     * @return <code>true</code> if the directory was successfully deleted, <code>false</code> otherwise.
     */
    private boolean deleteDirectory(File path)
    {
        if (path.exists())
        {
            File[] files = path.listFiles();
            for (File file : files)
            {
                if (file.isDirectory())
                {
                    deleteDirectory(file);
                }
                else
                {
                    file.delete();
                }
            }
        }
        return (path.delete());
    }

    public void destroy()
    {
        die = true;
        indexThread.interrupt();

        try
        {
            closeIndex();
            System.out.println("Index closed.");
        }
        catch (IOException e)
        {
            Log.error("Unable to close lucene index.", e);
        }
    }

    private Document createDocument(ArchivedMessage message)
    {
        final Document doc;
        final Conversation conversation = message.getConversation();

        if (message.getId() == null)
        {
            throw new IllegalArgumentException("Unable to create lucene document from unsaved message (id is null)");
        }

        doc = new Document();
        doc.add(new Field(FIELD_DOCTYPE, DOCTYPE_MESSAGE, Field.Store.YES, Field.Index.UN_TOKENIZED));
        doc.add(new Field(FIELD_MESSAGE_ID, message.getId().toString(), Field.Store.YES, Field.Index.NO));
        if (message.getConversation() != null)
        {
            doc.add(new Field(FIELD_CONVERSATION_ID, message.getConversation().getId().toString(), Field.Store.YES, Field.Index.NO));
        }
        doc.add(new Field(FIELD_TIME, dateToString(message.getTime()), Field.Store.YES, Field.Index.UN_TOKENIZED));
        doc.add(new Field(FIELD_OWNER_JID, conversation.getOwnerJid(), Field.Store.YES, Field.Index.UN_TOKENIZED));
        doc.add(new Field(FIELD_WITH_JID, conversation.getWithJid(), Field.Store.YES, Field.Index.UN_TOKENIZED));
        doc.add(new Field(FIELD_PARTICIPANT, message.getFrom(), Field.Store.YES, Field.Index.UN_TOKENIZED));
        doc.add(new Field(FIELD_PARTICIPANT, message.getTo(), Field.Store.YES, Field.Index.UN_TOKENIZED));
        doc.add(new Field(FIELD_TYPE, message.getType(), Field.Store.YES, Field.Index.UN_TOKENIZED));
        if (message.getSubject() != null)
        {
            doc.add(new Field(FIELD_SUBJECT, message.getSubject(), Field.Store.YES, Field.Index.TOKENIZED));
        }
        if (message.getBody() != null)
        {
            doc.add(new Field(FIELD_BODY, message.getBody(), Field.Store.YES, Field.Index.TOKENIZED));
        }

        return doc;
    }

    private Document createDocument(RosterItem rosterItem)
    {
        Document doc;

        doc = new Document();
        doc.add(new Field(FIELD_DOCTYPE, DOCTYPE_ROSTER_ITEM, Field.Store.YES, Field.Index.UN_TOKENIZED));
        if (rosterItem.getNickname() != null)
        {
            doc.add(new Field(FIELD_NICK, rosterItem.getNickname(), Field.Store.YES, Field.Index.TOKENIZED));
        }
        if (rosterItem.getJid() != null)
        {
            doc.add(new Field(FIELD_JID, rosterItem.getJid().toBareJID(), Field.Store.YES, Field.Index.TOKENIZED));
        }

        System.out.println("Added roster item nick=" + rosterItem.getNickname() + ", jid=" + rosterItem.getJid());
        return doc;
    }
    
    private String dateToString(Date date)
    {
        return DateTools.dateToString(date, DateTools.Resolution.DAY);
    }
}
