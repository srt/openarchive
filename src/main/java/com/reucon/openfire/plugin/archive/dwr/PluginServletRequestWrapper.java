package com.reucon.openfire.plugin.archive.dwr;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;

/**
 * Fixes serlvetPath and pathInfo for servlets used within Openfire plugins.
 * <p>
 * Currently only works for servlets mapped to <tt>/[some-name]/*</tt>.
 */
public class PluginServletRequestWrapper extends HttpServletRequestWrapper
{
    private String servletPath;
    private String pathInfo;

    public PluginServletRequestWrapper(HttpServletRequest httpServletRequest)
    {
        super(httpServletRequest);

        final StringBuilder servletPathSB = new StringBuilder();
        final StringBuilder pathInfoSB = new StringBuilder();
        final String originalServletPath = super.getServletPath();
        final String originalPathInfo = super.getPathInfo();

        servletPathSB.append(originalServletPath);

        int numSlashes = 0;
        for(int i = 0; i < originalPathInfo.length(); i++)
        {
            final char c = originalPathInfo.charAt(i);
            if (c == '/')
            {
                numSlashes++;
            }

            if (numSlashes < 3)
            {
                servletPathSB.append(c);
            }
            else
            {
                pathInfoSB.append(c);
            }
        }

        this.servletPath = servletPathSB.toString();
        this.pathInfo = pathInfoSB.toString();
    }

    public String getServletPath()
    {
        return servletPath;
    }

    public String getPathInfo()
    {
        return pathInfo;
    }
}
