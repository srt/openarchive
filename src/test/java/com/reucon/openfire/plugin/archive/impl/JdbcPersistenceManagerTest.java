package com.reucon.openfire.plugin.archive.impl;

import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.powermock.api.mockito.PowerMockito.mockStatic;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;

import org.jivesoftware.database.DbConnectionManager;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import com.reucon.openfire.plugin.archive.model.Conversation;

@RunWith(PowerMockRunner.class)
@PrepareForTest({DbConnectionManager.class})
public class JdbcPersistenceManagerTest {

	private JdbcPersistenceManager jdbcPersistenceManager;
	
	@Before
	public void setup() {
		jdbcPersistenceManager = new JdbcPersistenceManager();
	}
	
	@Test
	public void retrievingCollectionWithBareWIthJidWorks() throws SQLException {
		
		mockStatic(DbConnectionManager.class);
		
		// conversation query mocking
		String expectedCollectionQuery = "SELECT c.conversationId,c.startTime,c.endTime,c.version,c.ownerJid,c.ownerResource," +
				"c.withJid,c.withResource, c.subject,c.thread FROM archiveConversations AS c WHERE c.ownerJid = ? " +
				"AND c.withJid = ? AND c.startTime = ? ";
		
		Connection con = mock(Connection.class);
		PreparedStatement preparedCollectionStatement = mock(PreparedStatement.class);
		ResultSet rs = mock(ResultSet.class);
		
		when(DbConnectionManager.getConnection()).thenReturn(con);
		when(con.prepareStatement(expectedCollectionQuery)).thenReturn(preparedCollectionStatement);
		when(preparedCollectionStatement.executeQuery()).thenReturn(rs);
		
		// return one conversation
		when(rs.next()).thenReturn(true).thenReturn(false);
		when(rs.getLong(1)).thenReturn(1L);
		when(rs.getLong(2)).thenReturn(1340285010000L);
		when(rs.getLong(3)).thenReturn(1340285010000L);
		when(rs.getString(4)).thenReturn("string1");
		when(rs.getString(5)).thenReturn("string2");
		when(rs.getString(6)).thenReturn("string3");
		when(rs.getString(7)).thenReturn("string4");
		when(rs.getString(8)).thenReturn("string5");
		when(rs.getString(9)).thenReturn("string6");
		
		// participant query mocking - we don't care about what comes back in the participant query in this test
		PreparedStatement preparedParticipantStatement = mock(PreparedStatement.class);
		ResultSet participantResultSet = mock(ResultSet.class);
		
		String expectedParticipantQuery = "SELECT participantId,startTime,endTime,jid FROM archiveParticipants " +
				"WHERE conversationId =? ORDER BY startTime";
		
		when(con.prepareStatement(expectedParticipantQuery)).thenReturn(preparedParticipantStatement);
		
		when(preparedParticipantStatement.executeQuery()).thenReturn(participantResultSet);
		
		// message query - we don't care about what comes back in the message query in this test
		PreparedStatement preparedMessageStatement = mock(PreparedStatement.class);
		ResultSet messageResultSet = mock(ResultSet.class);
		
		String expectedMessageQuery = "SELECT messageId,time,direction,type,subject,body " +
                "FROM archiveMessages WHERE conversationId = ? ORDER BY time";
		
		when(con.prepareStatement(expectedMessageQuery)).thenReturn(preparedCollectionStatement);
		
		when(preparedMessageStatement.executeQuery()).thenReturn(messageResultSet);
		
		// method under test
		Conversation conversation = jdbcPersistenceManager.getConversation("me@me.com",
				"remoteparty@remote.com", new Date(1340285010000L));
		
		// verify that we got a conversation back from the persistence manager
		assertNotNull(conversation);
		
		// don't care about the values of the conversation in this test
		// - only whether the conversation was retrieved correctly with correct parameters
		
		// verify that the prepared statement for the conversation query had the correct invocations
		verify(preparedCollectionStatement).setString(1, "me@me.com");
		verify(preparedCollectionStatement).setString(2, "remoteparty@remote.com");
		verify(preparedCollectionStatement).setLong(3, 1340285010000L);
	}
	
	
	@Test
	public void retrievingCollectionSplitsExactJIDForWithBeforeQuery() throws SQLException {
		
		mockStatic(DbConnectionManager.class);
		
		// conversation query mocking
		String expectedCollectionQuery = "SELECT c.conversationId,c.startTime,c.endTime,c.version,c.ownerJid,c.ownerResource," +
				"c.withJid,c.withResource, c.subject,c.thread FROM archiveConversations AS c WHERE c.ownerJid = ? " +
				"AND c.withJid = ? AND c.withResource = ? AND c.startTime = ? ";
		
		Connection con = mock(Connection.class);
		PreparedStatement preparedCollectionStatement = mock(PreparedStatement.class);
		ResultSet rs = mock(ResultSet.class);
		
		when(DbConnectionManager.getConnection()).thenReturn(con);
		when(con.prepareStatement(expectedCollectionQuery)).thenReturn(preparedCollectionStatement);
		when(preparedCollectionStatement.executeQuery()).thenReturn(rs);
		
		// return one conversation
		when(rs.next()).thenReturn(true).thenReturn(false);
		when(rs.getLong(1)).thenReturn(1L);
		when(rs.getLong(2)).thenReturn(1340285010000L);
		when(rs.getLong(3)).thenReturn(1340285010000L);
		when(rs.getString(4)).thenReturn("string1");
		when(rs.getString(5)).thenReturn("string2");
		when(rs.getString(6)).thenReturn("string3");
		when(rs.getString(7)).thenReturn("string4");
		when(rs.getString(8)).thenReturn("string5");
		when(rs.getString(9)).thenReturn("string6");
		
		// participant query mocking - we don't care about what comes back in the participant query in this test
		PreparedStatement preparedParticipantStatement = mock(PreparedStatement.class);
		ResultSet participantResultSet = mock(ResultSet.class);
		
		String expectedParticipantQuery = "SELECT participantId,startTime,endTime,jid FROM archiveParticipants " +
				"WHERE conversationId =? ORDER BY startTime";
		
		when(con.prepareStatement(expectedParticipantQuery)).thenReturn(preparedParticipantStatement);
		
		when(preparedParticipantStatement.executeQuery()).thenReturn(participantResultSet);
		
		// message query - we don't care about what comes back in the message query in this test
		PreparedStatement preparedMessageStatement = mock(PreparedStatement.class);
		ResultSet messageResultSet = mock(ResultSet.class);
		
		String expectedMessageQuery = "SELECT messageId,time,direction,type,subject,body " +
                "FROM archiveMessages WHERE conversationId = ? ORDER BY time";
		
		when(con.prepareStatement(expectedMessageQuery)).thenReturn(preparedCollectionStatement);
		
		when(preparedMessageStatement.executeQuery()).thenReturn(messageResultSet);
		
		// method under test
		Conversation conversation = jdbcPersistenceManager.getConversation("me@me.com",
				"remoteparty@remote.com/remote_resource", new Date(1340285010000L));
		
		// verify that we got a conversation back from the persistence manager
		assertNotNull(conversation);
		
		// don't care about the values of the conversation in this test
		// - only whether the conversation was retrieved correctly with correct parameters
		
		// verify that the prepared statement for the conversation query had the correct invocations
		verify(preparedCollectionStatement).setString(1, "me@me.com");
		verify(preparedCollectionStatement).setString(2, "remoteparty@remote.com");
		verify(preparedCollectionStatement).setString(3, "remote_resource");
		verify(preparedCollectionStatement).setLong(4, 1340285010000L);
	}
}
