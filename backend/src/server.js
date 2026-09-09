import app from './app.js';
import { env } from './config/env.js';
import { prisma } from './config/prisma.js';
import { redis } from './config/redis.js';
import { SubmissionConsumer } from './modules/judge/submissionConsumer.js';

async function bootstrap() {
  try {
    // 1. Connect Prisma Database
    await prisma.$connect();
    console.log('✅ Database connected via Prisma Client (JavaScript)');

    // 2. Connect Redis Client
    redis.connect().catch((err) => {
      console.warn('⚠️ Initial Redis connection warning:', err.message);
    });

    // 3. Start Express Server
    const server = app.listen(env.PORT, () => {
      console.log(`🚀 CodeForge Node.js (JavaScript) API Server running at http://localhost:${env.PORT}`);
      console.log(`⚙️  Environment: ${env.NODE_ENV}`);
    });

    // 4. Start Redis Code Execution Worker
    SubmissionConsumer.startWorker().catch((err) => {
      console.error('Failed to start submission consumer worker:', err);
    });

    // Graceful Shutdown Handlers
    const shutdown = async () => {
      console.log('\n🛑 Shutdown signal received. Closing services...');
      SubmissionConsumer.stopWorker();
      server.close(() => {
        console.log('Server HTTP connections closed.');
      });
      await prisma.$disconnect();
      redis.disconnect();
      process.exit(0);
    };

    process.on('SIGINT', shutdown);
    process.on('SIGTERM', shutdown);
  } catch (err) {
    console.error('💥 Fatal error during server bootstrap:', err);
    process.exit(1);
  }
}

bootstrap();
