package com.reucon.openfire.plugin.archive.impl;

import com.reucon.openfire.plugin.archive.ArchivedMessageConsumer;
import com.reucon.openfire.plugin.archive.PersistenceManager;
import com.reucon.openfire.plugin.archive.xep0059.XmppResultSet;
import com.reucon.openfire.plugin.archive.model.ArchivedMessage;
import com.reucon.openfire.plugin.archive.model.Conversation;
import com.reucon.openfire.plugin.archive.model.Participant;
import org.jivesoftware.database.DbConnectionManager;
import org.jivesoftware.database.SequenceManager;
import org.jivesoftware.util.Log;

import java.sql.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

/**
 * Manages database persistence.
 */
public class JdbcPersistenceManager implements PersistenceManager
{
    public static final int DEFAULT_MAX = 1000;

    public static final String CREATE_MESSAGE =
            "INSERT INTO archiveMessages (messageId,time,direction,type,subject,body,conversationId) " +
                    "VALUES (?,?,?,?,?,?,?)";

    public static final String SELECT_ALL_MESSAGES =
            "SELECT m.messageId,m.time,m.direction,m.type,m.subject,m.body," +
                    " c.conversationId,c.startTime,c.endTime," +
                    " c.ownerJid,c.ownerResource,c.withJid,c.withResource,c.subject,c.thread " +
                    "FROM archiveMessages AS m, archiveConversations AS c " +
                    "WHERE m.conversationId = c.conversationId " +
                    "ORDER BY c.conversationId";

    public static final String SELECT_MESSAGES_BY_CONVERSATION =
            "SELECT messageId,time,direction,type,subject,body " +
                    "FROM archiveMessages WHERE conversationId = ? ORDER BY time";

    public static final String CREATE_CONVERSATION =
            "INSERT INTO archiveConversations (conversationId,startTime,endTime," +
                    " ownerJid,ownerResource,withJid,withResource,subject,thread) " +
                    "VALUES (?,?,?,?,?,?,?,?,?)";

    public static final String UPDATE_CONVERSATION_END =
            "UPDATE archiveConversations SET endTime = ? WHERE conversationId = ?";

    public static final String SELECT_CONVERSATIONS =
            "SELECT c.conversationId,c.startTime,c.endTime,c.ownerJid,c.ownerResource,c.withJid,c.withResource," +
                    " c.subject,c.thread " +
                    "FROM archiveConversations AS c";
    public static final String COUNT_CONVERSATIONS =
            "SELECT count(*) FROM archiveConversations AS c";
    public static final String CONVERSATION_ID = "c.conversationId";
    public static final String CONVERSATION_START_TIME = "c.startTime";
    public static final String CONVERSATION_END_TIME = "c.endTime";
    public static final String CONVERSATION_OWNER_JID = "c.ownerJid";
    public static final String CONVERSATION_WITH_JID = "c.withJid";

    public static final String SELECT_ACTIVE_CONVERSATIONS =
            "SELECT c.conversationId,c.startTime,c.endTime,c.ownerJid,c.ownerResource,withJid,c.withResource," +
                    " c.subject,c.thread " +
                    "FROM archiveConversations AS c WHERE c.endTime > ?";

    public static final String ADD_PARTICIPANT =
            "INSERT INTO archiveParticipants (participantId,startTime,endTime,jid,conversationId) " +
                    "VALUES (?,?,?,?,?)";

    public static final String SELECT_PARTICIPANTS_BY_CONVERSATION =
            "SELECT participantId,startTime,endTime,jid FROM archiveParticipants WHERE conversationId =? ORDER BY startTime";

