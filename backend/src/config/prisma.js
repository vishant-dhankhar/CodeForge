import { PrismaClient } from '@prisma/client';

// Patch BigInt serialization for JSON.stringify / Express res.json
BigInt.prototype.toJSON = function () {
  return Number(this);
};

export const prisma = new PrismaClient({
  log: process.env.NODE_ENV === 'development' ? ['error', 'warn'] : ['error'],
});
