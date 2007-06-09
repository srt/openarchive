package com.reucon.openfire.plugin.archive;

import org.xmpp.packet.IQ;
import org.xmpp.packet.PacketError;
import org.xmpp.packet.Packet;
import org.xmpp.packet.JID;
import org.xmpp.component.Component;
import org.xmpp.component.ComponentManager;
import org.xmpp.component.ComponentException;
import org.dom4j.Element;
import org.jivesoftware.util.Log;

/**
 *
 */
public class ArchiveComponent implements Component
{
    private static final String COMPONENT_NAME = "archive";
    private static final String COMPONENT_DESCRIPTION = "Open Archive";
    private JID jid;
    private ComponentManager componentManager;

    /* Implementation of Component */
    public String getName()
    {
        return COMPONENT_NAME;
    }

    public String getDescription()
    {
        return COMPONENT_DESCRIPTION;
    }

    public void initialize(JID jid, ComponentManager componentManager) throws ComponentException
    {
        this.jid = jid;
        this.componentManager = componentManager;
    }

    public void start()
    {
    }

    public void shutdown()
    {
    }

    public void processPacket(Packet packet)
    {
        if (packet instanceof IQ)
        {
            processPacket((IQ) packet);
        }
    }

    public void processPacket(IQ iq)
    {

        Element element = iq.getChildElement();
        String namespace = element.getNamespaceURI();

        /*
        if (NAMESPACE.equals(namespace)) {
            String type = element.attributeValue("type");

            if (PhoneAction.Type.DIAL.name().equals(type)) {
                handleDial(iq);
            }
            else if (PhoneAction.Type.FORWARD.name().equals(type)) {
                handleForward(iq);
            }

        }
        */
        if ("http://jabber.org/protocol/disco#info".equals(namespace))
        {
            handleDisco(iq);
        }
        else
        {
            // We were given a packet we don't know how to handle, send an error back
            IQ reply = IQ.createResultIQ(iq);
            reply.setType(IQ.Type.error);
            PacketError error = new PacketError(PacketError.Condition.feature_not_implemented,
                    PacketError.Type.cancel,
                    "Unknown operation");
            reply.setError(error);
            sendPacket(reply);
        }

    }


    public void handleDisco(IQ iq)
    {

        if (iq.getType().equals(IQ.Type.error))
        {
            // Log.info("Received disco error - " + iq);
            return;
        }

        if (!(iq.getType() == IQ.Type.get || iq.getType() == IQ.Type.set))
        {
            // Log.debug("Not set or get - " + iq);
            return;
        }

        // if information was sent to the component itself
        if (jid.equals(iq.getTo()))
        {

            //try to see if there is a node on the query
            Element child = iq.getChildElement();
            String node = child.attributeValue("node");

            // category - directory (since searching is possible)
            // type - phone

            // features
            // var - http://jabber.org/protocol/disco#info
            // var - jabber:iq:version

            IQ reply = IQ.createResultIQ(iq);
            reply.setType(IQ.Type.result);
            reply.setChildElement(iq.getChildElement().createCopy());

            Element queryElement = reply.getChildElement();

            // Create and add the identity
            Element identity = queryElement.addElement("identity");
            identity.addAttribute("category", "directory");
            identity.addAttribute("type", COMPONENT_NAME);
            identity.addAttribute("name", COMPONENT_DESCRIPTION);

            // Create and add a the feature provided by the workgroup
            // Create and add a the disco#info feature
            Element feature = queryElement.addElement("feature");
            feature.addAttribute("var", "http://jabber.org/protocol/disco#info");

            if (node == null)
            {
                // Indicate that we can provide information about the software version being used
                feature = queryElement.addElement("feature");
                feature.addAttribute("var", "jabber:iq:version");
            }
            else
            {
                // This is a query against a specific user
                /*
                try {

                    PhoneUser user = phoneManager.getPhoneUserByUsername(node);

                    // if there is a user they have support
                    if (user != null) {

                        // var http://jivesoftware.com/xmlns/phone
                        feature = queryElement.addElement("feature");
                        feature.addAttribute("var", "http://jivesoftware.com/phone");

                    }
                }
                catch (Exception e) {
                    log.log(Level.SEVERE, e.getMessage(), e);
                }
                */
            }
            sendPacket(reply);
        }
        else
        {
            // todo implement
        }
    }

    public void sendPacket(Packet packet)
    {
        try
        {
            componentManager.sendPacket(this, packet);
        }
        catch (Exception e)
        {
            Log.error(e);
        }
    }
}