    public boolean createMessage(ArchivedMessage message)
    {
        long id;
        Connection con = null;
        PreparedStatement pstmt = null;
        try
        {
            con = DbConnectionManager.getConnection();
            pstmt = con.prepareStatement(CREATE_MESSAGE);

            id = SequenceManager.nextID(message);
            pstmt.setLong(1, id);
            pstmt.setLong(2, dateToMillis(message.getTime()));
            pstmt.setString(3, message.getDirection().name());
            pstmt.setString(4, message.getType());
            pstmt.setString(5, message.getSubject());
            pstmt.setString(6, message.getBody());
            pstmt.setLong(7, message.getConversation().getId());
            pstmt.executeUpdate();

            message.setId(id);
            return true;
        }
        catch (SQLException sqle)
        {
            Log.error("Error saving archived message", sqle);
            return false;
        }
        finally
        {
            DbConnectionManager.closeConnection(pstmt, con);
        }
    }

    public int processAllMessages(ArchivedMessageConsumer callback)
    {
        int numMessagesProcessed = 0;
        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        Conversation conversation = null;
        try
        {
            con = DbConnectionManager.getConnection();
            pstmt = con.prepareStatement(SELECT_ALL_MESSAGES);
            rs = pstmt.executeQuery();

            while (rs.next())
            {
                final long conversationId;
                ArchivedMessage message;

                message = extractMessage(rs);
                conversationId = rs.getLong(7);
                if (conversation == null || !conversation.getId().equals(conversationId))
                {
                    conversation = new Conversation(
                            millisToDate(rs.getLong(8)), millisToDate(rs.getLong(9)),
                            rs.getString(10), rs.getString(11), rs.getString(12), rs.getString(13),
                            rs.getString(14), rs.getString(15));
                    conversation.setId(conversationId);
                }
                message.setConversation(conversation);

                try
                {
                    if (callback.consume(message))
                    {
                        numMessagesProcessed++;
                    }
                }
                catch (Exception e)
                {
                    Log.error("Error processing selected messages", e);
                }
            }
        }
        catch (SQLException sqle)
        {
            Log.error("Error selecting all archived messages", sqle);
            return numMessagesProcessed;
        }
        finally
        {
            DbConnectionManager.closeConnection(rs, pstmt, con);
        }
        return numMessagesProcessed;
    }

    public boolean createConversation(Conversation conversation)
    {
        long id;
        Connection con = null;
        PreparedStatement pstmt = null;
        try
        {
            con = DbConnectionManager.getConnection();
            pstmt = con.prepareStatement(CREATE_CONVERSATION);

            id = SequenceManager.nextID(conversation);
            pstmt.setLong(1, id);
            pstmt.setLong(2, dateToMillis(conversation.getStart()));
            pstmt.setLong(3, dateToMillis(conversation.getEnd()));
            pstmt.setString(4, conversation.getOwnerJid());
            pstmt.setString(5, conversation.getOwnerResource());
            pstmt.setString(6, conversation.getWithJid());
            pstmt.setString(7, conversation.getWithResource());
            pstmt.setString(8, conversation.getSubject());
            pstmt.setString(9, conversation.getThread());
            pstmt.executeUpdate();

            conversation.setId(id);
            return true;
        }
        catch (SQLException sqle)
        {
            Log.error("Error saving conversation", sqle);
            return false;
        }
        finally
        {
            DbConnectionManager.closeConnection(pstmt, con);
        }
    }

    public boolean updateConversationEnd(Conversation conversation)
    {
        Connection con = null;
        PreparedStatement pstmt = null;
        try
        {
            con = DbConnectionManager.getConnection();
            pstmt = con.prepareStatement(UPDATE_CONVERSATION_END);
            pstmt.setLong(1, dateToMillis(conversation.getEnd()));
            pstmt.setLong(2, conversation.getId());
            pstmt.executeUpdate();

            return true;
        }
        catch (SQLException sqle)
        {
            Log.error("Error updating conversation end", sqle);
            return false;
        }
        finally
        {
            DbConnectionManager.closeConnection(pstmt, con);
        }
    }

