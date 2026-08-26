-- =============================================================================
-- Step 1: Initial Problem Management Schema
-- =============================================================================

-- 1. PROBLEMS TABLE
CREATE TABLE IF NOT EXISTS problems (
    id BIGSERIAL PRIMARY KEY,
    slug VARCHAR(120) NOT NULL UNIQUE,
    title VARCHAR(200) NOT NULL,
    description TEXT NOT NULL,
    difficulty VARCHAR(20) NOT NULL,
    time_limit_ms INT NOT NULL DEFAULT 1000,
    memory_limit_mb INT NOT NULL DEFAULT 256,
    is_published BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT chk_problems_difficulty CHECK (difficulty IN ('EASY', 'MEDIUM', 'HARD'))
);

CREATE INDEX idx_problems_slug ON problems (slug);
CREATE INDEX idx_problems_difficulty ON problems (difficulty);
CREATE INDEX idx_problems_is_published ON problems (is_published);

-- 2. TAGS TABLE
CREATE TABLE IF NOT EXISTS tags (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(50) NOT NULL UNIQUE,
    slug VARCHAR(60) NOT NULL UNIQUE
);

-- 3. PROBLEM_TAGS JUNCTION TABLE
CREATE TABLE IF NOT EXISTS problem_tags (
    problem_id BIGINT NOT NULL,
    tag_id BIGINT NOT NULL,
    PRIMARY KEY (problem_id, tag_id),
    CONSTRAINT fk_problem_tags_problem FOREIGN KEY (problem_id) REFERENCES problems (id) ON DELETE CASCADE,
    CONSTRAINT fk_problem_tags_tag FOREIGN KEY (tag_id) REFERENCES tags (id) ON DELETE CASCADE
);

-- 4. PROBLEM_TEST_CASES TABLE
CREATE TABLE IF NOT EXISTS problem_test_cases (
    id BIGSERIAL PRIMARY KEY,
    problem_id BIGINT NOT NULL,
    input_data TEXT NOT NULL,
    expected_output TEXT NOT NULL,
    is_sample BOOLEAN NOT NULL DEFAULT FALSE,
    explanation TEXT,
    order_index INT NOT NULL DEFAULT 0,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_test_cases_problem FOREIGN KEY (problem_id) REFERENCES problems (id) ON DELETE CASCADE
);

CREATE INDEX idx_test_cases_problem_id ON problem_test_cases (problem_id);
