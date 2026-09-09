import { AuthService } from './auth.service.js';
import { registerSchema, loginSchema } from './auth.schema.js';
import { sendApiResponse } from '../../common/utils/apiHelpers.js';

export class AuthController {
  static async register(req, res, next) {
    try {
      const validated = registerSchema.parse(req.body);
      const result = await AuthService.register(validated);
      return sendApiResponse(res, result, 'User registered successfully', 201);
    } catch (err) {
      next(err);
    }
  }

  static async login(req, res, next) {
    try {
      const validated = loginSchema.parse(req.body);
      const result = await AuthService.login(validated);
      return sendApiResponse(res, result, 'User logged in successfully', 200);
    } catch (err) {
      next(err);
    }
  }
}
