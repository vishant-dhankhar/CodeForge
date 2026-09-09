import { AiService } from './ai.service.js';
import { aiAssistSchema } from './ai.schema.js';
import { sendApiResponse } from '../../common/utils/apiHelpers.js';

export class AiController {
  static async getGuidance(req, res, next) {
    try {
      const validated = aiAssistSchema.parse(req.body);
      const result = await AiService.getGuidance(validated);
      return sendApiResponse(res, result, 'AI guidance generated successfully');
    } catch (err) {
      next(err);
    }
  }
}
