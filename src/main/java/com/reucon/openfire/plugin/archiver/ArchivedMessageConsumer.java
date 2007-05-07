package com.reucon.openfire.plugin.archiver;

import com.reucon.openfire.plugin.archiver.model.ArchivedMessage;

/**
 * Consumes an ArchivedMessage.
 */
public interface ArchivedMessageConsumer
{
    boolean consume(ArchivedMessage message);
}
