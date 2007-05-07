DROP TABLE IF EXISTS archiverMessages;
DROP TABLE IF EXISTS archiverConversations;
DROP TABLE IF EXISTS archiverParticipants;

CREATE TABLE archiverConversations (
  conversationId        BIGINT          NOT NULL,
  start                 BIGINT          NOT NULL,
  end                   BIGINT          NOT NULL,
  roomJid               VARCHAR(255),
  PRIMARY KEY (conversationId)
);

CREATE TABLE archiverParticipants (
  participantId         BIGINT          NOT NULL,
  start                 BIGINT          NOT NULL,
  end                   BIGINT,
  jid                   VARCHAR(255)    NOT NULL,
  nick                  VARCHAR(255),
  conversationId        BIGINT          NOT NULL,
  PRIMARY KEY (participantId),
  INDEX idx_archiverParticipants_conversationId (conversationId)
);

CREATE TABLE archiverMessages (
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
  INDEX idx_archiverMessages_conversationId (conversationId)
);

INSERT INTO jiveVersion (name, version) VALUES ('archiver', 1);