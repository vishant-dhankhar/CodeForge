export const MOCK_PROBLEMS = [
  {
    id: 1,
    title: 'Two Sum',
    slug: 'two-sum',
    difficulty: 'EASY',
    tags: ['Array', 'Hash Table'],
    timeLimitMs: 1000,
    memoryLimitMb: 256,
    description: `Given an array of integers \`nums\` and an integer \`target\`, return indices of the two numbers such that they add up to \`target\`.

You may assume that each input would have **exactly one solution**, and you may not use the same element twice.

You can return the answer in any order.

### Example 1:
**Input:** nums = [2,7,11,15], target = 9
**Output:** [0,1]
**Explanation:** Because nums[0] + nums[1] == 9, we return [0, 1].

### Example 2:
**Input:** nums = [3,2,4], target = 6
**Output:** [1,2]

### Constraints:
- \`2 <= nums.length <= 10^4\`
- \`-10^9 <= nums[i] <= 10^9\`
- \`-10^9 <= target <= 10^9\`
- Only one valid answer exists.`,
    sampleTestCases: [
      {
        id: 101,
        inputData: '4\n2 7 11 15\n9',
        expectedOutput: '0 1',
        explanation: 'nums[0] + nums[1] = 2 + 7 = 9',
      },
      {
        id: 102,
        inputData: '3\n3 2 4\n6',
        expectedOutput: '1 2',
        explanation: 'nums[1] + nums[2] = 2 + 4 = 6',
      },
    ],
  },
  {
    id: 2,
    title: 'Valid Parentheses',
    slug: 'valid-parentheses',
    difficulty: 'EASY',
    tags: ['String', 'Stack'],
    timeLimitMs: 1000,
    memoryLimitMb: 256,
    description: `Given a string \`s\` containing just the characters '(', ')', '{', '}', '[' and ']', determine if the input string is valid.

An input string is valid if:
1. Open brackets must be closed by the same type of brackets.
2. Open brackets must be closed in the correct order.
3. Every close bracket has a corresponding open bracket of the same type.

### Example 1:
**Input:** s = "()"
**Output:** true

### Example 2:
**Input:** s = "()[]{}"
**Output:** true

### Example 3:
**Input:** s = "(]"
**Output:** false

### Constraints:
- \`1 <= s.length <= 10^4\`
- \`s\` consists of parentheses only \`()[]{}\`.`,
    sampleTestCases: [
      {
        id: 201,
        inputData: '()[]{}',
        expectedOutput: 'true',
        explanation: 'All open brackets are matched correctly in order.',
      },
      {
        id: 202,
        inputData: '(]',
        expectedOutput: 'false',
        explanation: 'Mismatched closing bracket ] for open bracket (.',
      },
    ],
  },
  {
    id: 3,
    title: 'Longest Substring Without Repeating Characters',
    slug: 'longest-substring-without-repeating-characters',
    difficulty: 'MEDIUM',
    tags: ['Hash Table', 'String', 'Sliding Window'],
    timeLimitMs: 2000,
    memoryLimitMb: 256,
    description: `Given a string \`s\`, find the length of the **longest substring** without repeating characters.

### Example 1:
**Input:** s = "abcabcbb"
**Output:** 3
**Explanation:** The answer is "abc", with the length of 3.

### Example 2:
**Input:** s = "bbbbb"
**Output:** 1
**Explanation:** The answer is "b", with the length of 1.

### Constraints:
- \`0 <= s.length <= 5 * 10^4\`
- \`s\` consists of English letters, digits, symbols and spaces.`,
    sampleTestCases: [
      {
        id: 301,
        inputData: 'abcabcbb',
        expectedOutput: '3',
        explanation: 'Longest non-repeating substring is "abc".',
      },
      {
        id: 302,
        inputData: 'bbbbb',
        expectedOutput: '1',
        explanation: 'Longest non-repeating substring is "b".',
      },
    ],
  },
  {
    id: 4,
    title: 'Median of Two Sorted Arrays',
    slug: 'median-of-two-sorted-arrays',
    difficulty: 'HARD',
    tags: ['Array', 'Binary Search', 'Divide and Conquer'],
    timeLimitMs: 2000,
    memoryLimitMb: 256,
    description: `Given two sorted arrays \`nums1\` and \`nums2\` of size \`m\` and \`n\` respectively, return the median of the two sorted arrays.

The overall run time complexity should be \`O(log (m+n))\`.

### Example 1:
**Input:** nums1 = [1,3], nums2 = [2]
**Output:** 2.00000
**Explanation:** merged array = [1,2,3] and median is 2.

### Constraints:
- \`nums1.length == m\`
- \`nums2.length == n\`
- \`0 <= m <= 1000\`
- \`0 <= n <= 1000\`
- \`-10^6 <= nums1[i], nums2[i] <= 10^6\``,
    sampleTestCases: [
      {
        id: 401,
        inputData: '2\n1 3\n1\n2',
        expectedOutput: '2.00000',
        explanation: 'Merged array: [1, 2, 3]',
      },
    ],
  },
  {
    id: 5,
    title: 'LRU Cache Design',
    slug: 'lru-cache-design',
    difficulty: 'HARD',
    tags: ['Hash Table', 'Linked List', 'Design'],
    timeLimitMs: 2000,
    memoryLimitMb: 256,
    description: `Design a data structure that follows the constraints of a **Least Recently Used (LRU) cache**.

Implement the \`LRUCache\` class:
- \`LRUCache(int capacity)\` Initialize the LRU cache with positive size capacity.
- \`int get(int key)\` Return the value of the key if the key exists, otherwise return \`-1\`.
- \`void put(int key, int value)\` Update the value of the key if key exists. Otherwise, add key-value pair. If keys exceed capacity, evict the least recently used key.

### Constraints:
- \`1 <= capacity <= 3000\`
- \`0 <= key <= 10^4\`
- \`0 <= value <= 10^5\`
- At most \`2 * 10^5\` calls will be made to \`get\` and \`put\`.`,
    sampleTestCases: [
      {
        id: 501,
        inputData: '2\nput 1 1\nput 2 2\nget 1\nput 3 3\nget 2',
        expectedOutput: '1 -1',
        explanation: 'Key 2 was evicted before put 3 3',
      },
    ],
  },
];

