package com.reucon.openfire.plugin.archive.xep0136;

import junit.framework.TestCase;
import org.dom4j.Element;
import org.dom4j.Namespace;
import org.dom4j.tree.BaseElement;

/**
 *
 */
public class RetrieveRequestTest extends TestCase
{
    private Element retrieveElement;
    private Element setElement;
    private RetrieveRequest retrieveRequest;


    @Override
    protected void setUp() throws Exception
    {

    }

    public void testWith()
    {
        retrieveElement = new BaseElement("retrieve", Namespace.get("urn:xmpp:archive"));
        retrieveElement.addAttribute("with", "user@example.com");
        retrieveRequest = new RetrieveRequest(retrieveElement);

        assertEquals("user@example.com", retrieveRequest.getWith());
    }

    public void testStart()
    {
        retrieveElement = new BaseElement("retrieve", Namespace.get("urn:xmpp:archive"));
        retrieveElement.addAttribute("start", "2008-07-21T02:56:15.000Z");
        retrieveRequest = new RetrieveRequest(retrieveElement);

        assertEquals(1216608975000l, retrieveRequest.getStart().getTime());
    }

    public void testStartWithoutMillis()
    {
        retrieveElement = new BaseElement("retrieve", Namespace.get("urn:xmpp:archive"));
        retrieveElement.addAttribute("with", "user@example.com");
        retrieveElement.addAttribute("start", "1469-07-21T02:56:15Z");
        setElement = new BaseElement("set", Namespace.get("http://jabber.org/protocol/rsm"));
        setElement.addAttribute("max", "30");
        retrieveElement.add(setElement);
        retrieveRequest = new RetrieveRequest(retrieveElement);

        assertNotNull(retrieveRequest.getStart());
    }

}
