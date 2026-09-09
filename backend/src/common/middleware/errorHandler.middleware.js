import { ApiError } from '../errors/apiError.js';
import { ZodError } from 'zod';

export const globalErrorHandler = (err, _req, res, _next) => {
  if (err instanceof ApiError) {
    return res.status(err.statusCode).json({
      status: err.statusCode,
      message: err.message,
      details: err.details || null,
      timestamp: new Date().toISOString(),
    });
  }

  if (err instanceof ZodError) {
    return res.status(400).json({
      status: 400,
      message: 'Validation failed',
      errors: err.errors.map((e) => ({ field: e.path.join('.'), message: e.message })),
      timestamp: new Date().toISOString(),
    });
  }

  console.error('💥 Unhandled Internal Error:', err);
  return res.status(500).json({
    status: 500,
    message: err.message || 'Internal server error',
    timestamp: new Date().toISOString(),
  });
};
