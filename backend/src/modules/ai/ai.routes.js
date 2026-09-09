import { Router } from 'express';
import { AiController } from './ai.controller.js';
import { optionalAuthenticateJwt } from '../../common/middleware/auth.middleware.js';
import { createSlidingWindowRateLimiter } from '../../common/middleware/rateLimiter.middleware.js';

const router = Router();

router.post(
  '/assist',
  optionalAuthenticateJwt,
  createSlidingWindowRateLimiter({ maxRequests: 10, windowMs: 60000 }),
  AiController.getGuidance
);

export default router;
