export class ApiError extends Error {
  constructor(statusCode, message, details = null) {
    super(message);
    this.statusCode = statusCode;
    this.details = details;
  }

  static badRequest(message, details = null) {
    return new ApiError(400, message, details);
  }

  static unauthorized(message = 'Unauthorized access') {
    return new ApiError(401, message);
  }

  static forbidden(message = 'Forbidden access') {
    return new ApiError(403, message);
  }

  static notFound(message = 'Resource not found') {
    return new ApiError(404, message);
  }

  static conflict(message) {
    return new ApiError(409, message);
  }

  static tooManyRequests(message = 'Too many requests, please slow down') {
    return new ApiError(429, message);
  }

  static internal(message = 'Internal server error') {
    return new ApiError(500, message);
  }
}
