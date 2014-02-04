/**
 *
 * Copyright (C) 20xx  Stefan Reuter + others  
 * Copyright (C) 2012  Taylor Raack <taylor@raack.info>
 * 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */


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
    private Long version;
    private final String ownerJid;
    private final String ownerResource;
    private final String withJid;
    private final String withResource;
    private String subject;
    private final String thread;
    private final List<Participant> participants;
    private final List<ArchivedMessage> messages;

    public Conversation(Date start, String ownerJid, String ownerResource, String withJid, String withResource,
                        String subject, String thread)
    {
        this(start, start, 0L, ownerJid, ownerResource, withJid, withResource, subject, thread);
    }

    public Conversation(Date start, Date end, Long version, String ownerJid, String ownerResource, String withJid, String withResource,
                        String subject, String thread)
    {
        this.start = start;
        this.end = end;
        this.version = version;
        this.ownerJid = ownerJid;
        this.ownerResource = ownerResource;
        this.withJid = withJid;
        this.withResource = withResource;
        this.subject = subject;
        this.thread = thread;
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
    
    public Long getVersion() {
		return version;
	}

	public void setVersion(Long version) {
		this.version = version;
	}

	public String getOwnerJid()
    {
        return ownerJid;
    }

    public String getOwnerResource()
    {
        return ownerResource;
    }

    public String getWithJid()
    {
        return withJid;
    }

    public String getWithResource()
    {
        return withResource;
    }

    public String getSubject()
    {
        return subject;
    }

    public void setSubject(String subject)
    {
        this.subject = subject;
    }

    public String getThread()
    {
        return thread;
    }

    public Collection<Participant> getParticipants()
    {
        return Collections.unmodifiableCollection(participants);
    }

    public void addParticipant(Participant participant)
    {
        synchronized (participants)
        {
            participants.add(participant);
        }
    }

    public List<ArchivedMessage> getMessages()
    {
        return Collections.unmodifiableList(messages);
    }

    public void addMessage(ArchivedMessage message)
    {
        synchronized (messages)
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
