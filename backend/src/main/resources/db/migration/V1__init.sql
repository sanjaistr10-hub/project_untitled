-- V1: accounts, communities (tenants), sign-in codes and sessions.
-- Rules for teammates: never edit a migration that has been merged; add V2__..., V3__... instead.

-- Creators sign in to the platform studio. Email and username are unique platform-wide.
CREATE TABLE creator_account (
    id            uuid         PRIMARY KEY,
    email         varchar(320) NOT NULL,
    full_name     varchar(120),
    username      varchar(40),
    avatar_url    varchar(1024),
    status        varchar(20)  NOT NULL DEFAULT 'ACTIVE',
    last_login_at timestamptz,
    created_at    timestamptz  NOT NULL DEFAULT now(),
    updated_at    timestamptz  NOT NULL DEFAULT now(),
    CONSTRAINT uq_creator_email UNIQUE (email),
    CONSTRAINT uq_creator_username UNIQUE (username)
);

-- A community is the tenant: a creator's white-label space with its own branding and members.
CREATE TABLE community (
    id           uuid         PRIMARY KEY,
    owner_id     uuid         NOT NULL REFERENCES creator_account (id),
    name         varchar(80)  NOT NULL,
    slug         varchar(40)  NOT NULL,
    tagline      varchar(160),
    description  text,
    logo_url     varchar(1024),
    accent_color varchar(7)   NOT NULL DEFAULT '#111111',
    status       varchar(20)  NOT NULL DEFAULT 'ACTIVE',
    created_at   timestamptz  NOT NULL DEFAULT now(),
    updated_at   timestamptz  NOT NULL DEFAULT now(),
    CONSTRAINT uq_community_slug UNIQUE (slug),
    -- v1 allows one community per creator. Drop this constraint to allow several.
    CONSTRAINT uq_community_owner UNIQUE (owner_id)
);

-- Custom domains (learn.creator.com -> community). Only verified rows resolve.
CREATE TABLE community_domain (
    id           uuid         PRIMARY KEY,
    community_id uuid         NOT NULL REFERENCES community (id) ON DELETE CASCADE,
    host         varchar(253) NOT NULL,
    verified_at  timestamptz,
    created_at   timestamptz  NOT NULL DEFAULT now(),
    updated_at   timestamptz  NOT NULL DEFAULT now(),
    CONSTRAINT uq_community_domain_host UNIQUE (host)
);
CREATE INDEX ix_community_domain_community ON community_domain (community_id);

-- Members belong to exactly one community. Same email in two communities = two accounts.
CREATE TABLE member_account (
    id            uuid         PRIMARY KEY,
    community_id  uuid         NOT NULL REFERENCES community (id) ON DELETE CASCADE,
    email         varchar(320) NOT NULL,
    full_name     varchar(120),
    username      varchar(40),
    avatar_url    varchar(1024),
    status        varchar(20)  NOT NULL DEFAULT 'ACTIVE',
    last_login_at timestamptz,
    created_at    timestamptz  NOT NULL DEFAULT now(),
    updated_at    timestamptz  NOT NULL DEFAULT now(),
    CONSTRAINT uq_member_email UNIQUE (community_id, email),
    CONSTRAINT uq_member_username UNIQUE (community_id, username)
);
CREATE INDEX ix_member_community_created ON member_account (community_id, created_at DESC);

-- Emailed sign-in codes. Only an HMAC of the code is stored.
-- scope = 'creator' or 'member:<community id>'.
CREATE TABLE otp_challenge (
    id          uuid         PRIMARY KEY,
    scope       varchar(64)  NOT NULL,
    email       varchar(320) NOT NULL,
    code_hash   varchar(64)  NOT NULL,
    attempts    integer      NOT NULL DEFAULT 0,
    expires_at  timestamptz  NOT NULL,
    consumed_at timestamptz,
    created_at  timestamptz  NOT NULL DEFAULT now(),
    updated_at  timestamptz  NOT NULL DEFAULT now()
);
CREATE INDEX ix_otp_scope_email_created ON otp_challenge (scope, email, created_at DESC);

-- Rotating refresh tokens (sessions). Only a SHA-256 of the token is stored.
CREATE TABLE refresh_token (
    id             uuid         PRIMARY KEY,
    audience       varchar(20)  NOT NULL,
    account_id     uuid         NOT NULL,
    community_id   uuid         REFERENCES community (id) ON DELETE CASCADE,
    token_hash     varchar(64)  NOT NULL,
    expires_at     timestamptz  NOT NULL,
    revoked_at     timestamptz,
    replaced_by_id uuid,
    user_agent     varchar(255),
    created_at     timestamptz  NOT NULL DEFAULT now(),
    updated_at     timestamptz  NOT NULL DEFAULT now(),
    CONSTRAINT uq_refresh_token_hash UNIQUE (token_hash)
);
CREATE INDEX ix_refresh_token_account ON refresh_token (audience, account_id);
