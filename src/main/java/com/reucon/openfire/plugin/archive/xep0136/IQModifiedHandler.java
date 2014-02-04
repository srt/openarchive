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

import java.util.Date;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.dom4j.Element;
import org.jivesoftware.openfire.auth.UnauthorizedException;
import org.xmpp.packet.IQ;
import org.xmpp.packet.JID;

import com.reucon.openfire.plugin.archive.model.Conversation;
import com.reucon.openfire.plugin.archive.util.XmppDateUtil;
import com.reucon.openfire.plugin.archive.xep0059.XmppResultSet;

public class IQModifiedHandler extends AbstractIQHandler {

    public IQModifiedHandler()
    {
        super("Message Archiving Modified Handler", "modified");
    }

	@Override
	public IQ handleIQ(IQ packet) throws UnauthorizedException {
		IQ reply = IQ.createResultIQ(packet);
        ListRequest listRequest = new ListRequest(packet.getChildElement());
        JID from = packet.getFrom();

        Element modifiedElement = reply.setChildElement("modified", NAMESPACE);
        
        
        List<Conversation> conversations = modified(from, listRequest);
        XmppResultSet resultSet = listRequest.getResultSet();

        for (Conversation conversation : conversations)
        {
            addConversationElement(modifiedElement, conversation);
        }

        if (resultSet != null)
        {
        	modifiedElement.add(resultSet.createResultElement());
        }

        return reply;
	}
	
	private List<Conversation> modified(JID from, ListRequest request)
    {
		// need to query the persistence manager for conversations whose since the start date provided (paginated)
		
        Date start = request.getStart();
        if(request.getResultSet().getAfter() != null) {
        	start = new Date(request.getResultSet().getAfter() + 1);
        }
		return getPersistenceManager().findModifiedConversationsSince(start,
                from.toBareJID(), request.getWith(), request.getResultSet());
    }

    private Element addConversationElement(Element modifiedElement, Conversation conversation)
    {

    	// TODO - if removal ever is implemented, then we need to mark as either
    	// changed OR removed
    	// for now, only changed elements are supported
        Element conversationElement = modifiedElement.addElement("changed");

        StringBuilder builder = new StringBuilder(conversation.getWithJid());
        if(StringUtils.isNotEmpty(conversation.getWithResource())) {
        	builder.append("/").append(conversation.getWithResource());
        }
        conversationElement.addAttribute("with", builder.toString());
        conversationElement.addAttribute("start", XmppDateUtil.formatDate(conversation.getStart()));
        conversationElement.addAttribute("version", conversation.getVersion() + "");

        return conversationElement;
    }
}
