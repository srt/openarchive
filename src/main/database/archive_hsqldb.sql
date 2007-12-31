CREATE TABLE archiveConversations (
  conversationId        BIGINT          NOT NULL PRIMARY KEY,
  startTime             BIGINT          NOT NULL,
  endTime               BIGINT          NOT NULL,
  ownerJid              VARCHAR(255)    NOT NULL,
  ownerResource         VARCHAR(255),
  withJid               VARCHAR(255)    NOT NULL,
  withResource          VARCHAR(255),
  subject               VARCHAR(255),
  thread                VARCHAR(255)
);
CREATE INDEX idx_archiveConversations_startTime ON archiveConversations (startTime);
CREATE INDEX idx_archiveConversations_endTime ON archiveConversations (endTime);
CREATE INDEX idx_archiveConversations_ownerJid ON archiveConversations (ownerJid);
CREATE INDEX idx_archiveConversations_withJid ON archiveConversations (withJid);

CREATE TABLE archiveParticipants (
  participantId         BIGINT          NOT NULL PRIMARY KEY,
  startTime             BIGINT          NOT NULL,
  endTime               BIGINT,
  jid                   VARCHAR(255)    NOT NULL,
  nick                  VARCHAR(255),
  conversationId        BIGINT          NOT NULL
);
CREATE INDEX idx_archiveParticipants_conversationId ON archiveParticipants (conversationId);
CREATE INDEX idx_archiveParticipants_jid ON archiveParticipants (jid);

CREATE TABLE archiveMessages (
  messageId             BIGINT          NOT NULL PRIMARY KEY,
  time                  BIGINT          NOT NULL,
  direction             CHAR(4)         NOT NULL,
  type                  CHAR(15)        NOT NULL,
  subject               VARCHAR(255),
  body                  LONGVARCHAR,
  conversationId        BIGINT          NOT NULL
);
CREATE INDEX idx_archiveMessages_conversationId ON archiveMessages (conversationId);
CREATE INDEX idx_archiveMessages_time ON archiveMessages (time);

INSERT INTO jiveVersion (name, version) VALUES ('archive', 2);