import { Router } from 'express';
import { ProblemController } from './problem.controller.js';

const router = Router();

router.get('/', ProblemController.getProblems);
router.get('/:slug', ProblemController.getProblemBySlug);

export default router;
