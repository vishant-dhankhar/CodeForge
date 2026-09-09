import { Router } from 'express';
import { SubmissionController } from './submission.controller.js';
import { authenticateJwt } from '../../common/middleware/auth.middleware.js';
import { createSlidingWindowRateLimiter } from '../../common/middleware/rateLimiter.middleware.js';

const router = Router();

router.post(
  '/',
  authenticateJwt,
  createSlidingWindowRateLimiter({ maxRequests: 10 }),
  SubmissionController.createSubmission
);

router.get('/my', authenticateJwt, SubmissionController.getMySubmissions);
router.get('/problem/:slug', SubmissionController.getSubmissionsByProblem);
router.get('/:id', SubmissionController.getSubmissionById);

export default router;
