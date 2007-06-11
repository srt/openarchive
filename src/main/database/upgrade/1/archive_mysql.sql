ALTER TABLE archiverConversations RENAME archiveConversations;
ALTER TABLE archiverParticipants RENAME archiveParticipants;
ALTER TABLE archiverMessages RENAME archiveMessages;

UPDATE jiveVersion SET version=1 WHERE name='archive';