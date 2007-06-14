package com.reucon.openfire.plugin.archive.impl;

import com.reucon.openfire.plugin.archive.model.ArchivedMessage;
import com.reucon.openfire.plugin.archive.model.Conversation;
import com.reucon.openfire.plugin.archive.model.Participant;
import com.reucon.openfire.plugin.archive.PersistenceManager;
import com.reucon.openfire.plugin.archive.ArchivedMessageConsumer;

import java.sql.*;
import java.util.Date;
import java.util.Collection;
import java.util.ArrayList;

import org.jivesoftware.database.DbConnectionManager;
import org.jivesoftware.database.SequenceManager;
import org.jivesoftware.util.Log;

/**
 * Manages database persistence.
 */
public class JdbcPersistenceManager implements PersistenceManager
{
    public static final String CREATE_MESSAGE =
            "INSERT INTO archiveMessages (messageID,time,originalId,fromJid,fromResource," +
                    " toJid,toResource,peerIpAddress,type,thread,subject,body,conversationID) " +
                    "VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?)";

    public static final String SELECT_ALL_MESSAGES =
            "SELECT messageID,time,originalId,fromJid,fromResource," +
                    " toJid,toResource,peerIpAddress,type,thread,subject,body,conversationID " +
                    "FROM archiveMessages";

    public static final String SELECT_MESSAGES_BY_CONVERSATION =
            "SELECT messageID,time,originalId,fromJid,fromResource," +
                    " toJid,toResource,peerIpAddress,type,thread,subject,body,conversationID " +
                    "FROM archiveMessages WHERE conversationId = ? ORDER BY time";
    
    public static final String CREATE_CONVERSATION =
            "INSERT INTO archiveConversations (conversationID,startTime,endTime,roomJid) " +
                    "VALUES (?,?,?,?)";

    public static final String UPDATE_CONVERSATION_END =
            "UPDATE archiveConversations SET endTime = ? WHERE conversationID = ?";

    public static final String ADD_PARTICIPANT =
            "INSERT INTO archiveParticipants (participantID,startTime,endTime,jid,conversationID) " +
                    "VALUES (?,?,?,?,?)";

    public static final String SELECT_CONVERSATION =
            "SELECT conversationID,startTime,endTime,roomJid FROM archiveConversations WHERE conversationId = ?";

    public static final String SELECT_CONVERSATIONS =
            "SELECT c.conversationID,c.startTime,c.endTime,c.roomJid FROM archiveConversations AS c";
    public static final String CONVERSATION_ID = "c.conversationID";

    public static final String SELECT_ACTIVE_CONVERSATIONS =
            "SELECT conversationID,startTime,endTime,roomJid FROM archiveConversations WHERE endTime > ?";

    public static final String SELECT_PARTICIPANTS_BY_CONVERSATION =
            "SELECT participantID,startTime,endTime,jid FROM archiveParticipants WHERE conversationID =? ORDER BY startTime";

    public boolean saveMessage(ArchivedMessage message)
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
            pstmt.setString(3, message.getOriginalId());
            pstmt.setString(4, message.getFrom());
            pstmt.setString(5, message.getFromResource());
            pstmt.setString(6, message.getTo());
            pstmt.setString(7, message.getToResource());
            pstmt.setString(8, message.getPeerIpAddress());
            pstmt.setString(9, message.getType());
            pstmt.setString(10, message.getThread());
            pstmt.setString(11, message.getSubject());
            pstmt.setString(12, message.getBody());
            pstmt.setLong(13, message.getConversation().getId());
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

