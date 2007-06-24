package com.reucon.openfire.plugin.archive.xep0059;

import org.dom4j.DocumentFactory;
import org.dom4j.Element;

/**
 * A <a href="http://www.xmpp.org/extensions/xep-0059.html">XEP-0059</a> result set.
 */
public class XmppResultSet
{
    public static String NAMESPACE = "http://jabber.org/protocol/rsm";
    private String before;
    private String after;
    private int index = -1;
    private int max = -1;
    private String first;
    private int firstIndex = -1;
    private String last;
    private int count = -1;

    public XmppResultSet(Element setElement)
    {
        this.before = setElement.attributeValue("before");
        this.after = setElement.attributeValue("after");
        if (setElement.attribute("max") != null)
        {
            try
            {
                this.max = Integer.parseInt(setElement.attributeValue("max"));
            }
            catch (Exception e)
            {
                // swallow
            }
        }
        if (setElement.attribute("index") != null)
        {
            try
            {
                this.max = Integer.parseInt(setElement.attributeValue("index"));
            }
            catch (Exception e)
            {
                // swallow
            }
        }
    }

    public String getBefore()
    {
        return before;
    }

    public String getAfter()
    {
        return after;
    }

    /**
     * Returns the index of the first element to return.
     *
     * @return the index of the first element to return.
     */
    public int getIndex()
    {
        return index;
    }

    /**
     * Returns the maximum number of items to return.
     *
     * @return the maximum number of items to return.
     */
    public int getMax()
    {
        return max;
    }

    /**
     * Sets the id of the first element returned.
     *
     * @param first the id of the first element returned.
     */
    public void setFirst(String first)
    {
        this.first = first;
    }

    /**
     * Sets the index of the first element returned.
     *
     * @param firstIndex the index of the first element returned.
     */
    public void setFirstIndex(int firstIndex)
    {
        this.firstIndex = firstIndex;
    }

    /**
     * Sets the id of the last element returned.
     *
     * @param last the id of the last element returned.
     */
    public void setLast(String last)
    {
        this.last = last;
    }

    /**
     * Sets the number of elements returned.
     *
     * @param count the number of elements returned.
     */
    public void setCount(int count)
    {
        this.count = count;
    }

    public Element createResultElement()
    {
        final Element set;

        set = DocumentFactory.getInstance().createElement("set", NAMESPACE);
        if (first != null || firstIndex != -1)
        {
            final Element firstElement = set.addElement("first");
            if (first != null)
            {
                firstElement.setText(first);
            }
            if (firstIndex != -1)
            {
                firstElement.addAttribute("index", Integer.toString(firstIndex));
            }
        }
        if (last != null)
        {
            set.addElement("last").setText(last);
        }
        if (count != -1)
        {
            set.addElement("count").setText(Integer.toString(count));
        }

        return set;
    }
}
