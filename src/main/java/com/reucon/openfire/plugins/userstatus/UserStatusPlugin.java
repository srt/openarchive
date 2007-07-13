package com.reucon.openfire.plugins.userstatus;

import org.jivesoftware.database.DbConnectionManager;
import org.jivesoftware.database.SequenceManager;
import org.jivesoftware.openfire.SessionManager;
import org.jivesoftware.openfire.XMPPServer;
import org.jivesoftware.openfire.container.Plugin;
import org.jivesoftware.openfire.container.PluginManager;
import org.jivesoftware.openfire.event.SessionEventDispatcher;
import org.jivesoftware.openfire.event.SessionEventListener;
import org.jivesoftware.openfire.session.ClientSession;
import org.jivesoftware.openfire.session.Session;
import org.jivesoftware.openfire.user.PresenceEventDispatcher;
import org.jivesoftware.openfire.user.PresenceEventListener;
import org.jivesoftware.util.*;
import org.xmpp.packet.Presence;

import java.io.File;
import java.net.UnknownHostException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Date;
import java.util.Map;

/**
 * UserStatus plugin for Openfire.
 */
public class UserStatusPlugin implements Plugin, PropertyEventListener, SessionEventListener, PresenceEventListener
{
    private static final int SEQ_ID = 510;

    private static final String ADD_USER_STATUS =
            "INSERT INTO userStatus (username, resource, online, lastIpAddress, lastLoginDate) " +
                    "VALUES (?, ?, 1, ?, ?)";

    private static final String UPDATE_USER_STATUS =
            "UPDATE userStatus SET online = 1, lastIpAddress = ?, lastLoginDate = ? " +
                    "WHERE username = ? AND resource = ?";

    private static final String SET_PRESENCE =
            "UPDATE userStatus SET presence = ? WHERE username = ? AND resource = ?";

    private static final String SET_OFFLINE =
            "UPDATE userStatus SET online = 0, lastLogoffDate = ? WHERE username = ? AND resource = ?";

    private static final String SET_ALL_OFFLINE =
            "UPDATE userStatus SET online = 0";

    private static final String ADD_USER_STATUS_HISTORY =
            "INSERT INTO userStatusHistory (historyID, username, resource, lastIpAddress," +
                    "lastLoginDate, lastLogoffDate) VALUES (?, ?, ?, ?, ?, ?)";

    private static final String DELETE_OLD_USER_STATUS_HISTORY =
            "DELETE from userStatusHistory WHERE lastLogoffDate < ?";

    public static final String HISTORY_DAYS_PROPERTY = "user-status.historyDays";
    public static final int DEFAULT_HISTORY_DAYS = -1;

    /**
     * Number of days to keep history entries.<p>
     * 0 for now history entries, -1 for unlimited.
     */
    private int historyDays = DEFAULT_HISTORY_DAYS;

    public void initializePlugin(PluginManager manager, File pluginDirectory)
    {
        Connection con = null;
        PreparedStatement pstmt = null;

        historyDays = JiveGlobals.getIntProperty(HISTORY_DAYS_PROPERTY, DEFAULT_HISTORY_DAYS);
        PropertyEventDispatcher.addListener(this);

        try
        {
            con = DbConnectionManager.getConnection();
            pstmt = con.prepareStatement(SET_ALL_OFFLINE);
            pstmt.executeUpdate();
        }
        catch (SQLException e)
        {
            Log.error("Unable to clean up user status", e);
        }
        finally
        {
            DbConnectionManager.closeConnection(pstmt, con);
        }

        for (ClientSession session : SessionManager.getInstance().getSessions())
        {
            sessionCreated(session);
        }

        SessionEventDispatcher.addListener(this);
        PresenceEventDispatcher.addListener(this);
    }

    public void destroyPlugin()
    {
        PresenceEventDispatcher.removeListener(this);
        SessionEventDispatcher.removeListener(this);
    }

    public void sessionCreated(Session session)
    {
        Connection con = null;
        PreparedStatement pstmt = null;
        int rowsUpdated = 0;

        if (!XMPPServer.getInstance().getUserManager().isRegisteredUser(session.getAddress()))
        {
            return;
        }

        try
        {
            con = DbConnectionManager.getConnection();
            pstmt = con.prepareStatement(UPDATE_USER_STATUS);
            pstmt.setString(1, getHostAddress(session));
            pstmt.setString(2, StringUtils.dateToMillis(session.getCreationDate()));
            pstmt.setString(3, session.getAddress().getNode());
            pstmt.setString(4, session.getAddress().getResource());
            rowsUpdated = pstmt.executeUpdate();
        }
        catch (SQLException e)
        {
            Log.error("Unable to update user status for " + session.getAddress(), e);
        }
        finally
        {
            DbConnectionManager.closeConnection(pstmt, con);
        }

        if (rowsUpdated == 0)
        {
            try
            {
                con = DbConnectionManager.getConnection();
                pstmt = con.prepareStatement(ADD_USER_STATUS);
                pstmt.setString(1, session.getAddress().getNode());
                pstmt.setString(2, session.getAddress().getResource());
                pstmt.setString(3, getHostAddress(session));
                pstmt.setString(4, StringUtils.dateToMillis(session.getCreationDate()));
                pstmt.executeUpdate();
            }
            catch (SQLException e)
            {
                Log.error("Unable to insert user status for " + session.getAddress(), e);
            }
            finally
            {
                DbConnectionManager.closeConnection(pstmt, con);
            }
        }
    }

