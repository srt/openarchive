package com.reucon.openfire.plugin.archive.ajax;

import com.reucon.openfire.plugin.archive.model.ArchivedMessage;
import com.reucon.openfire.plugin.archive.model.Conversation;
import com.reucon.openfire.plugin.archive.model.Participant;
import com.reucon.openfire.plugin.archive.util.EscapeUtil;
import org.jivesoftware.openfire.XMPPServer;
import org.jivesoftware.openfire.user.UserManager;
import org.jivesoftware.openfire.user.UserNotFoundException;
import org.xmpp.packet.JID;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class FormattedConversation implements Comparable<FormattedConversation>
{
    private final Date sort;
    private long conversationId;
    private String ownerWith;
    private String participantsMultiLine;
    private String participantsSingleLine;
    private String date;
    private String shortDate;
    private String duration;
    private String body;
    private Map<String, String> jidResolutionCache = new HashMap<String, String>();

    public FormattedConversation(Conversation conversation)
    {
        StringBuilder participantsMultiLineSB = new StringBuilder();
        StringBuilder participantsSingleLineSB = new StringBuilder();
        DateFormat df = DateFormat.getDateInstance(DateFormat.MEDIUM);
        DateFormat tf = new SimpleDateFormat("HH:mm:ss");
        String startDateString = df.format(conversation.getStart());
        String endDateString = df.format(conversation.getEnd());
        long minutes;

        conversationId = conversation.getId();
        Iterator<Participant> i = conversation.getParticipants().iterator();
        while (i.hasNext())
        {
            Participant participant = i.next();
            String s = resolveJid(participant.getJid());

            participantsMultiLineSB.append(s);
            participantsSingleLineSB.append(s);
            if (i.hasNext())
            {
                participantsMultiLineSB.append("<br/>");
                participantsSingleLineSB.append(", ");
            }
        }
        participantsMultiLine = participantsMultiLineSB.toString();
        participantsSingleLine = participantsSingleLineSB.toString();
        ownerWith = resolveJid(conversation.getOwnerJid()) + "<br/>" + resolveJid(conversation.getWithJid());

        if (startDateString.equals(endDateString))
        {
            date = startDateString;
        }
        else
        {
            date = startDateString + " - " + endDateString;
        }
        shortDate = endDateString;
        minutes = (conversation.getEnd().getTime() - conversation.getStart().getTime()) / 1000 / 60;
        if (minutes == 0)
        {
            duration = "&lt; 1 min";
        }
        else if (minutes < 60)
        {
            duration = minutes + " min";
        }
        else if (minutes < 60 * 24)
        {
            duration = minutes / 60 + "h " + minutes % 60 + "min";
        }
        else
        {
            duration = ((minutes / 60 / 24) + 1) + " days";
        }

        if (conversation.getMessages() != null && !conversation.getMessages().isEmpty())
        {
            Map<String, Integer> participantIndices = new HashMap<String, Integer>();
            int nextIndex = 1;
            StringBuilder sb;

            sb = new StringBuilder();
            for (ArchivedMessage message : conversation.getMessages())
            {
                final String from;
                Integer participantIndex;

                from = message.getDirection() == ArchivedMessage.Direction.to ? conversation.getOwnerJid() : conversation.getWithJid();
                participantIndex = participantIndices.get(from);
                if (participantIndex == null)
                {
                    participantIndex = nextIndex++;
                    participantIndices.put(from, participantIndex);
                }
                sb.append("<span class='conversation-participant-").append(participantIndex).append("'>");
                sb.append("[").append(tf.format(message.getTime())).append("] ");
                sb.append(resolveJid(from));
                sb.append(": </span>");
                sb.append(EscapeUtil.escapeHtml(message.getBody()));
                sb.append("<br/>");
            }
            body = sb.toString();
        }

        sort = conversation.getEnd();
    }

    private String resolveJid(String bareJid)
    {
        UserManager userManager;
        JID jid = new JID(bareJid);
        String result;

        if (jidResolutionCache.containsKey(bareJid))
        {
            return jidResolutionCache.get(bareJid);
        }

        userManager = XMPPServer.getInstance().getUserManager();
        try
        {
            result = userManager.getUser(jid.getNode()).getName();
        }
        catch (UserNotFoundException e)
        {
            result = bareJid;
        }
        jidResolutionCache.put(bareJid, result);
        return result;
    }

    /**
     * Returns the id of the conversation.
     *
     * @return the id of the conversation.
     */
    public long getConversationId()
    {
        return conversationId;
    }

    /**
     * Returns the participants separated by &lt;br/&gt;.
     *
     * @return the participants separated by &lt;br/&gt;.
     */
    public String getParticipants()
    {
        return participantsMultiLine;
    }

    /**
     * Returns the participants separated by comma.
     *
     * @return the participants separated by comma.
     */
    public String getParticipantsSingleLine()
    {
        return participantsSingleLine;
    }

    /**
     * Returns owner and "with" jid/name separated by &lt;br/&gt;.
     *
     * @return owner and "with" jid/name separated by &lt;br/&gt;.
     */
    public String getOwnerWith()
    {
        return ownerWith;
    }

    /**
     * Returns start and end date as string or only one date if they are equal.
     *
     * @return start and end date as string or only one date if they are equal.
     */
    public String getDate()
    {
        return date;
    }

    /**
     * Returns the end date as a string.
     *
     * @return the end date as a string.
     */
    public String getShortDate()
    {
        return shortDate;
    }

    /**
     * Returns the duration as a human readable string.
     *
     * @return the duration as a human readable string.
     */
    public String getDuration()
    {
        return duration;
    }

    /**
     * Returns the HTML escaped transcrit of the conversation.
     *
     * @return the HTML escaped transcrit of the conversation.
     */
    public String getBody()
    {
        return body;
    }

    public int compareTo(FormattedConversation o)
    {
        return o.sort.compareTo(sort);
    }
}
