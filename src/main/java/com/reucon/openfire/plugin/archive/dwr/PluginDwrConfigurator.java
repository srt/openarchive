package com.reucon.openfire.plugin.archive.dwr;

import org.directwebremoting.Container;
import org.directwebremoting.extend.Configurator;
import org.directwebremoting.impl.DwrXmlConfigurator;
import org.directwebremoting.util.Logger;

/**
 * Configures DWR from /WEB-INF/dwr-custom.xml which is loaded from the
 * classpath.
 */
public class PluginDwrConfigurator implements Configurator
{
    private static final Logger log = Logger.getLogger(PluginDwrConfigurator.class);
    private static final String CONFIG_LOCATION = "/WEB-INF/dwr-custom.xml";

    public void configure(Container container)
    {
        DwrXmlConfigurator local = new DwrXmlConfigurator();
        try
        {
            local.setClassResourceName(CONFIG_LOCATION);
            local.configure(container);
        }
        catch (Exception e)
        {
            log.error("Unable to configure DWR from " + CONFIG_LOCATION, e);
        }
    }
}
