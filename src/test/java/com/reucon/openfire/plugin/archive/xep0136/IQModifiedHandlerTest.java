/**
 *
 * Copyright (C) 2012  Taylor Raack <taylor@raack.info>
 * 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */

package com.reucon.openfire.plugin.archive.xep0136;

import static org.junit.Assert.assertEquals;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.argThat;
import static org.mockito.Matchers.eq;
import static org.mockito.Matchers.isNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.powermock.api.mockito.PowerMockito.mockStatic;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

import org.dom4j.Element;
import org.dom4j.Namespace;
import org.dom4j.QName;
import org.dom4j.dom.DOMElement;
import org.dom4j.tree.DefaultElement;
import org.jivesoftware.openfire.auth.UnauthorizedException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatcher;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.xmpp.packet.IQ;

import com.reucon.openfire.plugin.archive.ArchivePlugin;
import com.reucon.openfire.plugin.archive.PersistenceManager;
import com.reucon.openfire.plugin.archive.model.Conversation;
import com.reucon.openfire.plugin.archive.util.XmppDateUtil;
import com.reucon.openfire.plugin.archive.xep0059.XmppResultSet;

@RunWith(PowerMockRunner.class)
@PrepareForTest( { ArchivePlugin.class })
public class IQModifiedHandlerTest {

	private IQModifiedHandler handler;
	
	private PersistenceManager persistenceManager;
	
	@Before
	public void setup() {
		handler = new IQModifiedHandler();
		
		ArchivePlugin plugin = mock(ArchivePlugin.class);
		persistenceManager = mock(PersistenceManager.class);
		mockStatic(ArchivePlugin.class);
		when(ArchivePlugin.getInstance()).thenReturn(plugin);
		when(plugin.getPersistenceManager()).thenReturn(persistenceManager);
	}
	
	@Test
	public void basicEmptyModifiedResults() throws UnauthorizedException {
		
		// need to capture XMPPResultSet argument
        int maxResults = 30;
		String startTime = "1469-07-21T02:56:15Z";
        
		IQ packet = createModifiedRequest(maxResults, startTime, null);
		
		final List<Conversation> conversations = new ArrayList<Conversation>();
		
		mockPersistenceResponse(XmppDateUtil.parseDate(startTime), null, maxResults, conversations);
		
		// method under test
		IQ reply = handler.handleIQ(packet);
		
		// verification
		assertEquals(IQ.Type.result, reply.getType());
		assertEquals("thejid", reply.getTo().toString());
		assertNotNull(reply.getChildElement());
		Element replyModified = reply.getChildElement();
		assertEquals("modified", replyModified.getName());
		assertEquals("urn:xmpp:archive", replyModified.getNamespace().getText());
		
		// should have one child the set
		assertEquals(1, replyModified.nodeCount());
		Element replySetElement = replyModified.element(QName.get("set", XmppResultSet.NAMESPACE));
		assertNotNull(replySetElement);
		assertEquals("set", replySetElement.getName());
		assertEquals("http://jabber.org/protocol/rsm", replySetElement.getNamespace().getText());

		Element countElement = replySetElement.element("count");
		assertEquals("0", countElement.getText());
	}

	
	
	@Test
	public void conversationsReturned() throws UnauthorizedException {

		// need to capture XMPPResultSet argument
        int maxResults = 30;
		String startTime = "1469-07-21T02:56:15Z";
        
		IQ packet = createModifiedRequest(maxResults, startTime, null);
		
		Conversation conversation1 = new Conversation(date("2012/04/27 14:27:28"), date("2012/04/27 14:27:28"), 2L, "thejid", "theresource", "otherjid", "otherresource", null, null);
		Conversation conversation2 = new Conversation(date("2012/04/28 12:39:10"), date("2012/04/29 18:34:55"), 4L, "thejid", "theresource", "otherjid2", "otherresource2", null, null);
		final List<Conversation> conversations = Arrays.asList(new Conversation[] {conversation1, conversation2});
		
		mockPersistenceResponse(XmppDateUtil.parseDate(startTime), null, maxResults, conversations);
		
		// method under test
		IQ reply = handler.handleIQ(packet);
		
		// verification
		Element replyElement = reply.getChildElement();
		assertEquals(IQ.Type.result, reply.getType());
		assertEquals("thejid", reply.getTo().toString());
		assertNotNull(reply.getChildElement());
		Element replyModified = reply.getChildElement();
		assertEquals("modified", replyModified.getName());
		assertEquals("urn:xmpp:archive", replyModified.getNamespace().getText());
		
		// should have one child the set
		assertEquals(3, replyModified.nodeCount());
		Element replySetElement = replyModified.element(QName.get("set", XmppResultSet.NAMESPACE));
		assertNotNull(replySetElement);
		assertEquals("set", replySetElement.getName());
		assertEquals("http://jabber.org/protocol/rsm", replySetElement.getNamespace().getText());
		Element countElement = replySetElement.element("count");
		assertEquals("2", countElement.getText());
		
		Element lastElement = replySetElement.element("last");
		assertEquals(conversation2.getEnd().getTime() + "", lastElement.getText());
		
		List<Element> changed = replyModified.elements("changed");
		assertEquals(2, changed.size());
		verifyChanged(conversation1, changed.get(0));
		verifyChanged(conversation2, changed.get(1));
	}
	
