import React, { useState } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import { authService } from '../services/authService';
import { useToast } from '../hooks/useToast';
import { ROUTES } from '../constants/routes';
import Input from '../components/Input';
import Button from '../components/Button';
import { validateEmail, validatePassword } from '../utils/validators';

const RegisterPage = () => {
  const [role, setRole] = useState('PLAYER');
  const [name, setName] = useState('');
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [loading, setLoading] = useState(false);
  const [errors, setErrors] = useState({});
  const { addToast } = useToast();
  const navigate = useNavigate();

  const handleRegisterSubmit = async (e) => {
    e.preventDefault();
    const emailErr = validateEmail(email);
    const passErr = validatePassword(password);
    
    if (emailErr || passErr || !name) {
      setErrors({ email: emailErr, password: passErr, name: !name ? 'Name is required' : null });
      return;
    }
    
    setErrors({});
    setLoading(true);
    
    try {
      const res = await authService.register(name, email, password, role);
      if (res && res.success) {
        addToast('Registration successful! Please login.', 'success');
        navigate(ROUTES.LOGIN);
      } else {
        throw new Error(res?.message || 'Registration failed');
      }
    } catch (err) {
      addToast(err.message || 'Registration failed', 'error');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div>
      <h3 className="sr-only">Create your TurfConnect account</h3>
      
      {/* Role Toggle */}
      <div className="flex rounded-lg bg-gray-100 p-1 mb-6">
        <button
          type="button"
          onClick={() => setRole('PLAYER')}
          className={`flex-1 rounded-md py-2 text-sm font-bold transition-colors ${role === 'PLAYER' ? 'bg-white text-primary shadow-sm' : 'text-gray-500 hover:text-gray-900'}`}
        >
          Player
        </button>
        <button
          type="button"
          onClick={() => setRole('TURF_OWNER')}
          className={`flex-1 rounded-md py-2 text-sm font-bold transition-colors ${role === 'TURF_OWNER' ? 'bg-white text-primary shadow-sm' : 'text-gray-500 hover:text-gray-900'}`}
        >
          Turf Owner
        </button>
      </div>

      <form onSubmit={handleRegisterSubmit} className="space-y-6">
        <Input
          label="Full Name"
          type="text"
          id="name"
          placeholder="John Doe"
          value={name}
          onChange={(e) => setName(e.target.value)}
          error={errors.name}
        />

        <Input
          label="Email Address"
          type="email"
          id="email"
          placeholder="name@example.com"
          value={email}
          onChange={(e) => setEmail(e.target.value)}
          error={errors.email}
        />

        <Input
          label="Password"
          type="password"
          id="password"
          placeholder="Create a password"
          value={password}
          onChange={(e) => setPassword(e.target.value)}
          error={errors.password}
        />

        <Button
          type="submit"
          variant="primary"
          size="lg"
          className="w-full rounded-full py-4 shadow-lg shadow-primary/20"
          isLoading={loading}
        >
          Sign Up
          <span aria-hidden="true" className="ml-2 text-xl leading-none">-&gt;</span>
        </Button>

        <p className="pt-6 text-center text-sm font-semibold text-gray-500">
          Already have an account? <Link to={ROUTES.LOGIN} className="font-extrabold text-primary hover:underline">Sign In</Link>
        </p>
      </form>
    </div>
  );
};

export default RegisterPage;
