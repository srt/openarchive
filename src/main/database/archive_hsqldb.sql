CREATE TABLE archiveConversations (
  conversationId        BIGINT          NOT NULL PRIMARY KEY,
  startTime             BIGINT          NOT NULL,
  endTime               BIGINT          NOT NULL,
  ownerJid              VARCHAR(255)    NOT NULL,
  withJid               VARCHAR(255)    NOT NULL,
);

CREATE TABLE archiveParticipants (
  participantId         BIGINT          NOT NULL PRIMARY KEY,
  startTime             BIGINT          NOT NULL,
  endTime               BIGINT,
  jid                   VARCHAR(255)    NOT NULL,
  nick                  VARCHAR(255),
  conversationId        BIGINT          NOT NULL
);
CREATE INDEX idx_archiveParticipants_conversationId ON archiveParticipants (conversationId);

CREATE TABLE archiveMessages (
  messageId             BIGINT          NOT NULL PRIMARY KEY,
  time                  BIGINT          NOT NULL,
  originalId            VARCHAR(255),
  fromJid               VARCHAR(255)    NOT NULL,
  fromResource          VARCHAR(255),
  toJid                 VARCHAR(255)    NOT NULL,
  toResource            VARCHAR(255),
  peerIpAddress         VARCHAR(255),
  type                  CHAR(15)        NOT NULL,
  thread                VARCHAR(255),
  subject               VARCHAR(255),
  body                  LONGVARCHAR,
  conversationId        BIGINT NOT NULL
);
CREATE INDEX idx_archiveMessages_conversationId ON archiveMessages (conversationId);

INSERT INTO jiveVersion (name, version) VALUES ('archive', 2);