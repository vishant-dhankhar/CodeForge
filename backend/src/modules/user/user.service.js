import { prisma } from '../../config/prisma.js';
import { ApiError } from '../../common/errors/apiError.js';

export class UserService {
  static async getUserProfile(username) {
    const user = await prisma.user.findUnique({
      where: { username },
      include: { stats: true },
    });

    if (!user) {
      throw ApiError.notFound(`User with username '${username}' not found`);
    }

    const stats = user.stats || {
      easySolved: 0,
      mediumSolved: 0,
      hardSolved: 0,
      totalSubmissions: 0,
      acceptedSubmissions: 0,
    };

    const totalSolved = stats.easySolved + stats.mediumSolved + stats.hardSolved;
    const acceptanceRate =
      stats.totalSubmissions > 0
        ? parseFloat(((stats.acceptedSubmissions / stats.totalSubmissions) * 100).toFixed(2))
        : 0.0;

    return {
      id: Number(user.id),
      username: user.username,
      email: user.email,
      role: user.role,
      joinedAt: user.createdAt.toISOString(),
      stats: {
        easySolved: stats.easySolved,
        mediumSolved: stats.mediumSolved,
        hardSolved: stats.hardSolved,
        totalSolved,
        totalSubmissions: stats.totalSubmissions,
        acceptedSubmissions: stats.acceptedSubmissions,
        acceptanceRate,
      },
    };
  }
}
