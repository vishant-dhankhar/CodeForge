import { redis } from '../../config/redis.js';
import { VerdictEvaluator } from './verdictEvaluator.js';

export class SubmissionConsumer {
  static isRunning = false;

  static async startWorker() {
    if (this.isRunning) return;
    this.isRunning = true;
    console.log('🚀 Submission evaluation worker started, listening on queue: submissions:queue');

    while (this.isRunning) {
      try {
        if (redis.status !== 'ready') {
          await new Promise((resolve) => setTimeout(resolve, 2000));
          continue;
        }

        // BRPOP blocks up to 2 seconds waiting for new submission task
        const result = await redis.brpop('submissions:queue', 2);
        if (result && result[1]) {
          const payload = result[1];
          try {
            const task = JSON.parse(payload);
            const submissionId = task.submissionId || task.id;
            if (submissionId) {
              console.log(`📥 Processing submission #${submissionId} from queue`);
              await VerdictEvaluator.evaluateSubmission(BigInt(submissionId));
            }
          } catch (parseErr) {
            console.error('Failed to parse submission queue payload:', payload, parseErr);
          }
        }
      } catch (err) {
        if (this.isRunning) {
          console.warn('⚠️ Worker loop warning:', err.message || err);
          await new Promise((resolve) => setTimeout(resolve, 2000));
        }
      }
    }
  }

  static stopWorker() {
    this.isRunning = false;
  }
}
