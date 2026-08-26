import { mockService } from './mockData';

const API_BASE = import.meta.env.VITE_API_BASE_URL
  ? `${import.meta.env.VITE_API_BASE_URL.replace(/\/$/, '')}/api/v1`
  : '/api/v1';

async function request(endpoint, options = {}, mockFallbackFn) {
  const token = localStorage.getItem('codeforge_token');
  const headers = {
    'Content-Type': 'application/json',
    ...(token ? { Authorization: `Bearer ${token}` } : {}),
    ...options.headers,
  };

  try {
    const response = await fetch(`${API_BASE}${endpoint}`, {
      ...options,
      headers,
    });

    const contentType = response.headers.get('content-type');
    let data;

    if (contentType && contentType.includes('application/json')) {
      data = await response.json();
    } else {
      // Server returned HTML or plain text (e.g. 404/500 static page from Vercel)
      const text = await response.text();
      if (!response.ok) {
        // Fallback to mock data if provided
        if (mockFallbackFn) {
          console.warn(`[CodeForge] API unreachable (${response.status}). Using mock fallback data.`);
          return mockFallbackFn();
        }
        throw new Error(`API server returned non-JSON response (${response.status})`);
      }
      try {
        data = JSON.parse(text);
      } catch {
        if (mockFallbackFn) return mockFallbackFn();
        throw new Error('Invalid JSON response from server');
      }
    }

    if (!response.ok) {
      const errorMessage = data?.error?.message || data?.message || 'Request failed';
      throw new Error(errorMessage);
    }

    return data;
  } catch (err) {
    if (mockFallbackFn) {
      console.warn('[CodeForge] Network error accessing backend. Using demo mock fallback data.', err.message);
      return mockFallbackFn();
    }
    throw err;
  }
}

export const authApi = {
  login: (credentials) =>
    request(
      '/auth/login',
      {
        method: 'POST',
        body: JSON.stringify(credentials),
      },
      () => ({
        success: true,
        data: {
          token: 'mock-jwt-token-demo',
          username: credentials.username || 'demo_user',
          email: `${credentials.username || 'user'}@codeforge.io`,
        },
      })
    ),
  register: (userData) =>
    request(
      '/auth/register',
      {
        method: 'POST',
        body: JSON.stringify(userData),
      },
      () => ({
        success: true,
        data: {
          token: 'mock-jwt-token-demo',
          username: userData.username,
          email: userData.email,
        },
      })
    ),
};

export const userApi = {
  getMe: () =>
    request('/users/me', {}, () => ({
      success: true,
      data: {
        username: 'DemoUser',
        email: 'demo@codeforge.io',
        problemsSolved: 14,
        rank: 128,
        rating: 1650,
      },
    })),
  getProfile: (username) =>
    request(`/users/${username}`, {}, () => ({
      success: true,
      data: {
        username: username || 'DemoUser',
        email: 'demo@codeforge.io',
        problemsSolved: 14,
        rank: 128,
        rating: 1650,
      },
    })),
};

export const problemsApi = {
  getProblems: (params = {}) =>
    request('/problems?' + new URLSearchParams(params).toString(), {}, () =>
      mockService.getProblems(params)
    ),
  getProblemBySlug: (slug) =>
    request(`/problems/${slug}`, {}, () => mockService.getProblemBySlug(slug)),
};

export const submissionsApi = {
  createSubmission: (submissionData) =>
    request(
      '/submissions',
      {
        method: 'POST',
        body: JSON.stringify(submissionData),
      },
      () => mockService.createSubmission(submissionData)
    ),
  getSubmissionById: (id) =>
    request(`/submissions/${id}`, {}, () => mockService.getSubmissionById(id)),
  getMySubmissions: (params = {}) =>
    request('/submissions/my?' + new URLSearchParams(params).toString(), {}, () =>
      mockService.getProblemSubmissions()
    ),
  getProblemSubmissions: (slug, params = {}) =>
    request('/submissions/problem/' + slug + '?' + new URLSearchParams(params).toString(), {}, () =>
      mockService.getProblemSubmissions()
    ),
};
