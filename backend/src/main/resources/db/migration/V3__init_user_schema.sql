-- =============================================================================
-- Step 2: User and Problem Solving Statistics Schema
-- =============================================================================

-- 1. USERS TABLE
CREATE TABLE IF NOT EXISTS users (
    id BIGSERIAL PRIMARY KEY,
    username VARCHAR(50) NOT NULL,
    email VARCHAR(255) NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    role VARCHAR(20) NOT NULL DEFAULT 'USER',
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_users_username UNIQUE (username),
    CONSTRAINT uq_users_email UNIQUE (email),
    CONSTRAINT chk_users_role CHECK (role IN ('USER', 'ADMIN'))
);

CREATE INDEX idx_users_username ON users (username);
CREATE INDEX idx_users_email ON users (email);

-- 2. USER_STATS TABLE
CREATE TABLE IF NOT EXISTS user_stats (
    user_id BIGINT PRIMARY KEY,
    easy_solved INT NOT NULL DEFAULT 0,
    medium_solved INT NOT NULL DEFAULT 0,
    hard_solved INT NOT NULL DEFAULT 0,
    total_submissions INT NOT NULL DEFAULT 0,
    accepted_submissions INT NOT NULL DEFAULT 0,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_user_stats_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE
);
