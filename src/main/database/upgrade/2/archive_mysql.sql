ALTER TABLE archiveConversations CHANGE start startTime BIGINT NOT NULL;
ALTER TABLE archiveConversations CHANGE end endTime BIGINT NOT NULL;
ALTER TABLE archiveParticipants CHANGE start startTime BIGINT NOT NULL;
ALTER TABLE archiveParticipants CHANGE end endTime BIGINT;

UPDATE ofVersion SET version=2 WHERE name='archive';
