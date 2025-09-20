-- Enable UUID extension
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- Users table stores the core user profile.
CREATE TABLE users (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- Identities table stores various authentication methods for a user.
CREATE TABLE identities (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    provider VARCHAR(50) NOT NULL,
    provider_id VARCHAR(255) NOT NULL,
    password_hash BYTEA,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    UNIQUE (provider, provider_id)
);

CREATE INDEX idx_identities_user_id ON identities(user_id);

-- OTPs table for one-time password login.
CREATE TABLE otps (
    identifier VARCHAR(255) PRIMARY KEY,
    code VARCHAR(10) NOT NULL,
    expires_at TIMESTAMPTZ NOT NULL
);

-- WeChat Sessions table for QR code login flow.
CREATE TABLE wechat_sessions (
    session_id UUID PRIMARY KEY,
    ticket VARCHAR(255) NOT NULL,
    status VARCHAR(50) NOT NULL,
    user_id UUID,
    token TEXT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_wechat_sessions_ticket ON wechat_sessions(ticket);
