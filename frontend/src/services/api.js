const API_BASE = '/api/v1';

async function request(endpoint, options = {}) {
  const token = localStorage.getItem('codeforge_token');
  const headers = {
    'Content-Type': 'application/json',
    ...(token ? { Authorization: `Bearer ${token}` } : {}),
    ...options.headers,
  };

  const response = await fetch(`${API_BASE}${endpoint}`, {
    ...options,
    headers,
  });

  const data = await response.json();

  if (!response.ok) {
    const errorMessage = data?.error?.message || data?.message || 'Request failed';
    throw new Error(errorMessage);
  }

  return data;
}

export const authApi = {
  login: (credentials) =>
    request('/auth/login', {
      method: 'POST',
      body: JSON.stringify(credentials),
    }),
  register: (userData) =>
    request('/auth/register', {
      method: 'POST',
      body: JSON.stringify(userData),
    }),
};

export const userApi = {
  getMe: () => request('/users/me'),
  getProfile: (username) => request(`/users/${username}`),
};

export const problemsApi = {
  getProblems: ({ difficulty, search, page = 0, size = 10 } = {}) => {
    const params = new URLSearchParams();
    if (difficulty) params.append('difficulty', difficulty);
    if (search) params.append('search', search);
    params.append('page', page);
    params.append('size', size);
    return request(`/problems?${params.toString()}`);
  },
  getProblemBySlug: (slug) => request(`/problems/${slug}`),
};

export const submissionsApi = {
  createSubmission: (submissionData) =>
    request('/submissions', {
      method: 'POST',
      body: JSON.stringify(submissionData),
    }),
  getSubmissionById: (id) => request(`/submissions/${id}`),
  getMySubmissions: ({ problemId, page = 0, size = 10 } = {}) => {
    const params = new URLSearchParams();
    if (problemId) params.append('problemId', problemId);
    params.append('page', page);
    params.append('size', size);
    return request(`/submissions/my?${params.toString()}`);
  },
  getProblemSubmissions: (slug, { page = 0, size = 10 } = {}) => {
    const params = new URLSearchParams();
    params.append('page', page);
    params.append('size', size);
    return request(`/submissions/problem/${slug}?${params.toString()}`);
  },
};
