package com.reucon.openfire.plugin.archiver;

import com.reucon.openfire.plugin.archiver.model.ArchivedMessage;
import com.reucon.openfire.plugin.archiver.model.Conversation;
import com.reucon.openfire.plugin.archiver.model.Participant;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Collection;

import org.jivesoftware.database.DbConnectionManager;
import org.jivesoftware.database.SequenceManager;
import org.jivesoftware.util.StringUtils;
import org.jivesoftware.util.Log;

/**
 * Manages database persistence.
 */
public interface PersistenceManager
{
    boolean saveMessage(ArchivedMessage message);

    /**
     * Selects all messages and passes each message to the given callback for processing.
     *
     * @param callback callback to process messages.
     * @return number of messages processed.
     */
    int selectAllMessages(ArchivedMessageConsumer callback);

    boolean createConversation(Conversation conversation);

    boolean updateConversationEnd(Conversation conversation);

    boolean createParticipant(Participant participant, Long conversationId);

    Collection<Conversation> getActiveConversations(int conversationTimeout);

    Collection<Conversation> getConversations(Collection<Long> conversationIds);

    Conversation getConversation(Long conversationId);
}
