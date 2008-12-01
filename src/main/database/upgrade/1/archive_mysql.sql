ALTER TABLE archiverConversations RENAME archiveConversations;
ALTER TABLE archiverParticipants RENAME archiveParticipants;
ALTER TABLE archiverMessages RENAME archiveMessages;

UPDATE ofVersion SET version=1 WHERE name='archive';
