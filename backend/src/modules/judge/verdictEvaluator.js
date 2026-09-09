import { SubmissionStatus, Verdict, Difficulty } from '@prisma/client';
import { prisma } from '../../config/prisma.js';
import { LocalCodeExecutor } from './localCodeExecutor.js';

export class VerdictEvaluator {
  static async evaluateSubmission(submissionId) {
    try {
      // 1. Mark as RUNNING
      await prisma.submission.update({
        where: { id: submissionId },
        data: { status: SubmissionStatus.RUNNING },
      });

      const submission = await prisma.submission.findUnique({
        where: { id: submissionId },
        include: {
          problem: {
            include: {
              testCases: {
                orderBy: { orderIndex: 'asc' },
              },
            },
          },
        },
      });

      if (!submission || !submission.problem) {
        return;
      }

      const { problem } = submission;
      const testCases = problem.testCases;

      if (testCases.length === 0) {
        await prisma.submission.update({
          where: { id: submissionId },
          data: {
            status: SubmissionStatus.COMPLETED,
            verdict: Verdict.ACCEPTED,
            executionTimeMs: 0,
            memoryUsedKb: 0,
          },
        });
        return;
      }

      let finalVerdict = Verdict.ACCEPTED;
      let maxExecutionTimeMs = 0;
      let maxMemoryUsedKb = 0;
      let errorOutput = null;

      for (const testCase of testCases) {
        const result = await LocalCodeExecutor.execute(
          submission.language,
          submission.sourceCode,
          testCase.inputData,
          problem.timeLimitMs,
          problem.memoryLimitMb
        );

        maxExecutionTimeMs = Math.max(maxExecutionTimeMs, result.executionTimeMs);
        maxMemoryUsedKb = Math.max(maxMemoryUsedKb, result.memoryUsedKb);

        if (result.isCompileError) {
          finalVerdict = Verdict.COMPILATION_ERROR;
          errorOutput = result.stderr || 'Compilation failed';
          break;
        }

        if (result.isTimeout) {
          finalVerdict = Verdict.TIME_LIMIT_EXCEEDED;
          errorOutput = result.stderr || `Time limit exceeded (${problem.timeLimitMs}ms)`;
          break;
        }

        if (result.exitCode !== 0) {
          finalVerdict = Verdict.RUNTIME_ERROR;
          errorOutput = result.stderr || `Process exited with code ${result.exitCode}`;
          break;
        }

        const normalizedOutput = this.normalizeOutput(result.stdout);
        const normalizedExpected = this.normalizeOutput(testCase.expectedOutput);

        if (normalizedOutput !== normalizedExpected) {
          finalVerdict = Verdict.WRONG_ANSWER;
          errorOutput = `Test case #${testCase.orderIndex} failed.\nExpected:\n${testCase.expectedOutput}\nActual:\n${result.stdout}`;
          break;
        }
      }

      // 2. Update Submission Result
      await prisma.submission.update({
        where: { id: submissionId },
        data: {
          status: SubmissionStatus.COMPLETED,
          verdict: finalVerdict,
          executionTimeMs: maxExecutionTimeMs,
          memoryUsedKb: maxMemoryUsedKb,
          errorOutput: errorOutput,
        },
      });

      // 3. Update User Statistics
      await this.updateUserStats(submission.userId, submission.problemId, problem.difficulty, finalVerdict);
    } catch (err) {
      console.error(`💥 Error evaluating submission #${submissionId}:`, err);
      await prisma.submission.update({
        where: { id: submissionId },
        data: {
          status: SubmissionStatus.FAILED,
          verdict: Verdict.INTERNAL_ERROR,
          errorOutput: `Internal Judge Error: ${err.message || err}`,
        },
      });
    }
  }

  static normalizeOutput(str) {
    if (!str) return '';
    return str
      .replace(/\r\n/g, '\n')
      .split('\n')
      .map((line) => line.trimEnd())
      .join('\n')
      .trim();
  }

  static async updateUserStats(userId, problemId, difficulty, verdict) {
    try {
      const stats = await prisma.userStats.findUnique({ where: { userId } });
      if (!stats) return;

      const isAccepted = verdict === Verdict.ACCEPTED;

      // Check if user previously solved this problem
      const previousAccepted = await prisma.submission.findFirst({
        where: {
          userId,
          problemId,
          verdict: Verdict.ACCEPTED,
        },
      });

      const isFirstTimeAccept = isAccepted && !previousAccepted;

      let easyInc = 0;
      let mediumInc = 0;
      let hardInc = 0;

      if (isFirstTimeAccept) {
        if (difficulty === Difficulty.EASY) easyInc = 1;
        else if (difficulty === Difficulty.MEDIUM) mediumInc = 1;
        else if (difficulty === Difficulty.HARD) hardInc = 1;
      }

      await prisma.userStats.update({
        where: { userId },
        data: {
          totalSubmissions: { increment: 1 },
          acceptedSubmissions: isAccepted ? { increment: 1 } : undefined,
          easySolved: easyInc > 0 ? { increment: easyInc } : undefined,
          mediumSolved: mediumInc > 0 ? { increment: mediumInc } : undefined,
          hardSolved: hardInc > 0 ? { increment: hardInc } : undefined,
        },
      });
    } catch (err) {
      console.warn('Failed to update user stats:', err);
    }
  }
}
