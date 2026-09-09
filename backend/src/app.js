import express from 'express';
import cors from 'cors';
import authRoutes from './modules/auth/auth.routes.js';
import userRoutes from './modules/user/user.routes.js';
import problemRoutes from './modules/problem/problem.routes.js';
import submissionRoutes from './modules/submission/submission.routes.js';
import aiRoutes from './modules/ai/ai.routes.js';
import { globalErrorHandler } from './common/middleware/errorHandler.middleware.js';

const app = express();

app.use(cors({ origin: true, credentials: true }));
app.use(express.json());
app.use(express.urlencoded({ extended: true }));

// Health check endpoint
app.get('/api/v1/health', (_req, res) => {
  res.status(200).json({
    status: 'UP',
    service: 'CodeForge Node Backend (JavaScript)',
    timestamp: new Date().toISOString(),
  });
});

// API Routes
app.use('/api/v1/auth', authRoutes);
app.use('/api/v1/users', userRoutes);
app.use('/api/v1/problems', problemRoutes);
app.use('/api/v1/submissions', submissionRoutes);
app.use('/api/v1/ai', aiRoutes);

// Global Error Handler
app.use(globalErrorHandler);

export default app;
