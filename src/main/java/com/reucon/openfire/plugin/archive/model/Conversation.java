package com.reucon.openfire.plugin.archive.model;

import org.jivesoftware.database.JiveID;

import java.util.*;

/**
 * A conversation between two or more participants.
 */
@JiveID(502)
public class Conversation
{
    private Long id;
    private final Date start;
    private Date end;
    private final String ownerJid;
    private final String withJid;
    private final List<Participant> participants;
    private final List<ArchivedMessage> messages;


    public Conversation(Long id)
    {
        this(null, null, null);
        this.id = id;
    }

    public Conversation(Date start, String ownerJid, String withJid)
    {
        this.start = start;
        this.end = start;
        this.ownerJid = ownerJid;
        this.withJid = withJid;
        participants = new ArrayList<Participant>();
        messages = new ArrayList<ArchivedMessage>();
    }

    public Long getId()
    {
        return id;
    }

    public void setId(Long id)
    {
        this.id = id;
    }

    public Date getStart()
    {
        return start;
    }

    public Date getEnd()
    {
        return end;
    }

    public void setEnd(Date end)
    {
        this.end = end;
    }

    public String getOwnerJid()
    {
        return ownerJid;
    }

    public String getWithJid()
    {
        return withJid;
    }

    public Collection<Participant> getParticipants()
    {
        return Collections.unmodifiableCollection(participants);
    }

    public void addParticipant(Participant participant)
    {
        synchronized(participants)
        {
            participants.add(participant);
        }
    }

    public Collection<ArchivedMessage> getMessages()
    {
        return Collections.unmodifiableCollection(messages);
    }

    public void addMessage(ArchivedMessage message)
    {
        synchronized(messages)
        {
            messages.add(message);
        }
    }

    public boolean isStale(int conversationTimeout)
    {
        Long now = System.currentTimeMillis();

        return end.getTime() + conversationTimeout * 60L * 1000L < now;
    }

    /**
     * Checks if this conversation has an active participant with the given JID.
     *
     * @param jid JID of the participant
     * @return <code>true</code> if this conversation has an active participant with the given JID,
     *         <code>false</code> otherwise.
     */
    public boolean hasParticipant(String jid)
    {
        synchronized (participants)
        {
            for (Participant p : participants)
            {
                if (p.getJid().equals(jid))
                {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Checks if this conversation is new and has not yet been persisted.
     *
     * @return <code>true</code> if this conversation is new and has not yet been persisted,
     *         <code>false</code> otherwise.
     */
    public boolean isNew()
    {
        return id == null;
    }
}
