ALTER TABLE archiveConversations add column version BIGINT DEFAULT 0 NOT NULL;

UPDATE ofVersion SET version=3 WHERE name='archive';
