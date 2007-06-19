package com.reucon.openfire.plugin.archive.model;

/**
 *
 */
public class ArchiveMethod
{
    public enum Usage
    {
        forbid,
        concide,
        prefer
    }

    private String type;
    private Usage usage;

    public String getType()
    {
        return type;
    }

    public void setType(String type)
    {
        this.type = type;
    }

    public Usage getUsage()
    {
        return usage;
    }

    public void setUsage(Usage usage)
    {
        this.usage = usage;
    }
}
