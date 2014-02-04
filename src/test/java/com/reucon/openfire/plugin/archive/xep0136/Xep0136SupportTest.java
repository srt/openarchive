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

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.jivesoftware.openfire.IQRouter;
import org.jivesoftware.openfire.XMPPServer;
import org.jivesoftware.openfire.disco.IQDiscoInfoHandler;
import org.junit.Test;

public class Xep0136SupportTest {

	@Test
	public void featuresSupported() {
		XMPPServer server = mock(XMPPServer.class);
		IQDiscoInfoHandler discoInfoHandler = mock(IQDiscoInfoHandler.class);
		IQRouter router = mock(IQRouter.class);
		
		when(server.getIQDiscoInfoHandler()).thenReturn(discoInfoHandler);
		when(server.getIQRouter()).thenReturn(router);
		
		Xep0136Support support = new Xep0136Support(server);
		
		support.start();

		verify(discoInfoHandler).addServerFeature("urn:xmpp:archive");
		verify(discoInfoHandler).addServerFeature("urn:xmpp:archive:manage");
		verify(discoInfoHandler).addServerFeature("urn:xmpp:archive:auto");
	}
}
