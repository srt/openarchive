package com.reucon.openfire.plugin.archive;

import com.reucon.openfire.plugin.archive.model.ArchivedMessage;
import org.jivesoftware.openfire.session.Session;
import org.xmpp.packet.Message;

import java.util.Date;

/**
 * Factory to create model objects.
 */
public class ArchiveFactory
{
    private ArchiveFactory()
    {

    }
    
    public static ArchivedMessage createArchivedMessage(Session session, Message message, ArchivedMessage.Direction direction)
    {
        final ArchivedMessage archivedMessage;

        archivedMessage = new ArchivedMessage(new Date(), direction, message.getType().toString());
        archivedMessage.setSubject(message.getSubject());
        archivedMessage.setBody(message.getBody());

        return archivedMessage;
    }
}