    public boolean createParticipant(Participant participant, Long conversationId)
    {
        long id;
        Connection con = null;
        PreparedStatement pstmt = null;
        try
        {
            con = DbConnectionManager.getConnection();
            pstmt = con.prepareStatement(ADD_PARTICIPANT);

            id = SequenceManager.nextID(participant);
            pstmt.setLong(1, id);
            pstmt.setLong(2, dateToMillis(participant.getStart()));
            if (participant.getEnd() == null)
            {
                pstmt.setNull(3, Types.BIGINT);
            }
            else
            {
                pstmt.setLong(3, dateToMillis(participant.getEnd()));
            }
            pstmt.setString(4, participant.getJid());
            pstmt.setLong(5, conversationId);
            pstmt.executeUpdate();

            participant.setId(id);
            return true;
        }
        catch (SQLException sqle)
        {
            Log.error("Error creating participant", sqle);
            return false;
        }
        finally
        {
            DbConnectionManager.closeConnection(pstmt, con);
        }
    }

    public List<Conversation> findConversations(String[] participants, Date startDate, Date endDate)
    {
        final List<Conversation> conversations;
        final StringBuilder querySB;
        final StringBuilder whereSB;
        int parameterIndex;

        conversations = new ArrayList<Conversation>();

        querySB = new StringBuilder(SELECT_CONVERSATIONS);
        whereSB = new StringBuilder();

        for (int i = 0; i < participants.length; i++)
        {
            if (participants[i].length() == 0)
            {
                continue;
            }
            querySB.append(", archiveParticipants AS p").append(i);
            if (whereSB.length() != 0)
            {
                whereSB.append(" AND ");
            }
            whereSB.append("p").append(i).append(".conversationId = c.conversationId");
            whereSB.append(" AND p").append(i).append(".jid = ?");
        }
        if (startDate != null)
        {
            if (whereSB.length() != 0)
            {
                whereSB.append(" AND ");
            }
            whereSB.append(CONVERSATION_START_TIME).append(" >= ?");
        }
        if (endDate != null)
        {
            if (whereSB.length() != 0)
            {
                whereSB.append(" AND ");
            }
            whereSB.append(CONVERSATION_END_TIME).append(" <= ?");
        }
        querySB.append(" WHERE ").append(whereSB);
        querySB.append(" ORDER BY ").append(CONVERSATION_END_TIME);

        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try
        {
            con = DbConnectionManager.getConnection();
            pstmt = con.prepareStatement(querySB.toString());

            parameterIndex = 1;
            for (String participant : participants)
            {
                if (participant.length() == 0)
                {
                    continue;
                }

                pstmt.setString(parameterIndex++, participant);
            }
            if (startDate != null)
            {
                pstmt.setLong(parameterIndex++, dateToMillis(startDate));
            }
            if (endDate != null)
            {
                pstmt.setLong(parameterIndex++, dateToMillis(endDate));
            }

            rs = pstmt.executeQuery();
            while (rs.next())
            {
                conversations.add(extractConversation(rs));
            }
        }
        catch (SQLException sqle)
        {
            Log.error("Error selecting conversations", sqle);
        }
        finally
        {
            DbConnectionManager.closeConnection(rs, pstmt, con);
        }

        return conversations;
    }

