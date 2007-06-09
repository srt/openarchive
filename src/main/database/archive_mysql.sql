CREATE TABLE archiveConversations (
  conversationId        BIGINT          NOT NULL,
  start                 BIGINT          NOT NULL,
  end                   BIGINT          NOT NULL,
  roomJid               VARCHAR(255),
  PRIMARY KEY (conversationId)
);

CREATE TABLE archiveParticipants (
  participantId         BIGINT          NOT NULL,
  start                 BIGINT          NOT NULL,
  end                   BIGINT,
  jid                   VARCHAR(255)    NOT NULL,
  nick                  VARCHAR(255),
  conversationId        BIGINT          NOT NULL,
  PRIMARY KEY (participantId),
  INDEX idx_archiveParticipants_conversationId (conversationId)
);

CREATE TABLE archiveMessages (
  messageId             BIGINT          NOT NULL,
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
  body                  TEXT,
  conversationId        BIGINT          NOT NULL,
  PRIMARY KEY (messageId),
  INDEX idx_archiveMessages_conversationId (conversationId)
);

INSERT INTO jiveVersion (name, version) VALUES ('archive', 1);