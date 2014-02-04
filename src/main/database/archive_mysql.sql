CREATE TABLE archiveConversations (
  conversationId        BIGINT          NOT NULL,
  startTime             BIGINT          NOT NULL,
  endTime               BIGINT          NOT NULL,
  version               BIGINT          NOT NULL,
  ownerJid              VARCHAR(255)    NOT NULL,
  ownerResource         VARCHAR(255),
  withJid               VARCHAR(255)    NOT NULL,
  withResource          VARCHAR(255),
  subject               VARCHAR(255),
  thread                VARCHAR(255),
  PRIMARY KEY (conversationId),
  INDEX idx_archiveConversations_startTime (startTime),
  INDEX idx_archiveConversations_endTime (endTime),
  INDEX idx_archiveConversations_ownerJid (ownerJid),
  INDEX idx_archiveConversations_withJid (withJid)
);

CREATE TABLE archiveParticipants (
  participantId         BIGINT          NOT NULL,
  startTime             BIGINT          NOT NULL,
  endTime               BIGINT,
  jid                   VARCHAR(255)    NOT NULL,
  nick                  VARCHAR(255),
  conversationId        BIGINT          NOT NULL,
  PRIMARY KEY (participantId),
  INDEX idx_archiveParticipants_conversationId (conversationId),
  INDEX idx_archiveParticipants_jid (jid)
);

CREATE TABLE archiveMessages (
  messageId             BIGINT          NOT NULL,
  time                  BIGINT          NOT NULL,
  direction             CHAR(4)         NOT NULL,
  type                  CHAR(15)        NOT NULL,
  subject               VARCHAR(255),
  body                  TEXT,
  conversationId        BIGINT          NOT NULL,
  PRIMARY KEY (messageId),
  INDEX idx_archiveMessages_conversationId (conversationId),
  INDEX idx_archiveMessages_time (time)
);

CREATE TABLE archivePrefItems (
  username              VARCHAR(64)     NOT NULL,
  jid                   VARCHAR(255),
  saveMode              INTEGER,
  otrMode               INTEGER,
  expireTime            BIGINT,
  PRIMARY KEY (username,jid)
);

CREATE TABLE archivePrefMethods (
  username              VARCHAR(64)     NOT NULL,
  methodType            VARCHAR(255)    NOT NULL,
  methodUsage           INTEGER,
  PRIMARY KEY (username,methodType)
);

INSERT INTO ofVersion (name, version) VALUES ('archive', 3);
