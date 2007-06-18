CREATE TABLE archiveConversations (
  conversationId        BIGINT          NOT NULL,
  startTime             BIGINT          NOT NULL,
  endTime               BIGINT          NOT NULL,
  ownerJid              VARCHAR(255)    NOT NULL,
  withJid               VARCHAR(255)    NOT NULL,
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
  INDEX idx_archiveMessages_conversationId (conversationId),
  INDEX idx_archiveMessages_time (time)
);

CREATE TABLE archivePrefItems (
  ownerJid              VARCHAR(255)    NOT NULL,
  jid                   VARCHAR(255),
  saveMode              INTEGER,
  otrMode               INTEGER,
  expireTime            BIGINT
  PRIMARY KEY (ownerJid,jid)
}

CREATE TABLE archivePrefMethods (
  ownerJid              VARCHAR(255)    NOT NULL,
  method                VARCHAR(255)    NOT NULL,
  use                   INTEGER,
  PRIMARY KEY (ownerJid,method)
}

INSERT INTO jiveVersion (name, version) VALUES ('archive', 2);