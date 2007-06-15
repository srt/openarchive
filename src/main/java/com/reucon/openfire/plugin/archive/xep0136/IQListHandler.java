package com.reucon.openfire.plugin.archive.xep0136;

import org.jivesoftware.openfire.auth.UnauthorizedException;
import org.xmpp.packet.IQ;
import org.xmpp.packet.JID;
import com.reucon.openfire.plugin.archive.model.Conversation;
import com.reucon.openfire.plugin.archive.model.Participant;

import java.util.List;
import java.util.Collection;
import java.util.ArrayList;

/**
 * Message Archiving Preferences Handler.
 */
public class IQListHandler extends AbstractIQHandler
{
    public IQListHandler()
    {
        super("Message Archiving List Handler", "list");
    }

    public IQ handleIQ(IQ packet) throws UnauthorizedException
    {
        IQ reply = IQ.createResultIQ(packet);
        ListRequest listRequest = new ListRequest(packet.getChildElement());
        JID from = packet.getFrom();

        

        return reply;
    }

    private List<Conversation> list(JID from, ListRequest request)
    {
        final List<String> participants = new ArrayList<String>();

        participants.add(from.toBareJID());
        if (request.getWith() != null)
        {
            participants.add(request.getWith());
        }

        return getPersistenceManager().findConversations((String[]) participants.toArray(), request.getStart(), request.getEnd());
    }
}
