import React, { useState, useEffect } from 'react';
import { authService } from '../services/authService';
import { AuthContext } from './authContextCore';

export const AuthProvider = ({ children, value }) => {
  const [user, setUser] = useState(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    // Check if user is already logged in
    const storedUser = localStorage.getItem('user');
    const token = localStorage.getItem('accessToken');
    if (storedUser && token) {
      try {
        setUser(JSON.parse(storedUser));
      } catch {
        localStorage.removeItem('user');
      }
    }
    setLoading(false);
  }, []);

  const login = async (email, password) => {
    setLoading(true);
    try {
      const response = await authService.login(email, password);
      if (response && response.success) {
        const { accessToken, refreshToken, userId, role } = response.data;
        const userData = { userId, email, role };
        
        localStorage.setItem('accessToken', accessToken);
        localStorage.setItem('refreshToken', refreshToken);
        localStorage.setItem('user', JSON.stringify(userData));
        setUser(userData);
        return userData;
      }
      throw new Error(response?.message || 'Login failed');
    } finally {
      setLoading(false);
    }
  };

  const logout = () => {
    authService.logout();
    setUser(null);
  };

  const token = localStorage.getItem('accessToken');
  const providerValue = value || { user, loading, token, login, logout };

  return (
    <AuthContext.Provider value={providerValue}>
      {children}
    </AuthContext.Provider>
  );
};
