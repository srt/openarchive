package com.reucon.openfire.plugin.archive.xep0136;

import org.dom4j.Element;
import org.jivesoftware.openfire.auth.UnauthorizedException;
import org.jivesoftware.openfire.disco.ServerFeaturesProvider;
import org.xmpp.packet.IQ;
import org.xmpp.packet.JID;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Message Archiving Preferences Handler.
 */
public class IQPrefHandler extends AbstractIQHandler implements ServerFeaturesProvider
{
    private static final String NAMESPACE_PREF = "http://www.xmpp.org/extensions/xep-0136.html#ns-pref";

    public IQPrefHandler()
    {
        super("Message Archiving Preferences Handler", "pref");
    }

    @SuppressWarnings("unchecked")
    public IQ handleIQ(IQ packet) throws UnauthorizedException
    {
        IQ reply = IQ.createResultIQ(packet);
        Element prefRequest = packet.getChildElement();

        JID from = packet.getFrom();

        System.err.println("Received pref message from " + from);

        if (prefRequest.element("default") != null)
        {
            // User requests to set default modes
        }

        for (Element item : (List<Element>) prefRequest.elements("item"))
        {
            // User requests to set modes for a contact
        }

        for (Element method : (List<Element>) prefRequest.elements("method"))
        {
            // User requests to set archiving method preferences
        }

        return reply;
    }

    public Iterator<String> getFeatures()
    {
        ArrayList<String> features = new ArrayList<String>();
        features.add(NAMESPACE_PREF);
        return features.iterator();
    }
}
