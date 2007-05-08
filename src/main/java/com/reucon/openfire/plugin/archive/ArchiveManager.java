package com.reucon.openfire.plugin.archive;

import org.jivesoftware.openfire.session.Session;
import org.xmpp.packet.Message;

/**
 *
 */
public interface ArchiveManager
{
    void archiveMessage(Session session, Message message);

    void setConversationTimeout(int conversationTimeout);
}
