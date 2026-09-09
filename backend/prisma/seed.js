import { PrismaClient, Difficulty } from '@prisma/client';

const prisma = new PrismaClient();

async function main() {
  console.log('🌱 Seeding database with LeetCode-style starter templates...');

  // 1. Seed Tags
  const tagsData = [
    { name: 'Array', slug: 'array' },
    { name: 'Hash Table', slug: 'hash-table' },
    { name: 'Two Pointers', slug: 'two-pointers' },
    { name: 'String', slug: 'string' },
  ];

  const tagMap = new Map();
  for (const tag of tagsData) {
    const createdTag = await prisma.tag.upsert({
      where: { slug: tag.slug },
      update: {},
      create: tag,
    });
    tagMap.set(tag.slug, createdTag.id);
  }

  // 2. Seed Problem 1: Two Sum (LeetCode function signature style)
  const twoSumStarterCode = {
    JAVA: `class Solution {\n    public int[] twoSum(int[] nums, int target) {\n        // Write your solution here\n        return new int[]{0, 1};\n    }\n}`,
    CPP: `class Solution {\npublic:\n    vector<int> twoSum(vector<int>& nums, int target) {\n        // Write your solution here\n        return {0, 1};\n    }\n};`,
  };

  const twoSum = await prisma.problem.upsert({
    where: { slug: 'two-sum' },
    update: {
      starterCodeJson: twoSumStarterCode,
    },
    create: {
      slug: 'two-sum',
      title: 'Two Sum',
      description:
        'Given an array of integers `nums` and an integer `target`, return indices of the two numbers such that they add up to `target`.\n\nYou may assume that each input would have exactly one solution, and you may not use the same element twice.\n\n### Function Signature:\nImplement the `twoSum` function inside the `Solution` class.\n\n### Example 1:\n```\nInput: nums = [2,7,11,15], target = 9\nOutput: [0,1]\nExplanation: Because nums[0] + nums[1] == 9, we return [0, 1].\n```',
      difficulty: Difficulty.EASY,
      timeLimitMs: 1000,
      memoryLimitMb: 256,
      starterCodeJson: twoSumStarterCode,
      isPublished: true,
      testCases: {
        create: [
          {
            inputData: '4\n2 7 11 15\n9',
            expectedOutput: '0 1',
            isSample: true,
            explanation: 'nums[0] + nums[1] = 2 + 7 = 9',
            orderIndex: 1,
          },
          {
            inputData: '3\n3 2 4\n6',
            expectedOutput: '1 2',
            isSample: true,
            explanation: 'nums[1] + nums[2] = 2 + 4 = 6',
            orderIndex: 2,
          },
          {
            inputData: '2\n3 3\n6',
            expectedOutput: '0 1',
            isSample: false,
            explanation: null,
            orderIndex: 3,
          },
        ],
      },
    },
  });

  const arrayTagId = tagMap.get('array');
  const hashTableTagId = tagMap.get('hash-table');
  if (arrayTagId) {
    await prisma.problemTag.upsert({
      where: { problemId_tagId: { problemId: twoSum.id, tagId: arrayTagId } },
      update: {},
      create: { problemId: twoSum.id, tagId: arrayTagId },
    });
  }
  if (hashTableTagId) {
    await prisma.problemTag.upsert({
      where: { problemId_tagId: { problemId: twoSum.id, tagId: hashTableTagId } },
      update: {},
      create: { problemId: twoSum.id, tagId: hashTableTagId },
    });
  }

  // 3. Seed Problem 2: Reverse String (LeetCode function signature style)
  const reverseStringStarterCode = {
    JAVA: `class Solution {\n    public String reverseString(String s) {\n        // Write your solution here\n        return new StringBuilder(s).reverse().toString();\n    }\n}`,
    CPP: `class Solution {\npublic:\n    string reverseString(string s) {\n        // Write your solution here\n        reverse(s.begin(), s.end());\n        return s;\n    }\n};`,
  };

  const reverseString = await prisma.problem.upsert({
    where: { slug: 'reverse-string' },
    update: {
      starterCodeJson: reverseStringStarterCode,
    },
    create: {
      slug: 'reverse-string',
      title: 'Reverse String',
      description:
        'Write a function that reverses a string.\n\n### Function Signature:\nImplement the `reverseString` function inside the `Solution` class.\n\n### Example 1:\n```\nInput: s = "hello"\nOutput: "olleh"\n```',
      difficulty: Difficulty.EASY,
      timeLimitMs: 1000,
      memoryLimitMb: 256,
      starterCodeJson: reverseStringStarterCode,
      isPublished: true,
      testCases: {
        create: [
          {
            inputData: 'hello',
            expectedOutput: 'olleh',
            isSample: true,
            explanation: 'Reversing "hello" produces "olleh"',
            orderIndex: 1,
          },
          {
            inputData: 'Hannah',
            expectedOutput: 'hannaH',
            isSample: true,
            explanation: 'Reversing "Hannah" produces "hannaH"',
            orderIndex: 2,
          },
          {
            inputData: 'CodeForge',
            expectedOutput: 'egroFedoC',
            isSample: false,
            explanation: null,
            orderIndex: 3,
          },
        ],
      },
    },
  });

  const twoPointersTagId = tagMap.get('two-pointers');
  const stringTagId = tagMap.get('string');
  if (twoPointersTagId) {
    await prisma.problemTag.upsert({
      where: { problemId_tagId: { problemId: reverseString.id, tagId: twoPointersTagId } },
      update: {},
      create: { problemId: reverseString.id, tagId: twoPointersTagId },
    });
  }
  if (stringTagId) {
    await prisma.problemTag.upsert({
      where: { problemId_tagId: { problemId: reverseString.id, tagId: stringTagId } },
      update: {},
      create: { problemId: reverseString.id, tagId: stringTagId },
    });
  }

  console.log('✅ Seeding complete!');
}

main()
  .catch((e) => {
    console.error('❌ Error during seeding:', e);
    process.exit(1);
  })
  .finally(async () => {
    await prisma.$disconnect();
  });
