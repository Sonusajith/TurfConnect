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
  const [roleMode, setRoleMode] = useState('PLAYER');

  const handleLoginSubmit = async ({ email, password }) => {
    setLoading(true);
    try {
      const user = await login(email, password);
      addToast('Welcome back! Login successful.', 'success');
      
      if (user.role === 'SUPER_ADMIN' || user.role === 'ORG_ADMIN') {
        navigate(ROUTES.ADMIN_ANALYTICS);
      } else if (user.role === 'TURF_OWNER') {
        navigate(ROUTES.OWNER_DASHBOARD);
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
      
      {/* Role Toggle */}
      <div className="flex rounded-lg bg-gray-100 p-1 mb-6">
        <button
          type="button"
          onClick={() => setRoleMode('PLAYER')}
          className={`flex-1 rounded-md py-2 text-sm font-bold transition-colors ${roleMode === 'PLAYER' ? 'bg-white text-primary shadow-sm' : 'text-gray-500 hover:text-gray-900'}`}
        >
          Player
        </button>
        <button
          type="button"
          onClick={() => setRoleMode('TURF_OWNER')}
          className={`flex-1 rounded-md py-2 text-sm font-bold transition-colors ${roleMode === 'TURF_OWNER' ? 'bg-white text-primary shadow-sm' : 'text-gray-500 hover:text-gray-900'}`}
        >
          Turf Owner
        </button>
      </div>

      <LoginForm onSubmit={handleLoginSubmit} isLoading={loading} />
    </div>
  );
};

export default LoginPage;
