-- =============================================================================
-- Step 4: Submission Schema & History
-- =============================================================================

CREATE TABLE IF NOT EXISTS submissions (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    problem_id BIGINT NOT NULL,
    language VARCHAR(20) NOT NULL,
    source_code TEXT NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    verdict VARCHAR(30),
    execution_time_ms INT,
    memory_used_kb INT,
    error_output TEXT,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_submissions_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE,
    CONSTRAINT fk_submissions_problem FOREIGN KEY (problem_id) REFERENCES problems (id) ON DELETE CASCADE,
    CONSTRAINT chk_submissions_language CHECK (language IN ('JAVA', 'CPP')),
    CONSTRAINT chk_submissions_status CHECK (status IN ('PENDING', 'QUEUED', 'RUNNING', 'COMPLETED', 'FAILED')),
    CONSTRAINT chk_submissions_verdict CHECK (verdict IS NULL OR verdict IN (
        'ACCEPTED', 'WRONG_ANSWER', 'TIME_LIMIT_EXCEEDED', 'MEMORY_LIMIT_EXCEEDED',
        'COMPILATION_ERROR', 'RUNTIME_ERROR', 'INTERNAL_ERROR'
    ))
);

CREATE INDEX idx_submissions_user_id ON submissions (user_id);
CREATE INDEX idx_submissions_problem_id ON submissions (problem_id);
CREATE INDEX idx_submissions_status ON submissions (status);
CREATE INDEX idx_submissions_created_at ON submissions (created_at DESC);
CREATE INDEX idx_submissions_user_prob ON submissions (user_id, problem_id, created_at DESC);
