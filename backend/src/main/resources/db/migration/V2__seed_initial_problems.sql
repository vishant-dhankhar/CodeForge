-- =============================================================================
-- Seed Initial Tags and Starter Problems
-- =============================================================================

-- Seed Tags
INSERT INTO tags (name, slug) VALUES
    ('Array', 'array'),
    ('Hash Table', 'hash-table'),
    ('Two Pointers', 'two-pointers'),
    ('String', 'string')
ON CONFLICT (name) DO NOTHING;

-- Seed Problem 1: Two Sum
INSERT INTO problems (id, slug, title, description, difficulty, time_limit_ms, memory_limit_mb, is_published) VALUES
(
    1,
    'two-sum',
    'Two Sum',
    'Given an array of integers `nums` and an integer `target`, return indices of the two numbers such that they add up to `target`.\n\nYou may assume that each input would have exactly one solution, and you may not use the same element twice.\n\n### Example 1:\n```\nInput: nums = [2,7,11,15], target = 9\nOutput: [0,1]\nExplanation: Because nums[0] + nums[1] == 9, we return [0, 1].\n```',
    'EASY',
    1000,
    256,
    TRUE
) ON CONFLICT (slug) DO NOTHING;

-- Map Tags for Problem 1
INSERT INTO problem_tags (problem_id, tag_id)
SELECT 1, id FROM tags WHERE slug IN ('array', 'hash-table')
ON CONFLICT DO NOTHING;

-- Test Cases for Problem 1
INSERT INTO problem_test_cases (problem_id, input_data, expected_output, is_sample, explanation, order_index) VALUES
(1, '4\n2 7 11 15\n9', '0 1', TRUE, 'nums[0] + nums[1] = 2 + 7 = 9', 1),
(1, '3\n3 2 4\n6', '1 2', TRUE, 'nums[1] + nums[2] = 2 + 4 = 6', 2),
(1, '2\n3 3\n6', '0 1', FALSE, NULL, 3);

-- Seed Problem 2: Reverse String
INSERT INTO problems (id, slug, title, description, difficulty, time_limit_ms, memory_limit_mb, is_published) VALUES
(
    2,
    'reverse-string',
    'Reverse String',
    'Write a function that reverses a string.\n\n### Example 1:\n```\nInput: s = "hello"\nOutput: "olleh"\n```',
    'EASY',
    1000,
    256,
    TRUE
) ON CONFLICT (slug) DO NOTHING;

-- Map Tags for Problem 2
INSERT INTO problem_tags (problem_id, tag_id)
SELECT 2, id FROM tags WHERE slug IN ('two-pointers', 'string')
ON CONFLICT DO NOTHING;

-- Test Cases for Problem 2
INSERT INTO problem_test_cases (problem_id, input_data, expected_output, is_sample, explanation, order_index) VALUES
(2, 'hello', 'olleh', TRUE, 'Reversing "hello" produces "olleh"', 1),
(2, 'Hannah', 'hannaH', TRUE, 'Reversing "Hannah" produces "hannaH"', 2),
(2, 'CodeForge', 'egroFedoC', FALSE, NULL, 3);
