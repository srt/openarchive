package com.reucon.openfire.plugin.archive.xep0136;

import com.reucon.openfire.plugin.archive.ArchivePlugin;
import com.reucon.openfire.plugin.archive.PersistenceManager;
import org.jivesoftware.openfire.IQHandlerInfo;
import org.jivesoftware.openfire.handler.IQHandler;

/**
 * Abstract base class for XEP-0136 IQ Handlers.
 */
public abstract class AbstractIQHandler extends IQHandler
{
    private static final String NAMESPACE = "http://www.xmpp.org/extensions/xep-0136.html#ns";
    private final IQHandlerInfo info;

    protected AbstractIQHandler(String moduleName, String elementName)
    {
        super(moduleName);
        this.info = new IQHandlerInfo(elementName, NAMESPACE);
    }

    public final IQHandlerInfo getInfo()
    {
        return info;
    }

    protected PersistenceManager getPersistenceManager()
    {
        return ArchivePlugin.getInstance().getPersistenceManager();
    }
}
