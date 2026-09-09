import { z } from 'zod';

export const createSubmissionSchema = z.object({
  problemId: z.number().optional(),
  problemSlug: z.string().optional(),
  language: z.enum(['JAVA', 'CPP']),
  sourceCode: z.string().min(1, 'Source code cannot be empty'),
});
