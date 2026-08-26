import React, { createContext, useContext, useState, useEffect, useCallback } from 'react';
import { authApi, userApi } from '../services/api';

const AuthContext = createContext(null);

export function AuthProvider({ children }) {
  const [token, setToken] = useState(() => localStorage.getItem('codeforge_token'));
  const [currentUser, setCurrentUser] = useState(() => {
    const savedUser = localStorage.getItem('codeforge_user');
    return savedUser ? JSON.parse(savedUser) : null;
  });
  const [profile, setProfile] = useState(null);
  const [isLoading, setIsLoading] = useState(true);

  const fetchProfile = useCallback(async () => {
    if (!token) {
      setProfile(null);
      setIsLoading(false);
      return;
    }

    try {
      const res = await userApi.getMe();
      if (res.success && res.data) {
        setProfile(res.data);
      }
    } catch (err) {
      console.warn('Could not fetch user profile, session might be invalid:', err.message);
    } finally {
      setIsLoading(false);
    }
  }, [token]);

  useEffect(() => {
    fetchProfile();
  }, [fetchProfile]);

  const login = async (usernameOrEmail, password) => {
    const res = await authApi.login({ usernameOrEmail, password });
    if (res.success && res.data) {
      const { token: jwtToken, ...userData } = res.data;
      localStorage.setItem('codeforge_token', jwtToken);
      localStorage.setItem('codeforge_user', JSON.stringify(userData));
      setToken(jwtToken);
      setCurrentUser(userData);
      await fetchProfile();
      return res.data;
    }
  };

  const register = async (username, email, password) => {
    const res = await authApi.register({ username, email, password });
    if (res.success && res.data) {
      const { token: jwtToken, ...userData } = res.data;
      localStorage.setItem('codeforge_token', jwtToken);
      localStorage.setItem('codeforge_user', JSON.stringify(userData));
      setToken(jwtToken);
      setCurrentUser(userData);
      await fetchProfile();
      return res.data;
    }
  };

  const logout = () => {
    localStorage.removeItem('codeforge_token');
    localStorage.removeItem('codeforge_user');
    setToken(null);
    setCurrentUser(null);
    setProfile(null);
  };

  return (
    <AuthContext.Provider
      value={{
        token,
        currentUser,
        profile,
        isLoading,
        isAuthenticated: !!token,
        login,
        register,
        logout,
        refreshProfile: fetchProfile,
      }}
    >
      {children}
    </AuthContext.Provider>
  );
}

export function useAuth() {
  const context = useContext(AuthContext);
  if (!context) {
    throw new Error('useAuth must be used within an AuthProvider');
  }
  return context;
}
