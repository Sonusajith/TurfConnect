import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../hooks/useAuth';
import { useToast } from '../hooks/useToast';
import { ROUTES } from '../constants/routes';
import Input from '../components/Input';
import Button from '../components/Button';

const AdminSecretLogin = () => {
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [loading, setLoading] = useState(false);
  const { login } = useAuth();
  const { addToast } = useToast();
  const navigate = useNavigate();

  const handleLoginSubmit = async (e) => {
    e.preventDefault();
    setLoading(true);
    try {
      const user = await login(email, password);
      if (user.role === 'SUPER_ADMIN' || user.role === 'ORG_ADMIN') {
        addToast('Admin access granted.', 'success');
        navigate(ROUTES.ADMIN_ANALYTICS);
      } else {
        addToast('Unauthorized role.', 'error');
        // We log them out if they are not an admin trying to use the secret portal
        // authService.logout() could be called here
      }
    } catch (err) {
      addToast(err.message || 'Authentication failed.', 'error');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div>
      <h3 className="sr-only">Sign in to Admin Portal</h3>
      <div className="mb-6 rounded-lg bg-red-50 p-4 border border-red-100">
        <p className="text-sm font-bold text-red-600 text-center uppercase tracking-wider">Super Admin Portal</p>
      </div>
      <form onSubmit={handleLoginSubmit} className="space-y-6">
        <Input
          label="Admin Email"
          type="email"
          id="email"
          placeholder="admin@turfconnect.com"
          value={email}
          onChange={(e) => setEmail(e.target.value)}
        />
        <Input
          label="Secret Password"
          type="password"
          id="password"
          placeholder="Enter secret password"
          value={password}
          onChange={(e) => setPassword(e.target.value)}
        />
        <Button
          type="submit"
          variant="primary"
          size="lg"
          className="w-full rounded-full py-4 shadow-lg shadow-red-500/20 bg-red-600 hover:bg-red-700"
          isLoading={loading}
        >
          Access Portal
          <span aria-hidden="true" className="ml-2 text-xl leading-none">-&gt;</span>
        </Button>
      </form>
    </div>
  );
};

export default AdminSecretLogin;
