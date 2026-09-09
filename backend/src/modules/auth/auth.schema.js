import { z } from 'zod';

export const registerSchema = z.object({
  username: z.string().min(3).max(50),
  email: z.string().email().max(255),
  password: z.string().min(6).max(100),
});

export const loginSchema = z.object({
  usernameOrEmail: z.string().min(1),
  password: z.string().min(1),
});
