import dotenv from 'dotenv';
import path from 'path';

dotenv.config({ path: path.resolve(process.cwd(), '.env') });

export const env = {
  PORT: parseInt(process.env.PORT || '8080', 10),
  NODE_ENV: process.env.NODE_ENV || 'development',
  DATABASE_URL: process.env.DATABASE_URL || 'postgresql://postgres:postgres@localhost:5432/codeforge_db?schema=public',
  REDIS_HOST: process.env.REDIS_HOST || 'localhost',
  REDIS_PORT: parseInt(process.env.REDIS_PORT || '6379', 10),
  REDIS_PASSWORD: process.env.REDIS_PASSWORD || undefined,
  JWT_SECRET: process.env.JWT_SECRET || 'codeforge_secret_key_change_in_production',
  JWT_EXPIRATION_MS: parseInt(process.env.JWT_EXPIRATION_MS || '86400000', 10),
};