    public List<Conversation> findConversations(Date startDate, Date endDate, String ownerJid, String withJid, XmppResultSet xmppResultSet)
    {
        final List<Conversation> conversations;
        final StringBuilder querySB;
        final StringBuilder whereSB;
        final StringBuilder limitSB;

        conversations = new ArrayList<Conversation>();

        querySB = new StringBuilder(SELECT_CONVERSATIONS);
        whereSB = new StringBuilder();
        limitSB = new StringBuilder();

        if (startDate != null)
        {
            appendWhere(whereSB, CONVERSATION_START_TIME, " >= ?");
        }
        if (endDate != null)
        {
            appendWhere(whereSB, CONVERSATION_END_TIME, " <= ?");
        }
        if (ownerJid != null)
        {
            appendWhere(whereSB, CONVERSATION_OWNER_JID, " = ?");
        }
        if (withJid != null)
        {
            appendWhere(whereSB, CONVERSATION_WITH_JID, " = ?");
        }

        if (xmppResultSet != null)
        {
            Integer firstIndex = null;
            int max = xmppResultSet.getMax() != null ? xmppResultSet.getMax() : DEFAULT_MAX;

            xmppResultSet.setCount(countConversations(startDate, endDate, ownerJid, withJid, whereSB.toString()));
            if (xmppResultSet.getIndex() != null)
            {
                firstIndex = xmppResultSet.getIndex();
            }
            else if (xmppResultSet.getAfter() != null)
            {
                firstIndex = countConversationsBefore(startDate, endDate, ownerJid, withJid, xmppResultSet.getAfter(), whereSB.toString());
                firstIndex += 1;
            }
            else if (xmppResultSet.getBefore() != null)
            {
                firstIndex = countConversationsBefore(startDate, endDate, ownerJid, withJid, xmppResultSet.getBefore(), whereSB.toString());
                firstIndex -= max;
                if (firstIndex < 0)
                {
                    firstIndex = 0;
                }
            }
            firstIndex = firstIndex != null ? firstIndex : 0;

            limitSB.append(" LIMIT ").append(max);
            limitSB.append(" OFFSET ").append(firstIndex);
            xmppResultSet.setFirstIndex(firstIndex);
        }

        if (whereSB.length() != 0)
        {
            querySB.append(" WHERE ").append(whereSB);
        }
        querySB.append(" ORDER BY ").append(CONVERSATION_ID);
        querySB.append(limitSB);

        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try
        {
            con = DbConnectionManager.getConnection();
            pstmt = con.prepareStatement(querySB.toString());
            bindConversationParameters(startDate, endDate, ownerJid, withJid, pstmt);
            rs = pstmt.executeQuery();
            while (rs.next())
            {
                conversations.add(extractConversation(rs));
            }
        }
        catch (SQLException sqle)
        {
            Log.error("Error selecting conversations", sqle);
        }
        finally
        {
            DbConnectionManager.closeConnection(rs, pstmt, con);
        }

        if (xmppResultSet != null && conversations.size() > 0)
        {
            xmppResultSet.setFirst(conversations.get(0).getId());
            xmppResultSet.setLast(conversations.get(conversations.size() - 1).getId());
        }
        return conversations;
    }

    private void appendWhere(StringBuilder sb, String... fragments)
    {
        if (sb.length() != 0)
        {
            sb.append(" AND ");
        }

        for (String fragment : fragments)
        {
            sb.append(fragment);
        }
    }

    private int countConversations(Date startDate, Date endDate, String ownerJid, String withJid, String whereClause)
    {
        StringBuilder querySB;

        querySB = new StringBuilder(COUNT_CONVERSATIONS);
        if (whereClause != null && whereClause.length() != 0)
        {
            querySB.append(" WHERE ").append(whereClause);
        }

        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try
        {
            con = DbConnectionManager.getConnection();
            pstmt = con.prepareStatement(querySB.toString());
            bindConversationParameters(startDate, endDate, ownerJid, withJid, pstmt);
            rs = pstmt.executeQuery();
            if (rs.next())
            {
                return rs.getInt(1);
            }
            else
            {
                return 0;
            }
        }
        catch (SQLException sqle)
        {
            Log.error("Error counting conversations", sqle);
            return 0;
        }
        finally
        {
            DbConnectionManager.closeConnection(rs, pstmt, con);
        }
    }

