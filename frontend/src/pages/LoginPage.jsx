import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../contexts/AuthContext';
import { useToast } from '../contexts/ToastContext';
import LoginForm from '../features/auth/LoginForm';
import { ROUTES } from '../constants/routes';

const LoginPage = () => {
  const { login } = useAuth();
  const { addToast } = useToast();
  const navigate = useNavigate();
  const [loading, setLoading] = useState(false);

  const handleLoginSubmit = async ({ email, password }) => {
    setLoading(true);
    try {
      await login(email, password);
      addToast('Welcome back! Login successful.', 'success');
      navigate(ROUTES.DASHBOARD);
    } catch (e) {
      addToast(e.message || 'Authentication failed. Please verify credentials.', 'error');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div>
      <h3 className="sr-only">Sign in to your TurfConnect account</h3>
      <LoginForm onSubmit={handleLoginSubmit} isLoading={loading} />
    </div>
  );
};

export default LoginPage;
