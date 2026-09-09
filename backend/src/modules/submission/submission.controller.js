import { SubmissionService } from './submission.service.js';
import { createSubmissionSchema } from './submission.schema.js';
import { sendApiResponse } from '../../common/utils/apiHelpers.js';
import { ApiError } from '../../common/errors/apiError.js';

export class SubmissionController {
  static async createSubmission(req, res, next) {
    try {
      if (!req.user) {
        throw ApiError.unauthorized('Authentication required');
      }
      const validated = createSubmissionSchema.parse(req.body);
      const result = await SubmissionService.createSubmission(BigInt(req.user.id), validated);
      return sendApiResponse(res, result, 'Submission queued for evaluation', 202);
    } catch (err) {
      next(err);
    }
  }

  static async getSubmissionById(req, res, next) {
    try {
      const id = BigInt(req.params.id);
      const result = await SubmissionService.getSubmissionById(id);
      return sendApiResponse(res, result, 'Submission status fetched successfully');
    } catch (err) {
      next(err);
    }
  }

  static async getMySubmissions(req, res, next) {
    try {
      if (!req.user) {
        throw ApiError.unauthorized('Authentication required');
      }
      const problemId = req.query.problemId ? BigInt(req.query.problemId) : null;
      const page = parseInt(req.query.page || '0', 10);
      const size = parseInt(req.query.size || '10', 10);

      const result = await SubmissionService.getMySubmissions(
        BigInt(req.user.id),
        problemId,
        page,
        size
      );
      return sendApiResponse(res, result, 'User submissions fetched successfully');
    } catch (err) {
      next(err);
    }
  }

  static async getSubmissionsByProblem(req, res, next) {
    try {
      const { slug } = req.params;
      const page = parseInt(req.query.page || '0', 10);
      const size = parseInt(req.query.size || '10', 10);

      const result = await SubmissionService.getSubmissionsByProblemSlug(slug, page, size);
      return sendApiResponse(res, result, 'Problem submissions fetched successfully');
    } catch (err) {
      next(err);
    }
  }
}
