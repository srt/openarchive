package com.reucon.openfire.plugin.archive;

import com.reucon.openfire.plugin.archive.model.ArchivedMessage;
import org.xmpp.packet.Message;
import org.xmpp.packet.JID;
import org.jivesoftware.openfire.session.Session;
import org.jivesoftware.util.Log;

import java.net.UnknownHostException;
import java.util.Date;

/**
 * Factory to create model objects.
 */
public class ArchiverFactory
{
    private ArchiverFactory()
    {

    }
    
    public static ArchivedMessage createArchivedMessage(Session session, Message message)
    {
        final ArchivedMessage archivedMessage;

        final JID from = message.getFrom();
        final JID to = message.getTo();

        archivedMessage = new ArchivedMessage(new Date(), from.toBareJID(), from.getResource(),
                to.toBareJID(), to.getResource(), message.getType().toString());

        archivedMessage.setOriginalId(message.getID());
        try
        {
            if (session != null && session.getConnection() != null)
            {
                archivedMessage.setPeerIpAddress(session.getConnection().getInetAddress().getHostAddress());
            }
        }
        catch (UnknownHostException e)
        {
            Log.info("Unable to get peerIpAddress for archived message.", e);
        }
        archivedMessage.setThread(message.getThread());
        archivedMessage.setSubject(message.getSubject());
        archivedMessage.setBody(message.getBody());

        return archivedMessage;
    }
}