let mockSubmissionIdCounter = 100;
const mockSubmissionsStore = new Map();

export const mockService = {
  getProblems: ({ difficulty, search }) => {
    let filtered = [...MOCK_PROBLEMS];
    if (difficulty) {
      filtered = filtered.filter((p) => p.difficulty === difficulty);
    }
    if (search) {
      const q = search.toLowerCase();
      filtered = filtered.filter(
        (p) => p.title.toLowerCase().includes(q) || p.tags.some((t) => t.toLowerCase().includes(q))
      );
    }
    return {
      success: true,
      data: {
        content: filtered,
        totalPages: 1,
        totalElements: filtered.length,
      },
    };
  },

  getProblemBySlug: (slug) => {
    const found = MOCK_PROBLEMS.find((p) => p.slug === slug) || MOCK_PROBLEMS[0];
    return {
      success: true,
      data: found,
    };
  },

  createSubmission: ({ problemId, language, sourceCode }) => {
    mockSubmissionIdCounter += 1;
    const subId = mockSubmissionIdCounter;
    const sub = {
      id: subId,
      problemId,
      language,
      status: 'QUEUED',
      verdict: null,
      submittedAt: new Date().toISOString(),
      username: 'DemoUser',
    };
    mockSubmissionsStore.set(subId, sub);

    // Simulate async evaluation process in mock
    setTimeout(() => {
      sub.status = 'RUNNING';
    }, 400);

    setTimeout(() => {
      sub.status = 'COMPLETED';
      sub.verdict = 'ACCEPTED';
      sub.executionTimeMs = Math.floor(Math.random() * 25) + 10;
      sub.memoryUsedKb = 3400 + Math.floor(Math.random() * 500);
    }, 1200);

    return {
      success: true,
      data: sub,
    };
  },

  getSubmissionById: (id) => {
    const sub = mockSubmissionsStore.get(Number(id)) || {
      id: Number(id),
      status: 'COMPLETED',
      verdict: 'ACCEPTED',
      executionTimeMs: 18,
      memoryUsedKb: 3620,
    };
    return {
      success: true,
      data: sub,
    };
  },

  getProblemSubmissions: () => {
    return {
      success: true,
      data: {
        content: Array.from(mockSubmissionsStore.values()).reverse(),
      },
    };
  },
};
