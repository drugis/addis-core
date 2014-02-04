-- liquibase formatted sql

-- changeset reidd:1

create table UserConnection (userId varchar(255) not null,
  providerId varchar(255) not null,
  providerUserId varchar(255),
  rank int not null,
  displayName varchar(255),
  profileUrl varchar(512),
  imageUrl varchar(512),
  accessToken varchar(255) not null,          
  secret varchar(255),
  refreshToken varchar(255),
  expireTime bigint,
  primary key (userId, providerId, providerUserId));
create unique index UserConnectionRank on UserConnection(userId, providerId, rank);

create table Account (id SERIAL NOT NULL,
            username varchar unique,
            firstName varchar not null, 
            lastName varchar not null,
            password varchar default '',
            primary key (id));

CREATE TABLE AccountRoles (
    accountId INT,
    role VARCHAR NOT NULL,
    FOREIGN KEY (accountId) REFERENCES Account(id)
)