    public void sessionDestroyed(Session session)
    {
        Connection con = null;
        PreparedStatement pstmt = null;
        final Date logoffDate;

        if (!XMPPServer.getInstance().getUserManager().isRegisteredUser(session.getAddress()))
        {
            return;
        }

        logoffDate = new Date();
        try
        {
            con = DbConnectionManager.getConnection();
            pstmt = con.prepareStatement(SET_OFFLINE);
            pstmt.setString(1, StringUtils.dateToMillis(logoffDate));
            pstmt.setString(2, session.getAddress().getNode());
            pstmt.setString(3, session.getAddress().getResource());
            pstmt.executeUpdate();
        }
        catch (SQLException e)
        {
            Log.error("Unable to update user status for " + session.getAddress(), e);
        }
        finally
        {
            DbConnectionManager.closeConnection(pstmt, con);
        }

        // write history entry
        if (historyDays != 0)
        {
            try
            {
                con = DbConnectionManager.getConnection();
                pstmt = con.prepareStatement(ADD_USER_STATUS_HISTORY);
                pstmt.setLong(1, SequenceManager.nextID(SEQ_ID));
                pstmt.setString(2, session.getAddress().getNode());
                pstmt.setString(3, session.getAddress().getResource());
                pstmt.setString(4, getHostAddress(session));
                pstmt.setString(5, StringUtils.dateToMillis(session.getCreationDate()));
                pstmt.setString(6, StringUtils.dateToMillis(logoffDate));
                pstmt.executeUpdate();
            }
            catch (SQLException e)
            {
                Log.error("Unable to add user status history for " + session.getAddress(), e);
            }
            finally
            {
                DbConnectionManager.closeConnection(pstmt, con);
            }
        }

        deleteOldHistoryEntries();
    }

    public void anonymousSessionCreated(Session session)
    {
        // we are not interested in anonymous sessions
    }

    public void anonymousSessionDestroyed(Session session)
    {
        // we are not interested in anonymous sessions
    }

    public void availableSession(ClientSession session, Presence presence)
    {
        updatePresence(session, presence);
    }

    public void unavailableSession(ClientSession session, Presence presence)
    {
        updatePresence(session, presence);
    }

    public void presencePriorityChanged(ClientSession session, Presence presence)
    {
        // we are not interested in priority changes
    }

    public void presenceChanged(ClientSession session, Presence presence)
    {
        updatePresence(session, presence);
    }

    public void propertySet(String property, Map<String, Object> params)
    {
        if (HISTORY_DAYS_PROPERTY.equals(property))
        {
            final Object value = params.get("value");
            if (value != null)
            {
                try
                {
                    historyDays = Integer.valueOf(value.toString());
                }
                catch (NumberFormatException e)
                {
                    historyDays = DEFAULT_HISTORY_DAYS;
                }
                deleteOldHistoryEntries();
            }
        }
    }

    public void propertyDeleted(String property, Map<String, Object> params)
    {
        if (HISTORY_DAYS_PROPERTY.equals(property))
        {
            historyDays = DEFAULT_HISTORY_DAYS;
            deleteOldHistoryEntries();
        }
    }

    public void xmlPropertySet(String property, Map<String, Object> params)
    {
        // we don't use xml properties
    }

    public void xmlPropertyDeleted(String property, Map<String, Object> params)
    {
        // we don't use xml properties
    }

    private void updatePresence(ClientSession session, Presence presence)
    {
        Connection con = null;
        PreparedStatement pstmt = null;
        final String presenceText;

        if (!XMPPServer.getInstance().getUserManager().isRegisteredUser(session.getAddress()))
        {
            return;
        }

        if (Presence.Type.unavailable.equals(presence.getType()))
        {
            presenceText = presence.getType().toString();
        }
        else if (presence.getShow() != null)
        {
            presenceText = presence.getShow().toString();
        }
        else if (presence.isAvailable())
        {
            presenceText = "available";
        }
        else
        {
            return;
        }

        try
        {
            con = DbConnectionManager.getConnection();
            pstmt = con.prepareStatement(SET_PRESENCE);
            pstmt.setString(1, presenceText);
            pstmt.setString(2, session.getAddress().getNode());
            pstmt.setString(3, session.getAddress().getResource());
            pstmt.executeUpdate();
        }
        catch (SQLException e)
        {
            Log.error("Unable to update presence for " + session.getAddress(), e);
        }
        finally
        {
            DbConnectionManager.closeConnection(pstmt, con);
        }
    }

    private void deleteOldHistoryEntries()
    {
        Connection con = null;
        PreparedStatement pstmt = null;

        if (historyDays > 0)
        {
            final Date deleteBefore;

            deleteBefore = new Date(System.currentTimeMillis() - historyDays * 24L * 60L * 60L * 1000L);

            try
            {
                con = DbConnectionManager.getConnection();
                pstmt = con.prepareStatement(DELETE_OLD_USER_STATUS_HISTORY);
                pstmt.setString(1, StringUtils.dateToMillis(deleteBefore));
                pstmt.executeUpdate();
            }
            catch (SQLException e)
            {
                Log.error("Unable to delete old user status history", e);
            }
            finally
            {
                DbConnectionManager.closeConnection(pstmt, con);
            }
        }
    }

    private String getHostAddress(Session session)
    {
        try
        {
            return session.getConnection().getInetAddress().getHostAddress();
        }
        catch (UnknownHostException e)
        {
            return "";
        }
    }
}
