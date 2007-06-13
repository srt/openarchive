package com.reucon.openfire.plugin.archive.xep0136;

import com.reucon.openfire.plugin.archive.util.DateUtil;
import org.dom4j.Element;
import org.dom4j.QName;

import java.util.Date;

/**
 * A request to retrieve a collection.
 */
public class RetrieveRequest
{
    private String with;
    private Date start;

    private int max = -1;
    private String after;

    public RetrieveRequest(Element listElement)
    {
        this.with = listElement.attributeValue("with");
        this.start = DateUtil.parseDate(listElement.attributeValue("start"));

        Element rsmElement = listElement.element(QName.get("set", "http://jabber.org/protocol/rsm"));
        if (rsmElement != null)
        {
            if (rsmElement.attribute("max") != null)
            {
                this.max = Integer.parseInt(rsmElement.attributeValue("max"));
            }
            this.after = rsmElement.attributeValue("after");
        }
    }

    public String getWith()
    {
        return with;
    }

    public Date getStart()
    {
        return start;
    }

    public int getMax()
    {
        return max;
    }

    public String getAfter()
    {
        return after;
    }
}
