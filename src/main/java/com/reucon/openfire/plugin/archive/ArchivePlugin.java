package com.reucon.openfire.plugin.archive;

import com.reucon.openfire.plugin.archive.impl.ArchiveManagerImpl;
import com.reucon.openfire.plugin.archive.impl.JdbcPersistenceManager;
import com.reucon.openfire.plugin.archive.impl.LuceneIndexManager;
import com.reucon.openfire.plugin.archive.xep0136.Xep0136Support;
import org.jivesoftware.openfire.XMPPServer;
import org.jivesoftware.openfire.container.Plugin;
import org.jivesoftware.openfire.container.PluginManager;
import org.jivesoftware.openfire.interceptor.InterceptorManager;
import org.jivesoftware.openfire.interceptor.PacketInterceptor;
import org.jivesoftware.openfire.interceptor.PacketRejectedException;
import org.jivesoftware.openfire.muc.MultiUserChatManager;
import org.jivesoftware.openfire.session.Session;
import org.jivesoftware.util.JiveGlobals;
import org.jivesoftware.util.Log;
import org.jivesoftware.util.PropertyEventDispatcher;
import org.jivesoftware.util.PropertyEventListener;
import org.xmpp.packet.Message;
import org.xmpp.packet.Packet;
import org.xmpp.packet.Presence;

import java.io.File;
import java.io.IOException;
import java.util.Map;

/**
 * A sample plugin for Openfire.
 */
public class ArchivePlugin implements Plugin, PacketInterceptor
{
    private static final int DEFAULT_CONVERSATION_TIMEOUT = 30; // minutes
    private static final String DEFAULT_INDEX_DIR = "archive/index";
    private static ArchivePlugin instance;

    private String indexDir;
    private int conversationTimeout;
    private boolean enabled;
    private PropertyListener propertyListener;

    private XMPPServer server;
    private MultiUserChatManager mucServer;

    private ArchiveManager archiveManager;
    private PersistenceManager persistenceManager;
    private IndexManager indexManager;
    private Xep0136Support xep0136Support;

    public ArchivePlugin()
    {
        instance = this;
    }

    /* Implementation of Plugin */
    public void initializePlugin(PluginManager manager, File pluginDirectory)
    {
        /* Configuration */
        propertyListener = new PropertyListener();
        PropertyEventDispatcher.addListener(propertyListener);

        indexDir = JiveGlobals.getProperty(ArchiveProperties.INDEX_DIR, JiveGlobals.getHomeDirectory() + File.separator + DEFAULT_INDEX_DIR);
        conversationTimeout = JiveGlobals.getIntProperty(ArchiveProperties.CONVERSATION_TIMEOUT, DEFAULT_CONVERSATION_TIMEOUT);
        enabled = JiveGlobals.getBooleanProperty(ArchiveProperties.ENABLED, false);

        server = XMPPServer.getInstance();
        mucServer = server.getMultiUserChatManager();
        //mucServer.addListener();

        persistenceManager = new JdbcPersistenceManager();
        try
        {
            indexManager = new LuceneIndexManager(persistenceManager, indexDir);
        }
        catch (IOException e)
        {
            Log.error("Unable to create IndexManager.", e);
        }

        archiveManager = new ArchiveManagerImpl(persistenceManager, indexManager, conversationTimeout);

        InterceptorManager.getInstance().addInterceptor(this);

        xep0136Support = new Xep0136Support(server);
        xep0136Support.start();

        Log.info("Archive Plugin initialized");
    }

    public void destroyPlugin()
    {
        enabled = false;

        xep0136Support.stop();
        InterceptorManager.getInstance().removeInterceptor(this);

        if (indexManager != null)
        {
            indexManager.destroy();
        }

        PropertyEventDispatcher.removeListener(propertyListener);
        propertyListener = null;
        instance = null;
        Log.info("Archive Plugin destroyed");
    }

    /* Implementation of PacketInterceptor */
    public void interceptPacket(Packet packet, Session session, boolean incoming, boolean processed) throws PacketRejectedException
    {
        if (!isEnabled())
        {
            return;
        }

        if (!isValidTargetPacket(packet, incoming, processed))
        {
            return;
        }

        if (packet instanceof Message)
        {
            archiveManager.archiveMessage(session, (Message) packet, incoming);
        }
    }

    public static ArchivePlugin getInstance()
    {
        return instance;
    }

    public ArchiveManager getArchiveManager()
    {
        return archiveManager;
    }

    public IndexManager getIndexManager()
    {
        return indexManager;
    }

    public PersistenceManager getPersistenceManager()
    {
        return persistenceManager;
    }

    /* enabled property */
    public boolean isEnabled()
    {
        return this.enabled;
    }

    public void setEnabled(boolean enabled)
    {
        JiveGlobals.setProperty(ArchiveProperties.ENABLED, Boolean.toString(enabled));
    }

    private void doSetEnabled(boolean enabled)
    {
        this.enabled = enabled;
    }

    /* conversationTimeout property */
    public int getConversationTimeout()
    {
        return conversationTimeout;
    }

    public void setConversationTimeout(int conversationTimeout)
    {
        JiveGlobals.setProperty(ArchiveProperties.CONVERSATION_TIMEOUT, Integer.toString(conversationTimeout));
    }

    private void doSetConversationTimeout(int conversationTimeout)
    {
        this.conversationTimeout = conversationTimeout;
        archiveManager.setConversationTimeout(conversationTimeout);
    }

    private boolean isValidTargetPacket(Packet packet, boolean incoming, boolean processed)
    {
        if (! (packet instanceof Message) && ! (packet instanceof Presence))
        {
            return false;
        }
        if (! processed)
        {
            return false;
        }

        if (server.isLocal(packet.getFrom()) && incoming)
        {
            return true;
        }
        if (server.isLocal(packet.getTo()) && ! incoming)
        {
            return true;
        }
        return false;
    }

    /**
     * Listen for configuration changes.
     */
    private class PropertyListener implements PropertyEventListener
    {

        public void propertySet(String property, Map params)
        {
            Object value = params.get("value");

            if (value == null)
            {
                return;
            }

            if (ArchiveProperties.ENABLED.equals(property))
            {
                doSetEnabled(Boolean.valueOf(value.toString()));
            }
            else if (ArchiveProperties.CONVERSATION_TIMEOUT.equals(property))
            {
                doSetConversationTimeout(Integer.valueOf(value.toString()));
            }
        }

        public void propertyDeleted(String property, Map params)
        {
            if (ArchiveProperties.ENABLED.equals(property))
            {
                doSetEnabled(false);
            }
            else if (ArchiveProperties.CONVERSATION_TIMEOUT.equals(property))
            {
                doSetConversationTimeout(DEFAULT_CONVERSATION_TIMEOUT);
            }
        }

        public void xmlPropertySet(String property, Map params)
        {
        }

        public void xmlPropertyDeleted(String property, Map params)
        {
        }
    }
}
