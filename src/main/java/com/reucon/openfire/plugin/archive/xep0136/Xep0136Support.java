package com.reucon.openfire.plugin.archive.xep0136;

import org.jivesoftware.openfire.container.Module;
import org.jivesoftware.openfire.XMPPServer;
import org.jivesoftware.openfire.disco.ServerFeaturesProvider;
import org.jivesoftware.openfire.handler.IQHandler;
import org.jivesoftware.util.Log;
import org.jivesoftware.util.LocaleUtils;

import java.util.Map;
import java.util.Collection;
import java.util.ArrayList;
import java.util.Iterator;
import java.lang.reflect.Field;

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
        for (IQHandler iqHandler : iqHandlers)
        {
            server.getIQRouter().removeHandler(iqHandler);
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
                    server.getIQDiscoInfoHandler().removeServerFeature(i.next());
                }
            }
        }
    }
}
