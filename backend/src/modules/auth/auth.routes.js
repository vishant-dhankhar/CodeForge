import { Router } from 'express';
import { AuthController } from './auth.controller.js';
import { createSlidingWindowRateLimiter } from '../../common/middleware/rateLimiter.middleware.js';

const router = Router();

router.post('/register', createSlidingWindowRateLimiter({ maxRequests: 5 }), AuthController.register);
router.post('/login', createSlidingWindowRateLimiter({ maxRequests: 10 }), AuthController.login);

export default router;
