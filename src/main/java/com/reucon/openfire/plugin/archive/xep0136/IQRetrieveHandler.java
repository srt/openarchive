package com.reucon.openfire.plugin.archive.xep0136;

import org.xmpp.packet.IQ;
import org.xmpp.packet.JID;
import org.xmpp.packet.PacketError;
import org.jivesoftware.openfire.auth.UnauthorizedException;
import org.dom4j.Element;
import com.reucon.openfire.plugin.archive.model.Conversation;
import com.reucon.openfire.plugin.archive.model.ArchivedMessage;
import com.reucon.openfire.plugin.archive.XmppResultSet;
import com.reucon.openfire.plugin.archive.util.XmppDateUtil;

import java.util.List;

/**
 * Message Archiving Retrieve Handler.
 */
public class IQRetrieveHandler extends AbstractIQHandler
{
    public IQRetrieveHandler()
    {
        super("Message Archiving Retrieve Handler", "retrieve");
    }

    public IQ handleIQ(IQ packet) throws UnauthorizedException
    {
        IQ reply = IQ.createResultIQ(packet);
        RetrieveRequest retrieveRequest = new RetrieveRequest(packet.getChildElement());

        Conversation conversation = retrieve(packet.getFrom(), retrieveRequest);
        if (conversation == null)
        {
            return error(packet, PacketError.Condition.item_not_found);
        }

        Element chatElement = reply.setChildElement("chat", NAMESPACE);
        chatElement.addAttribute("with", conversation.getWithJid());
        chatElement.addAttribute("start", XmppDateUtil.formatDate(conversation.getStart()));

        for (ArchivedMessage message : conversation.getMessages())
        {
            addMessageElement(chatElement, conversation, message);
        }

        return reply;
    }

    private Conversation retrieve(JID from, RetrieveRequest request)
    {
        return getPersistenceManager().getConversation(from.toBareJID(), request.getWith(), request.getStart());
    }

    private Element addMessageElement(Element listElement, Conversation conversation, ArchivedMessage message)
    {
        Element messageElement;

        if (conversation.getOwnerJid().equals(message.getFrom()))
        {
            messageElement = listElement.addElement("to");
        }
        else
        {
            messageElement = listElement.addElement("from");
        }
        long secs = message.getTime().getTime() - conversation.getStart().getTime();
        messageElement.addAttribute("secs", Long.toString(secs));
        messageElement.addElement("body").setText(message.getBody());

        return messageElement;
    }
}
