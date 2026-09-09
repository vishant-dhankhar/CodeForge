import { redis } from '../../config/redis.js';
import { ApiError } from '../errors/apiError.js';

export const createSlidingWindowRateLimiter = (options = {}) => {
  const windowMs = options.windowMs || 60000; // 1 minute
  const maxRequests = options.maxRequests || 10;

  return async (req, res, next) => {
    try {
      const identifier = req.user ? `user:${req.user.id}` : `ip:${req.ip || 'unknown'}`;
      const key = `ratelimit:${req.path}:${identifier}`;
      const now = Date.now();
      const windowStart = now - windowMs;

      if (redis.status !== 'ready') {
        // Fallback: If Redis is unavailable, skip rate limit check
        return next();
      }

      const pipeline = redis.pipeline();
      pipeline.zremrangebyscore(key, 0, windowStart);
      pipeline.zcard(key);
      pipeline.zadd(key, now, `${now}-${Math.random()}`);
      pipeline.expire(key, Math.ceil(windowMs / 1000));

      const results = await pipeline.exec();
      if (results && results[1]) {
        const requestCount = results[1][1];
        res.setHeader('X-RateLimit-Limit', maxRequests);
        res.setHeader('X-RateLimit-Remaining', Math.max(0, maxRequests - requestCount));

        if (requestCount >= maxRequests) {
          return next(ApiError.tooManyRequests('Submission rate limit exceeded. Please wait a moment before submitting again.'));
        }
      }

      next();
    } catch (err) {
      // Pass-through on rate-limiting infrastructure error
      next();
    }
  };
};
