import { z } from 'zod';

export const aiAssistSchema = z.object({
  problemSlug: z.string().min(1),
  currentCode: z.string().default(''),
  language: z.enum(['JAVA', 'CPP']).default('JAVA'),
  verdict: z.string().optional().nullable(),
  errorOutput: z.string().optional().nullable(),
  promptType: z.enum(['HINT', 'DEBUG', 'OPTIMIZE', 'CUSTOM']).default('HINT'),
  userQuestion: z.string().optional().nullable(),
});