    private int countConversationsBefore(Date startDate, Date endDate, String ownerJid, String withJid, Long before, String whereClause)
    {
        StringBuilder querySB;

        querySB = new StringBuilder(COUNT_CONVERSATIONS);
        querySB.append(" WHERE ");
        if (whereClause != null && whereClause.length() != 0)
        {
            querySB.append(whereClause);
            querySB.append(" AND ");
        }
        querySB.append(CONVERSATION_ID).append(" < ?");

        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try
        {
            int parameterIndex;
            con = DbConnectionManager.getConnection();
            pstmt = con.prepareStatement(querySB.toString());
            parameterIndex = bindConversationParameters(startDate, endDate, ownerJid, withJid, pstmt);
            pstmt.setLong(parameterIndex, before);
            rs = pstmt.executeQuery();
            if (rs.next())
            {
                return rs.getInt(1);
            }
            else
            {
                return 0;
            }
        }
        catch (SQLException sqle)
        {
            Log.error("Error counting conversations", sqle);
            return 0;
        }
        finally
        {
            DbConnectionManager.closeConnection(rs, pstmt, con);
        }
    }

    private int bindConversationParameters(Date startDate, Date endDate, String ownerJid, String withJid, PreparedStatement pstmt) throws SQLException
    {
        int parameterIndex = 1;

        if (startDate != null)
        {
            pstmt.setLong(parameterIndex++, dateToMillis(startDate));
        }
        if (endDate != null)
        {
            pstmt.setLong(parameterIndex++, dateToMillis(endDate));
        }
        if (ownerJid != null)
        {
            pstmt.setString(parameterIndex++, ownerJid);
        }
        if (withJid != null)
        {
            pstmt.setString(parameterIndex++, withJid);
        }
        return parameterIndex;
    }

    public Collection<Conversation> getActiveConversations(int conversationTimeout)
    {
        final Collection<Conversation> conversations;
        final long now = System.currentTimeMillis();

        conversations = new ArrayList<Conversation>();

        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try
        {
            con = DbConnectionManager.getConnection();
            pstmt = con.prepareStatement(SELECT_ACTIVE_CONVERSATIONS);

            pstmt.setLong(1, now - conversationTimeout * 60L * 1000L);
            rs = pstmt.executeQuery();
            while (rs.next())
            {
                conversations.add(extractConversation(rs));
            }
        }
        catch (SQLException sqle)
        {
            Log.error("Error selecting conversations", sqle);
        }
        finally
        {
            DbConnectionManager.closeConnection(rs, pstmt, con);
        }

        return conversations;
    }

    public List<Conversation> getConversations(Collection<Long> conversationIds)
    {
        final List<Conversation> conversations;
        final StringBuilder querySB;

        conversations = new ArrayList<Conversation>();
        if (conversationIds.isEmpty())
        {
            return conversations;
        }

        querySB = new StringBuilder(SELECT_CONVERSATIONS);
        querySB.append(" WHERE ");
        querySB.append(CONVERSATION_ID);
        querySB.append(" IN ( ");
        for (int i = 0; i < conversationIds.size(); i++)
        {
            if (i == 0)
            {
                querySB.append("?");
            }
            else
            {
                querySB.append(",?");
            }
        }
        querySB.append(" )");
        querySB.append(" ORDER BY ").append(CONVERSATION_END_TIME);

        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try
        {
            con = DbConnectionManager.getConnection();
            pstmt = con.prepareStatement(querySB.toString());

            int i = 0;
            for (Long id : conversationIds)
            {
                pstmt.setLong(++i, id);
            }
            rs = pstmt.executeQuery();
            while (rs.next())
            {
                conversations.add(extractConversation(rs));
            }
        }
        catch (SQLException sqle)
        {
            Log.error("Error selecting conversations", sqle);
        }
        finally
        {
            DbConnectionManager.closeConnection(rs, pstmt, con);
        }

        return conversations;
    }

    public Conversation getConversation(String ownerJid, String withJid, Date start)
    {
        return getConversation(null, ownerJid, withJid, start);
    }

    public Conversation getConversation(Long conversationId)
    {
        return getConversation(conversationId, null, null, null);
    }

