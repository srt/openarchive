package com.reucon.openfire.plugin.archive.xep0136;

import org.jivesoftware.openfire.IQRouter;
import org.jivesoftware.openfire.XMPPServer;
import org.jivesoftware.openfire.auth.UnauthorizedException;
import org.jivesoftware.openfire.disco.IQDiscoInfoHandler;
import org.jivesoftware.openfire.disco.ServerFeaturesProvider;
import org.jivesoftware.openfire.handler.IQHandler;
import org.jivesoftware.util.Log;
import org.xmpp.packet.IQ;
import org.xmpp.packet.PacketError;

import java.util.*;

import com.reucon.openfire.plugin.archive.ArchivePlugin;

/**
 * Encapsulates support for <a href="http://www.xmpp.org/extensions/xep-0136.html">XEP-0136</a>.
 */
public class Xep0136Support
{
    final XMPPServer server;
    final Map<String, IQHandler> element2Handlers;
    final IQHandler iqDispatcher;
    final Collection<IQHandler> iqHandlers;

    public Xep0136Support(XMPPServer server)
    {
        this.server = server;
        this.element2Handlers = Collections.synchronizedMap(new HashMap<String, IQHandler>());
        this.iqDispatcher = new AbstractIQHandler("XEP-0136 IQ Dispatcher", null) {
            public IQ handleIQ(IQ packet) throws UnauthorizedException
            {
                if (! ArchivePlugin.getInstance().isEnabled())
                {
                    return error(packet, PacketError.Condition.feature_not_implemented);
                }

                final IQHandler iqHandler = element2Handlers.get(packet.getChildElement().getName());
                if (iqHandler != null)
                {
                    return iqHandler.handleIQ(packet);
                }
                else
                {
                    return error(packet, PacketError.Condition.feature_not_implemented);
                }
            }
        };
        
        iqHandlers = new ArrayList<IQHandler>();
        iqHandlers.add(new IQPrefHandler());
        iqHandlers.add(new IQListHandler());
        iqHandlers.add(new IQRetrieveHandler());
    }

    public void start()
    {
        for (IQHandler iqHandler : iqHandlers)
        {
            try
            {
                iqHandler.initialize(server);
                iqHandler.start();
            }
            catch (Exception e)
            {
                Log.error("Unable to initialize and start " + iqHandler.getClass());
                continue;
            }

            element2Handlers.put(iqHandler.getInfo().getName(), iqHandler);
            if (iqHandler instanceof ServerFeaturesProvider)
            {
                for (Iterator<String> i = ((ServerFeaturesProvider) iqHandler).getFeatures(); i.hasNext(); )
                {
                    server.getIQDiscoInfoHandler().addServerFeature(i.next());
                }
            }
        }
        server.getIQRouter().addHandler(iqDispatcher);
    }

    public void stop()
    {
        IQRouter iqRouter = server.getIQRouter();
        IQDiscoInfoHandler iqDiscoInfoHandler = server.getIQDiscoInfoHandler();

        for (IQHandler iqHandler : iqHandlers)
        {
            element2Handlers.remove(iqHandler.getInfo().getName());
            try
            {
                iqHandler.stop();
                iqHandler.destroy();
            }
            catch (Exception e)
            {
                Log.warn("Unable to stop and destroy " + iqHandler.getClass());
            }

            if (iqHandler instanceof ServerFeaturesProvider)
            {
                for (Iterator<String> i = ((ServerFeaturesProvider) iqHandler).getFeatures(); i.hasNext(); )
                {
                    if (iqDiscoInfoHandler != null)
                    {
                        iqDiscoInfoHandler.removeServerFeature(i.next());
                    }
                }
            }
        }
        if (iqRouter != null)
        {
            iqRouter.removeHandler(iqDispatcher);
        }
    }
}
