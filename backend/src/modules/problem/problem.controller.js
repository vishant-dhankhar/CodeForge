import { ProblemService } from './problem.service.js';
import { sendApiResponse } from '../../common/utils/apiHelpers.js';

export class ProblemController {
  static async getProblems(req, res, next) {
    try {
      const difficulty = req.query.difficulty;
      const search = req.query.search;
      const page = parseInt(req.query.page || '0', 10);
      const size = parseInt(req.query.size || '10', 10);

      const result = await ProblemService.getProblems(difficulty, search, page, size);
      return sendApiResponse(res, result, 'Problems fetched successfully');
    } catch (err) {
      next(err);
    }
  }

  static async getProblemBySlug(req, res, next) {
    try {
      const { slug } = req.params;
      const result = await ProblemService.getProblemBySlug(slug);
      return sendApiResponse(res, result, 'Problem fetched successfully');
    } catch (err) {
      next(err);
    }
  }
}