	@Test
	public void conversationsReturnedWithAfterSpecified() throws UnauthorizedException {

		// need to capture XMPPResultSet argument
        int maxResults = 30;
		String startTime = "1469-07-21T02:56:15.132Z";
		String afterTime = "2012-03-01T02:56:15.765Z";
        
		IQ packet = createModifiedRequest(maxResults, startTime, XmppDateUtil.parseDate(afterTime).getTime() + "");
		
		Conversation conversation1 = new Conversation(date("2012/04/27 14:27:28"), date("2012/04/27 14:27:28"), 2L, "thejid", "theresource", "otherjid", "otherresource", null, null);
		Conversation conversation2 = new Conversation(date("2012/04/28 12:39:10"), date("2012/04/29 18:34:55"), 4L, "thejid", "theresource", "otherjid2", "otherresource2", null, null);
		final List<Conversation> conversations = Arrays.asList(new Conversation[] {conversation1, conversation2});

		// expect the persistence layer to look just after the afterDate
		Date afterDate = new Date(XmppDateUtil.parseDate(afterTime).getTime() + 1);
		mockPersistenceResponse(XmppDateUtil.parseDate(startTime), afterDate, maxResults, conversations);
		
		// method under test
		IQ reply = handler.handleIQ(packet);
		
		verify(persistenceManager).findModifiedConversationsSince(eq(afterDate), any(String.class), any(String.class), argThat(hasMaxResults(maxResults)));
		
		
		// verification
		Element replyElement = reply.getChildElement();
		assertEquals(IQ.Type.result, reply.getType());
		assertEquals("thejid", reply.getTo().toString());
		assertNotNull(reply.getChildElement());
		Element replyModified = reply.getChildElement();
		assertEquals("modified", replyModified.getName());
		assertEquals("urn:xmpp:archive", replyModified.getNamespace().getText());
		
		// should have one child the set
		assertEquals(3, replyModified.nodeCount());
		Element replySetElement = replyModified.element(QName.get("set", XmppResultSet.NAMESPACE));
		assertNotNull(replySetElement);
		assertEquals("set", replySetElement.getName());
		assertEquals("http://jabber.org/protocol/rsm", replySetElement.getNamespace().getText());
		Element countElement = replySetElement.element("count");
		assertEquals("2", countElement.getText());
		
		Element lastElement = replySetElement.element("last");
		assertEquals(conversation2.getEnd().getTime() + "", lastElement.getText());
		
		List<Element> changed = replyModified.elements("changed");
		assertEquals(2, changed.size());
		verifyChanged(conversation1, changed.get(0));
		verifyChanged(conversation2, changed.get(1));
		
		}

	private void verifyChanged(Conversation conversation, Element element) {
		assertEquals(conversation.getWithJid() + "/" + conversation.getWithResource(), element.attribute("with").getText());
		assertEquals(dateUTC(conversation.getStart()), element.attribute("start").getText());
		assertEquals(conversation.getVersion() + "", element.attribute("version").getText());
	}
	
	private String dateUTC(Date date) {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
		sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
		return sdf.format(date);
	}
	
	private Date date(String dateString) {
		String format = "yyyy/MM/dd HH:mm:ss";
		try {
			return new SimpleDateFormat(format, Locale.ENGLISH).parse(dateString);
		} catch (ParseException e) {
			throw new RuntimeException("Cannot convert " + dateString + " to " + format, e);
		}
	}
	
	private void mockPersistenceResponse(Date date, Date afterDate, int maxResults, final List<Conversation> conversations) {
		if(afterDate != null) {
			date = afterDate;
		}
		when(persistenceManager.findModifiedConversationsSince(eq(date), eq("thejid"), isNull(String.class), argThat(hasMaxResults(maxResults))))
		.thenAnswer(new Answer<List<Conversation>>() {
			@Override
			public List<Conversation> answer(InvocationOnMock invocation) throws Throwable {
				Object args[] = invocation.getArguments();
				
				// add empty result set
				XmppResultSet resultSet = (XmppResultSet)args[3];
				if(conversations.size() > 0) {
					resultSet.setLast(conversations.get(conversations.size() - 1).getEnd().getTime());
				}
				resultSet.setCount(conversations.size());
				
				return conversations;
			}});
	}


	private ArgumentMatcher<XmppResultSet> hasMaxResults(final int maxResults) {
		return new ArgumentMatcher<XmppResultSet>() {
			public boolean matches(Object argument) {
				XmppResultSet resultSet = (XmppResultSet)argument;
				return resultSet.getMax() == maxResults;
			}};
	}

	private IQ createModifiedRequest(int maxResults, String startTime, String afterTime) {
		Element modifiedElement = new DOMElement("modified", Namespace.get("urn:xmpp:archive"));
		modifiedElement.addAttribute("start", startTime);
        Element setElement = new DefaultElement("set", Namespace.get("http://jabber.org/protocol/rsm"));
		
        Element maxElement = new DefaultElement("max");
        maxElement.setText(maxResults + "");
        setElement.add(maxElement);
        if(afterTime != null) {
	        Element afterElement = new DefaultElement("after");
	        afterElement.setText(afterTime);
	        setElement.add(afterElement);
        }
        modifiedElement.add(setElement);

		IQ packet = new IQ(IQ.Type.get, "sync1");
		packet.setFrom("thejid");
		packet.setChildElement(modifiedElement);
		return packet;
	}
	
}
