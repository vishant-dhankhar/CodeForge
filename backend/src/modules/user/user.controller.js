import { UserService } from './user.service.js';
import { sendApiResponse } from '../../common/utils/apiHelpers.js';
import { ApiError } from '../../common/errors/apiError.js';

export class UserController {
  static async getCurrentUser(req, res, next) {
    try {
      if (!req.user) {
        throw ApiError.unauthorized('User principal not found');
      }
      const result = await UserService.getUserProfile(req.user.username);
      return sendApiResponse(res, result, 'Current user profile fetched successfully');
    } catch (err) {
      next(err);
    }
  }

  static async getUserProfile(req, res, next) {
    try {
      const { username } = req.params;
      const result = await UserService.getUserProfile(username);
      return sendApiResponse(res, result, 'User profile fetched successfully');
    } catch (err) {
      next(err);
    }
  }
}
