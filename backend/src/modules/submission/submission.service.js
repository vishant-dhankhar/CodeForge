import { SubmissionStatus } from '@prisma/client';
import { prisma } from '../../config/prisma.js';
import { redis } from '../../config/redis.js';
import { ApiError } from '../../common/errors/apiError.js';
import { createPagedResponse } from '../../common/utils/apiHelpers.js';
import { VerdictEvaluator } from '../judge/verdictEvaluator.js';

export class SubmissionService {
  static async createSubmission(userId, input) {
    let problem;
    if (input.problemId) {
      problem = await prisma.problem.findUnique({ where: { id: BigInt(input.problemId) } });
    } else if (input.problemSlug) {
      problem = await prisma.problem.findUnique({ where: { slug: input.problemSlug } });
    }

    if (!problem || !problem.isPublished) {
      throw ApiError.notFound('Problem not found or not published');
    }

    const submission = await prisma.submission.create({
      data: {
        userId,
        problemId: problem.id,
        language: input.language,
        sourceCode: input.sourceCode,
        status: SubmissionStatus.QUEUED,
      },
      include: {
        user: true,
        problem: true,
      },
    });

    const taskPayload = JSON.stringify({
      submissionId: Number(submission.id),
      problemId: Number(problem.id),
      userId: Number(userId),
      language: input.language,
    });

    // Enqueue task to Redis queue
    if (redis.status === 'ready') {
      await redis.lpush('submissions:queue', taskPayload);
    } else {
      // Fallback: evaluate synchronously if Redis queue is unavailable
      setImmediate(() => {
        VerdictEvaluator.evaluateSubmission(submission.id);
      });
    }

    return this.mapToSubmissionResponse(submission);
  }

  static async getSubmissionById(id) {
    const submission = await prisma.submission.findUnique({
      where: { id },
      include: { user: true, problem: true },
    });

    if (!submission) {
      throw ApiError.notFound(`Submission #${id} not found`);
    }

    return this.mapToSubmissionResponse(submission);
  }

  static async getMySubmissions(userId, problemId = null, page = 0, size = 10) {
    const limit = Math.min(Math.max(size, 1), 50);
    const skip = page * limit;

    const where = { userId };
    if (problemId) {
      where.problemId = problemId;
    }

    const [totalElements, submissions] = await Promise.all([
      prisma.submission.count({ where }),
      prisma.submission.findMany({
        where,
        skip,
        take: limit,
        orderBy: { id: 'desc' },
        include: { user: true, problem: true },
      }),
    ]);

    const content = submissions.map((s) => this.mapToSubmissionResponse(s));
    return createPagedResponse(content, page, limit, totalElements);
  }

  static async getSubmissionsByProblemSlug(slug, page = 0, size = 10) {
    const problem = await prisma.problem.findUnique({ where: { slug } });
    if (!problem) {
      throw ApiError.notFound(`Problem '${slug}' not found`);
    }

    const limit = Math.min(Math.max(size, 1), 50);
    const skip = page * limit;
    const where = { problemId: problem.id };

    const [totalElements, submissions] = await Promise.all([
      prisma.submission.count({ where }),
      prisma.submission.findMany({
        where,
        skip,
        take: limit,
        orderBy: { id: 'desc' },
        include: { user: true, problem: true },
      }),
    ]);

    const content = submissions.map((s) => this.mapToSubmissionResponse(s));
    return createPagedResponse(content, page, limit, totalElements);
  }

  static mapToSubmissionResponse(s) {
    return {
      id: Number(s.id),
      problemId: Number(s.problemId),
      problemTitle: s.problem ? s.problem.title : '',
      problemSlug: s.problem ? s.problem.slug : '',
      userId: Number(s.userId),
      username: s.user ? s.user.username : '',
      language: s.language,
      sourceCode: s.sourceCode,
      status: s.status,
      verdict: s.verdict || null,
      executionTimeMs: s.executionTimeMs ?? null,
      memoryUsedKb: s.memoryUsedKb ?? null,
      errorOutput: s.errorOutput || null,
      submittedAt: s.createdAt.toISOString(),
      updatedAt: s.updatedAt.toISOString(),
    };
  }
}
