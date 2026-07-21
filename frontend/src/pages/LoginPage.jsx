import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../hooks/useAuth';
import { useToast } from '../hooks/useToast';
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
      const user = await login(email, password);
      addToast('Welcome back! Login successful.', 'success');
      
      if (user.role === 'SUPER_ADMIN' || user.role === 'ORG_ADMIN') {
        window.location.assign(ROUTES.ADMIN_ANALYTICS);
      } else if (user.role === 'TURF_OWNER') {
        window.location.assign(ROUTES.OWNER_DASHBOARD);
      } else {
        navigate(ROUTES.DASHBOARD);
      }
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
