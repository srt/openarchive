package com.reucon.openfire.plugin.archive;

import com.reucon.openfire.plugin.archive.model.ArchivedMessage;
import com.reucon.openfire.plugin.archive.model.Conversation;
import com.reucon.openfire.plugin.archive.model.Participant;

import java.util.Collection;
import java.util.Date;
import java.util.List;

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

    List<Conversation> findConversations(String[] participants, Date startDate, Date endDate);

    Collection<Conversation> getActiveConversations(int conversationTimeout);

    Collection<Conversation> getConversations(Collection<Long> conversationIds);

    Conversation getConversation(Long conversationId);
}
