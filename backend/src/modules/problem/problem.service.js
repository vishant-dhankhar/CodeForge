import { Difficulty } from '@prisma/client';
import { prisma } from '../../config/prisma.js';
import { ApiError } from '../../common/errors/apiError.js';
import { createPagedResponse } from '../../common/utils/apiHelpers.js';

export class ProblemService {
  static async getProblems(difficulty, search, page = 0, size = 10) {
    const limit = Math.min(Math.max(size, 1), 50);
    const skip = page * limit;

    const where = {
      isPublished: true,
    };

    if (difficulty) {
      const upperDifficulty = difficulty.toUpperCase();
      if (Object.values(Difficulty).includes(upperDifficulty)) {
        where.difficulty = upperDifficulty;
      }
    }

    if (search && search.trim().length > 0) {
      const query = search.trim();
      where.OR = [
        { title: { contains: query, mode: 'insensitive' } },
        { description: { contains: query, mode: 'insensitive' } },
      ];
    }

    const [totalElements, problems] = await Promise.all([
      prisma.problem.count({ where }),
      prisma.problem.findMany({
        where,
        skip,
        take: limit,
        orderBy: { id: 'asc' },
        include: {
          tags: {
            include: { tag: true },
          },
        },
      }),
    ]);

    const content = problems.map((p) => ({
      id: Number(p.id),
      slug: p.slug,
      title: p.title,
      difficulty: p.difficulty,
      timeLimitMs: p.timeLimitMs,
      memoryLimitMb: p.memoryLimitMb,
      tags: p.tags.map((pt) => pt.tag.name),
    }));

    return createPagedResponse(content, page, limit, totalElements);
  }

  static async getProblemBySlug(slug) {
    const problem = await prisma.problem.findUnique({
      where: { slug },
      include: {
        tags: {
          include: { tag: true },
        },
        testCases: {
          where: { isSample: true },
          orderBy: { orderIndex: 'asc' },
        },
      },
    });

    if (!problem || !problem.isPublished) {
      throw ApiError.notFound(`Problem with slug '${slug}' not found`);
    }

    return {
      id: Number(problem.id),
      slug: problem.slug,
      title: problem.title,
      description: problem.description,
      difficulty: problem.difficulty,
      timeLimitMs: problem.timeLimitMs,
      memoryLimitMb: problem.memoryLimitMb,
      starterCodeJson: problem.starterCodeJson || null,
      tags: problem.tags.map((pt) => pt.tag.name),
      sampleTestCases: problem.testCases.map((tc) => ({
        id: Number(tc.id),
        inputData: tc.inputData,
        expectedOutput: tc.expectedOutput,
        explanation: tc.explanation || null,
        orderIndex: tc.orderIndex,
      })),
    };
  }
}
