package com.reucon.openfire.plugin.archive.xep0136;

import org.jivesoftware.openfire.auth.UnauthorizedException;
import org.xmpp.packet.IQ;
import org.xmpp.packet.JID;

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
}
