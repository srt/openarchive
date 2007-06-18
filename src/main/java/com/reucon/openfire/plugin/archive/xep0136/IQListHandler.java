package com.reucon.openfire.plugin.archive.xep0136;

import org.jivesoftware.openfire.auth.UnauthorizedException;
import org.xmpp.packet.IQ;
import org.xmpp.packet.JID;
import org.dom4j.Element;
import org.dom4j.DocumentFactory;
import com.reucon.openfire.plugin.archive.model.Conversation;
import com.reucon.openfire.plugin.archive.model.Participant;
import com.reucon.openfire.plugin.archive.util.DateUtil;
import com.reucon.openfire.plugin.archive.XmppResultSet;

import java.util.List;
import java.util.Collection;
import java.util.ArrayList;

/**
 * Message Archiving List Handler.
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

        Element listElement = reply.setChildElement("list", NAMESPACE);
        List<Conversation> conversations = list(packet.getFrom(), listRequest);
        XmppResultSet resultSet = listRequest.getResultSet();

        if (resultSet == null)
        {
            for (Conversation conversation : conversations)
            {
                addChatElement(listElement, packet, conversation);
            }
        }
        else
        {
            int start = resultSet.getIndex() < 0 ? 0 : resultSet.getIndex();
            boolean skip = false;
            int num = 0;

            if (resultSet.getAfter() != null)
            {
                skip = true;
            }

            for (int i = start; i < conversations.size(); i++)
            {
                Conversation conversation = conversations.get(i);

                if (skip)
                {
                    if (resultSet.getAfter() != null && resultSet.getAfter().equals(conversation.getId()))
                    {
                        skip = false;
                    }
                    continue;
                }

                addChatElement(listElement, packet, conversation);
                num++;

                if (resultSet.getMax() > 0 && num >= resultSet.getMax())
                {
                    break;
                }
            }
        }

        return reply;
    }

    private List<Conversation> list(JID from, ListRequest request)
    {
        final String[] participants;

        if (request.getWith() == null)
        {
            participants = new String[1];
        }
        else
        {
            participants = new String[2];
            participants[1] = request.getWith();
        }
        participants[0] = from.toBareJID();

        return getPersistenceManager().findConversations(participants, request.getStart(), request.getEnd());
    }

    private Element addChatElement(Element listElement, IQ packet, Conversation conversation)
    {
        Element chatElement = listElement.addElement("chat");
        String with = null;
        
        chatElement.addAttribute("with", conversation.getWithJid());
        chatElement.addAttribute("start", DateUtil.formatDate(conversation.getStart()));

        return chatElement;
    }
}
