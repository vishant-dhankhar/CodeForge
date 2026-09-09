import jwt from 'jsonwebtoken';
import { env } from '../../config/env.js';
import { ApiError } from '../errors/apiError.js';

export const authenticateJwt = (req, _res, next) => {
  const authHeader = req.headers.authorization;
  if (!authHeader || !authHeader.startsWith('Bearer ')) {
    return next(ApiError.unauthorized('Missing or invalid Authorization header'));
  }

  const token = authHeader.substring(7);
  try {
    const decoded = jwt.verify(token, env.JWT_SECRET);
    req.user = decoded;
    next();
  } catch (err) {
    return next(ApiError.unauthorized('Invalid or expired token'));
  }
};

export const optionalAuthenticateJwt = (req, _res, next) => {
  const authHeader = req.headers.authorization;
  if (authHeader && authHeader.startsWith('Bearer ')) {
    const token = authHeader.substring(7);
    try {
      const decoded = jwt.verify(token, env.JWT_SECRET);
      req.user = decoded;
    } catch {
      // Ignore token errors for optional auth
    }
  }
  next();
};
