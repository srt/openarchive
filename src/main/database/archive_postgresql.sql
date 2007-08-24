CREATE TABLE archiveConversations (
  conversationId        BIGINT          NOT NULL,
  startTime             BIGINT          NOT NULL,
  endTime               BIGINT          NOT NULL,
  ownerJid              VARCHAR(255)    NOT NULL,
  ownerResource         VARCHAR(255),
  withJid               VARCHAR(255)    NOT NULL,
  withResource          VARCHAR(255),
  subject               VARCHAR(255),
  thread                VARCHAR(255),
  CONSTRAINT archiveConversations_pk PRIMARY KEY (conversationId)
);
CREATE INDEX archiveConversations_startTime_idx ON archiveConversations (startTime);
CREATE INDEX archiveConversations_endTime_idx ON archiveConversations (endTime);
CREATE INDEX archiveConversations_ownerJid_idx ON archiveConversations (ownerJid);
CREATE INDEX archiveConversations_withJid_idx ON archiveConversations (withJid);


CREATE TABLE archiveParticipants (
  participantId         BIGINT          NOT NULL,
  startTime             BIGINT          NOT NULL,
  endTime               BIGINT,
  jid                   VARCHAR(255)    NOT NULL,
  nick                  VARCHAR(255),
  conversationId        BIGINT          NOT NULL,
  CONSTRAINT archiveParticipants_pk PRIMARY KEY (participantId)
);
CREATE INDEX archiveParticipants_conversationId_idx ON archiveParticipants (conversationId);
CREATE INDEX archiveParticipants_jid_idx ON archiveParticipants (jid);

CREATE TABLE archiveMessages (
  messageId             BIGINT          NOT NULL,
  time                  BIGINT          NOT NULL,
  direction             CHAR(4)         NOT NULL,
  type                  CHAR(15)        NOT NULL,
  subject               VARCHAR(255),
  body                  TEXT,
  conversationId        BIGINT          NOT NULL,
  CONSTRAINT archiveMessages_pk PRIMARY KEY (messageId)
);
CREATE INDEX archiveMessages_conversationId_idx ON archiveMessages (conversationId);
CREATE INDEX archiveMessages_time_idx ON archiveMessages (time);

CREATE TABLE archivePrefItems (
  username              VARCHAR(64)     NOT NULL,
  jid                   VARCHAR(255),
  saveMode              INTEGER,
  otrMode               INTEGER,
  expireTime            BIGINT,
  CONSTRAINT archivePrefItems_pk PRIMARY KEY (username,jid)
);

CREATE TABLE archivePrefMethods (
  username              VARCHAR(64)     NOT NULL,
  methodType            VARCHAR(255)    NOT NULL,
  methodUsage           INTEGER,
  CONSTRAINT archivePrefMethods_pk PRIMARY KEY (username,methodType)
);

INSERT INTO jiveVersion (name, version) VALUES ('archive', 2);