    private Conversation getConversation(Long conversationId, String ownerJid, String withJid, Date start)
    {
        Conversation conversation = null;
        StringBuilder querySB;

        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        querySB = new StringBuilder(SELECT_CONVERSATIONS);
        querySB.append(" WHERE ");
        if (conversationId != null)
        {
            querySB.append(CONVERSATION_ID).append(" = ? ");
        }
        else
        {
            querySB.append(CONVERSATION_OWNER_JID).append(" = ?");
            if (withJid != null)
            {
                querySB.append(" AND ");
                querySB.append(CONVERSATION_WITH_JID).append(" = ?");
            }
            if (start != null)
            {
                querySB.append(" AND ");
                querySB.append(CONVERSATION_START_TIME).append(" = ? ");
            }
        }

        try
        {
            con = DbConnectionManager.getConnection();
            pstmt = con.prepareStatement(querySB.toString());
            int i = 1;

            if (conversationId != null)
            {
                pstmt.setLong(1, conversationId);
            }
            else
            {
                pstmt.setString(i++, ownerJid);
                if (withJid != null)
                {
                    pstmt.setString(i++, withJid);
                }
                if (start != null)
                {
                    pstmt.setLong(i++, dateToMillis(start));
                }
            }
            rs = pstmt.executeQuery();
            if (rs.next())
            {
                conversation = extractConversation(rs);
            }
            else
            {
                return null;
            }

            rs.close();
            pstmt.close();

            pstmt = con.prepareStatement(SELECT_PARTICIPANTS_BY_CONVERSATION);
            pstmt.setLong(1, conversation.getId());

            rs = pstmt.executeQuery();
            while (rs.next())
            {
                conversation.addParticipant(extractParticipant(rs));
            }

            rs.close();
            pstmt.close();

            pstmt = con.prepareStatement(SELECT_MESSAGES_BY_CONVERSATION);
            pstmt.setLong(1, conversation.getId());

            rs = pstmt.executeQuery();
            while (rs.next())
            {
                ArchivedMessage message;

                message = extractMessage(rs);
                message.setConversation(conversation);
                conversation.addMessage(message);
            }
        }
        catch (SQLException sqle)
        {
            Log.error("Error selecting conversation", sqle);
        }
        finally
        {
            DbConnectionManager.closeConnection(rs, pstmt, con);
        }

        return conversation;
    }

    private Conversation extractConversation(ResultSet rs)
            throws SQLException
    {
        final Conversation conversation;
        final long id;

        id = rs.getLong(1);
        conversation = new Conversation(millisToDate(rs.getLong(2)), millisToDate(rs.getLong(3)),
                rs.getString(4), rs.getString(5), rs.getString(6), rs.getString(7),
                rs.getString(8), rs.getString(9));
        conversation.setId(id);
        return conversation;
    }

    private Participant extractParticipant(ResultSet rs)
            throws SQLException
    {
        final Participant participant;

        long end = rs.getLong(3);
        participant = new Participant(millisToDate(rs.getLong(2)), rs.getString(4));
        participant.setEnd(end == 0 ? null : millisToDate(end));
        return participant;
    }

    private ArchivedMessage extractMessage(ResultSet rs)
            throws SQLException
    {
        final ArchivedMessage message;
        final long id;

        id = rs.getLong(1);
        //TODO workaround for PostgreSQL, see http://www.igniterealtime.org/community/message/158668 patch from ctux.
        //Log.error("--" + rs.getLong(2) + "-" + rs.getString(3) + "-" + rs.getString(4));
        message = new ArchivedMessage(millisToDate(rs.getLong(2)), ArchivedMessage.Direction.valueOf(rs.getString(3).trim()),
                rs.getString(4));
        message.setId(id);
        message.setSubject(rs.getString(5));
        message.setBody(rs.getString(6));
        return message;
    }

    private Long dateToMillis(Date date)
    {
        return date == null ? null : date.getTime();
    }

    private Date millisToDate(Long millis)
    {
        return millis == null ? null : new Date(millis);
    }
}
