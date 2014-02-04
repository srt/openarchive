ALTER TABLE archiveConversations add column version BIGINT NOT NULL default 0;

UPDATE ofVersion SET version=3 WHERE name='archive';
