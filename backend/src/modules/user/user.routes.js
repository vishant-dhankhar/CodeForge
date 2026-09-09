import { Router } from 'express';
import { UserController } from './user.controller.js';
import { authenticateJwt } from '../../common/middleware/auth.middleware.js';

const router = Router();

router.get('/me', authenticateJwt, UserController.getCurrentUser);
router.get('/:username', UserController.getUserProfile);

export default router;
