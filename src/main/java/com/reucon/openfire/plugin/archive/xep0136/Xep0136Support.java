package com.reucon.openfire.plugin.archive.xep0136;

import org.jivesoftware.openfire.XMPPServer;
import org.jivesoftware.openfire.IQRouter;
import org.jivesoftware.openfire.disco.ServerFeaturesProvider;
import org.jivesoftware.openfire.disco.IQDiscoInfoHandler;
import org.jivesoftware.openfire.handler.IQHandler;
import org.jivesoftware.util.Log;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

/**
 * Encapsulates support for XEP-0136.
 */
public class Xep0136Support
{
    final XMPPServer server;
    final Collection<IQHandler> iqHandlers;

    public Xep0136Support(XMPPServer server)
    {
        this.server = server;
        this.iqHandlers = new ArrayList<IQHandler>();
        iqHandlers.add(new IQPrefHandler());
        iqHandlers.add(new IQListHandler());
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
            server.getIQRouter().addHandler(iqHandler);

            if (iqHandler instanceof ServerFeaturesProvider)
            {
                for (Iterator<String> i = ((ServerFeaturesProvider) iqHandler).getFeatures(); i.hasNext(); )
                {
                    server.getIQDiscoInfoHandler().addServerFeature(i.next());
                }
            }
        }
    }

    public void stop()
    {
        IQRouter iqRouter = server.getIQRouter();
        IQDiscoInfoHandler iqDiscoInfoHandler = server.getIQDiscoInfoHandler();

        for (IQHandler iqHandler : iqHandlers)
        {
            if (iqRouter != null)
            {
                iqRouter.removeHandler(iqHandler);
            }
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
    }
}