    public int selectAllMessages(ArchivedMessageConsumer callback)
    {
        long id;
        int numMessagesProcessed = 0;
        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try
        {
            con = DbConnectionManager.getConnection();
            pstmt = con.prepareStatement(SELECT_ALL_MESSAGES);
            rs = pstmt.executeQuery();

            while (rs.next())
            {
                ArchivedMessage message;

                message = extractMessage(rs);

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

    private ArchivedMessage extractMessage(ResultSet rs)
            throws SQLException
    {
        ArchivedMessage message;
        message = new ArchivedMessage(millisToDate(rs.getLong(2)),
                rs.getString(4), rs.getString(5), rs.getString(6), rs.getString(7), rs.getString(9));
        message.setId(rs.getLong(1));
        message.setOriginalId(rs.getString(3));
        message.setPeerIpAddress(rs.getString(8));
        message.setThread(rs.getString(10));
        message.setSubject(rs.getString(11));
        message.setBody(rs.getString(12));
        message.setConversation(new Conversation(rs.getLong(13)));
        return message;
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
            pstmt.setString(4, conversation.getRoomJid());
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

    public Collection<Conversation> findConversations(String[] participants, Date startDate, Date endDate)
    {
        final Collection<Conversation> conversations;
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
            whereSB.append("c.startTime >= ?");
        }
        if (endDate != null)
        {
            if (whereSB.length() != 0)
            {
                whereSB.append(" AND ");
            }
            whereSB.append("c.endTime <= ?");
        }
        querySB.append(" WHERE ").append(whereSB);
        System.out.println(querySB.toString());

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

                System.out.println(parameterIndex + ": " + participant);
                pstmt.setString(parameterIndex++, participant);
            }
            if (startDate != null)
            {
                System.out.println(parameterIndex + ": " + startDate);
                pstmt.setLong(parameterIndex++, dateToMillis(startDate));
            }
            if (endDate != null)
            {
                System.out.println(parameterIndex + ": " + endDate);
                pstmt.setLong(parameterIndex++, dateToMillis(endDate));
            }

            rs = pstmt.executeQuery();
            while (rs.next())
            {
                Conversation conversation;

                conversation = new Conversation(millisToDate(rs.getLong(2)), rs.getString(4));
                conversation.setId(rs.getLong(1));
                conversation.setEnd(millisToDate(rs.getLong(3)));
                conversations.add(conversation);
            }

            rs.close();
            pstmt.close();

            pstmt = con.prepareStatement(SELECT_PARTICIPANTS_BY_CONVERSATION);
            for (Conversation conversation : conversations)
            {
                pstmt.setLong(1, conversation.getId());
                rs = pstmt.executeQuery();
                while (rs.next())
                {
                    Participant participant;
                    participant = extractParticipant(rs);
                    conversation.addParticipant(participant);
                }
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
                Conversation conversation;

                conversation = new Conversation(millisToDate(rs.getLong(2)), rs.getString(4));
                conversation.setId(rs.getLong(1));
                conversation.setEnd(millisToDate(rs.getLong(3)));
                conversations.add(conversation);
            }

            rs.close();
            pstmt.close();

            pstmt = con.prepareStatement(SELECT_PARTICIPANTS_BY_CONVERSATION);
            for (Conversation conversation : conversations)
            {
                pstmt.setLong(1, conversation.getId());
                rs = pstmt.executeQuery();
                while (rs.next())
                {
                    Participant participant;
                    participant = extractParticipant(rs);
                    conversation.addParticipant(participant);
                }
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

    public Collection<Conversation> getConversations(Collection<Long> conversationIds)
    {
        final Collection<Conversation> conversations;
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
                Conversation conversation;

                conversation = new Conversation(millisToDate(rs.getLong(2)), rs.getString(4));
                conversation.setId(rs.getLong(1));
                conversation.setEnd(millisToDate(rs.getLong(3)));
                conversations.add(conversation);
            }

            rs.close();
            pstmt.close();

            pstmt = con.prepareStatement(SELECT_PARTICIPANTS_BY_CONVERSATION);
            for (Conversation conversation : conversations)
            {
                pstmt.setLong(1, conversation.getId());
                rs = pstmt.executeQuery();
                while (rs.next())
                {
                    Participant participant;
                    participant = extractParticipant(rs);
                    conversation.addParticipant(participant);
                }
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

    private Participant extractParticipant(ResultSet rs)
            throws SQLException
    {
        Participant participant;
        long end = rs.getLong(3);
        participant = new Participant(millisToDate(rs.getLong(2)), rs.getString(4));
        participant.setEnd(end == 0 ? null : millisToDate(end));
        return participant;
    }

    public Conversation getConversation(Long conversationId)
    {
        Conversation conversation = null;

        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try
        {
            con = DbConnectionManager.getConnection();
            pstmt = con.prepareStatement(SELECT_CONVERSATION);

            pstmt.setLong(1, conversationId);
            rs = pstmt.executeQuery();
            if (rs.next())
            {
                conversation = new Conversation(millisToDate(rs.getLong(2)), rs.getString(4));
                conversation.setId(rs.getLong(1));
                conversation.setEnd(millisToDate(rs.getLong(3)));
            }
            else
            {
                return null;
            }

            rs.close();
            pstmt.close();

            pstmt = con.prepareStatement(SELECT_PARTICIPANTS_BY_CONVERSATION);
            pstmt.setLong(1, conversationId);

            rs = pstmt.executeQuery();
            while (rs.next())
            {
                Participant participant;
                participant = extractParticipant(rs);
                conversation.addParticipant(participant);
            }

            rs.close();
            pstmt.close();

            pstmt = con.prepareStatement(SELECT_MESSAGES_BY_CONVERSATION);
            pstmt.setLong(1, conversationId);

            rs = pstmt.executeQuery();
            while (rs.next())
            {
                ArchivedMessage message;

                message = extractMessage(rs);
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

    private Long dateToMillis(Date date)
    {
        return date == null ? null : date.getTime();
    }

    private Date millisToDate(Long millis)
    {
        return millis == null ? null : new Date(millis);
    }
}
