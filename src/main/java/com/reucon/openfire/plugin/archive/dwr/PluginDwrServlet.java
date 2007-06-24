package com.reucon.openfire.plugin.archive.dwr;

import org.directwebremoting.servlet.DwrServlet;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Enumeration;
import java.util.Hashtable;

/**
 * Wrapper around DwrServlet to make it work in Openfire plugins.
 */
public class PluginDwrServlet extends DwrServlet
{
    private final Hashtable<String, String> initParameters;
    private ServletConfig servletConfig;

    public PluginDwrServlet()
    {
        super();

        // Openfire's PluginManager doesn't use the init-params we put into web.xml
        // so we set the parameters here and pass them to DwrServlet's init method.
        initParameters = new Hashtable<String, String>();
        //initParameters.put("debug", "true");
        initParameters.put("customConfigurator", PluginDwrConfigurator.class.getName());
    }

    public void init(ServletConfig servletConfig) throws ServletException
    {
        final ClassLoader originalClassLoader;

        // Save servlet config for later use.
        this.servletConfig = servletConfig;

        // DWR's IoC container loads classes from the current thread's class loader so
        // we set the class loader to the plugin's class loader while in DWR.
        // see also: http://getahead.org/bugs/browse/DWR-98
        originalClassLoader = Thread.currentThread().getContextClassLoader();
        try
        {
            Thread.currentThread().setContextClassLoader(getClass().getClassLoader());
            super.init(this);
        }
        finally
        {
            Thread.currentThread().setContextClassLoader(originalClassLoader);
        }
    }

    public void destroy()
    {
        final ClassLoader originalClassLoader;

        originalClassLoader = Thread.currentThread().getContextClassLoader();
        try
        {
            Thread.currentThread().setContextClassLoader(getClass().getClassLoader());
            super.destroy();
            //servletConfig.getServletContext().setAttribute(ContainerUtil.ATTRIBUTE_CONTAINER_LIST, null);
        }
        finally
        {
            Thread.currentThread().setContextClassLoader(originalClassLoader);
        }
    }

    protected void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException
    {
        final PluginServletRequestWrapper requestWrapper;
        final ClassLoader originalClassLoader;

        // Wrap the request so servletPath and pathInfo get fixed.
        requestWrapper = new PluginServletRequestWrapper(req);

        originalClassLoader = Thread.currentThread().getContextClassLoader();
        try
        {
            Thread.currentThread().setContextClassLoader(getClass().getClassLoader());
            super.service(requestWrapper, resp);
        }
        finally
        {
            Thread.currentThread().setContextClassLoader(originalClassLoader);
        }
    }

    public ServletConfig getServletConfig()
    {
        return servletConfig;
    }

    public String getInitParameter(String name)
    {
        return initParameters.get(name);
    }

    public Enumeration getInitParameterNames()
    {
        return initParameters.keys();
    }
}
