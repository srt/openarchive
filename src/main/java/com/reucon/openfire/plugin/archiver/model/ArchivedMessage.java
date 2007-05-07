package com.reucon.openfire.plugin.archiver.model;

import org.jivesoftware.database.JiveID;

import java.util.Date;

/**
 * An archived message.
 */
@JiveID(501)
public class ArchivedMessage
{
    private Long id;
    private final Date time;
    private String originalId;
    private final String from;
    private final String fromResource;
    private final String to;
    private final String toResource;
    private String peerIpAddress;
    private final String type;
    private String thread;
    private String subject;
    private String body;
    private Conversation conversation;

    public ArchivedMessage(Date time, String from, String fromResource, String to, String toResource, String type)
    {
        this.time = time;
        this.from = from;
        this.fromResource = fromResource;
        this.to = to;
        this.toResource = toResource;
        this.type = type;
    }

    public Long getId()
    {
        return id;
    }

    public void setId(Long id)
    {
        this.id = id;
    }

    public Date getTime()
    {
        return time;
    }

    public String getOriginalId()
    {
        return originalId;
    }

    public void setOriginalId(String originalId)
    {
        this.originalId = originalId;
    }

    public String getFrom()
    {
        return from;
    }

    public String getFromResource()
    {
        return fromResource;
    }

    public String getTo()
    {
        return to;
    }

    public String getToResource()
    {
        return toResource;
    }

    public String getPeerIpAddress()
    {
        return peerIpAddress;
    }

    public void setPeerIpAddress(String peerIpAddress)
    {
        this.peerIpAddress = peerIpAddress;
    }

    public String getType()
    {
        return type;
    }

    public String getThread()
    {
        return thread;
    }

    public void setThread(String thread)
    {
        this.thread = thread;
    }

    public String getSubject()
    {
        return subject;
    }

    public void setSubject(String subject)
    {
        this.subject = subject;
    }

    public String getBody()
    {
        return body;
    }

    public void setBody(String body)
    {
        this.body = body;
    }

    public Conversation getConversation()
    {
        return conversation;
    }

    public void setConversation(Conversation conversation)
    {
        this.conversation = conversation;
    }

    /**
     * Checks if this message contains payload that should be archived.
     *
     * @return <code>true</code> if this message is empty, <code>false</code> otherwise.
     */
    public boolean isEmpty()
    {
        return subject == null && body == null;
    }

    public String toString()
    {
        StringBuilder sb = new StringBuilder();

        sb.append("ArchivedMessage[id=").append(id).append(",");
        sb.append("time=").append(time).append(",");
        sb.append("from='").append(from).append("',");
        sb.append("to='").append(to).append("']");
        
        return sb.toString();
    }
}
