import bcrypt from 'bcryptjs';
import jwt from 'jsonwebtoken';
import { prisma } from '../../config/prisma.js';
import { env } from '../../config/env.js';
import { ApiError } from '../../common/errors/apiError.js';

export class AuthService {
  static async register(data) {
    const existingUsername = await prisma.user.findUnique({
      where: { username: data.username },
    });
    if (existingUsername) {
      throw ApiError.badRequest('Username is already taken');
    }

    const existingEmail = await prisma.user.findUnique({
      where: { email: data.email },
    });
    if (existingEmail) {
      throw ApiError.badRequest('Email is already registered');
    }

    const passwordHash = await bcrypt.hash(data.password, 10);

    const user = await prisma.user.create({
      data: {
        username: data.username,
        email: data.email,
        passwordHash,
        stats: {
          create: {
            easySolved: 0,
            mediumSolved: 0,
            hardSolved: 0,
            totalSubmissions: 0,
            acceptedSubmissions: 0,
          },
        },
      },
    });

    const tokenPayload = {
      id: Number(user.id),
      username: user.username,
      email: user.email,
      role: user.role,
    };

    const token = jwt.sign(tokenPayload, env.JWT_SECRET, {
      expiresIn: `${env.JWT_EXPIRATION_MS}ms`,
    });

    return {
      token,
      type: 'Bearer',
      userId: Number(user.id),
      username: user.username,
      email: user.email,
      role: user.role,
    };
  }

  static async login(data) {
    const user = await prisma.user.findFirst({
      where: {
        OR: [{ username: data.usernameOrEmail }, { email: data.usernameOrEmail }],
      },
    });

    if (!user) {
      throw ApiError.unauthorized('Invalid username/email or password');
    }

    const isPasswordValid = await bcrypt.compare(data.password, user.passwordHash);
    if (!isPasswordValid) {
      throw ApiError.unauthorized('Invalid username/email or password');
    }

    const tokenPayload = {
      id: Number(user.id),
      username: user.username,
      email: user.email,
      role: user.role,
    };

    const token = jwt.sign(tokenPayload, env.JWT_SECRET, {
      expiresIn: `${env.JWT_EXPIRATION_MS}ms`,
    });

    return {
      token,
      type: 'Bearer',
      userId: Number(user.id),
      username: user.username,
      email: user.email,
      role: user.role,
    };
  }
}
