export function sendApiResponse(res, data, message, statusCode = 200) {
  return res.status(statusCode).json({
    success: true,
    message: message || undefined,
    data,
    timestamp: new Date().toISOString(),
  });
}

export function createPagedResponse(content, page, size, totalElements) {
  const totalPages = Math.ceil(totalElements / size) || (totalElements === 0 ? 0 : 1);
  return {
    content,
    page,
    size,
    totalElements,
    totalPages,
    isFirst: page === 0,
    isLast: page >= totalPages - 1 || totalPages === 0,
  };
}
