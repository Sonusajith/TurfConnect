import React, { useState } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import { useAuth } from '../hooks/useAuth';
import { useToast } from '../hooks/useToast';
import { ROUTES } from '../constants/routes';
import Input from '../components/Input';
import Button from '../components/Button';
import { validateEmail, validateRegistrationPassword } from '../utils/validators';

const roles = [
  {
    id: 'PLAYER',
    title: 'Player',
    icon: 'sports_soccer',
    description: 'Book turfs, manage teams, split payments, and join tournaments.',
  },
  {
    id: 'TURF_OWNER',
    title: 'Turf Owner',
    icon: 'stadium',
    description: 'List venues, manage bookings, and track your turf business.',
  },
];

const RegisterPage = () => {
  const [role, setRole] = useState('PLAYER');
  const [name, setName] = useState('');
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [confirmPassword, setConfirmPassword] = useState('');
  const [loading, setLoading] = useState(false);
  const [errors, setErrors] = useState({});
  const { addToast } = useToast();
  const { register } = useAuth();
  const navigate = useNavigate();

  const handleRegisterSubmit = async (e) => {
    e.preventDefault();
    const emailErr = validateEmail(email);
    const passErr = validateRegistrationPassword(password);
    const trimmedName = name.trim();
    const confirmPasswordErr = password !== confirmPassword ? 'Passwords do not match' : '';
    
    if (emailErr || passErr || confirmPasswordErr || !trimmedName) {
      setErrors({
        email: emailErr,
        password: passErr,
        confirmPassword: confirmPasswordErr,
        name: !trimmedName ? 'Full name is required' : '',
      });
      return;
    }
    
    setErrors({});
    setLoading(true);
    
    try {
      const user = await register({ name: trimmedName, email: email.trim(), password, role });
      addToast('Account created successfully. Welcome to TurfConnect!', 'success');

      if (user.role === 'TURF_OWNER') {
        navigate(ROUTES.OWNER_DASHBOARD, { replace: true });
      } else {
        navigate(ROUTES.DASHBOARD, { replace: true });
      }
    } catch (err) {
      addToast(err.message || 'Registration failed', 'error');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="space-y-7">
      <div>
        <p className="text-xs font-extrabold uppercase tracking-wider text-accent">New Account</p>
        <h3 className="mt-1 text-2xl font-extrabold tracking-tight text-gray-950">Create your TurfConnect profile</h3>
        <p className="mt-2 text-sm font-semibold text-gray-500">Choose the account type that matches how you want to use the platform.</p>
      </div>

      <div className="grid gap-3 sm:grid-cols-2">
        {roles.map((accountRole) => {
          const selected = role === accountRole.id;
          return (
            <button
              key={accountRole.id}
              type="button"
              onClick={() => setRole(accountRole.id)}
              className={`rounded-lg border p-4 text-left transition-all ${
                selected
                  ? 'border-primary bg-primary-light shadow-sm ring-2 ring-primary/20'
                  : 'border-gray-200 bg-white hover:border-primary/40'
              }`}
            >
              <span className={`material-symbols-outlined mb-3 text-3xl ${selected ? 'text-primary' : 'text-gray-400'}`}>
                {accountRole.icon}
              </span>
              <span className="block text-base font-extrabold text-gray-950">{accountRole.title}</span>
              <span className="mt-1 block text-xs font-semibold leading-5 text-gray-500">{accountRole.description}</span>
            </button>
          );
        })}
      </div>

      <form onSubmit={handleRegisterSubmit} className="space-y-6">
        <Input
          label="Full Name"
          type="text"
          id="name"
          name="name"
          autoComplete="name"
          placeholder={role === 'TURF_OWNER' ? 'Sonu Turf Owner' : 'Saif Player'}
          value={name}
          onChange={(e) => setName(e.target.value)}
          error={errors.name}
        />

        <Input
          label="Email Address"
          type="email"
          id="email"
          name="email"
          autoComplete="email"
          placeholder={role === 'TURF_OWNER' ? 'owner@example.com' : 'player@example.com'}
          value={email}
          onChange={(e) => setEmail(e.target.value)}
          error={errors.email}
        />

        <Input
          label="Password"
          type="password"
          id="password"
          name="password"
          autoComplete="new-password"
          placeholder="Use uppercase, number, and special character"
          value={password}
          onChange={(e) => setPassword(e.target.value)}
          error={errors.password}
        />

        <Input
          label="Confirm Password"
          type="password"
          id="confirmPassword"
          name="confirmPassword"
          autoComplete="new-password"
          placeholder="Re-enter your password"
          value={confirmPassword}
          onChange={(e) => setConfirmPassword(e.target.value)}
          error={errors.confirmPassword}
        />

        <div className="rounded-lg border border-primary/10 bg-primary-light/60 p-4">
          <p className="text-sm font-extrabold text-primary-dark">
            {role === 'TURF_OWNER' ? 'Owner account will open the dashboard after signup.' : 'Player account will open the booking dashboard after signup.'}
          </p>
          <p className="mt-1 text-xs font-semibold text-gray-600">
            Password example: Password123!
          </p>
        </div>

        <Button
          type="submit"
          variant="primary"
          size="lg"
          className="w-full rounded-full py-4 shadow-lg shadow-primary/20"
          isLoading={loading}
        >
          Create {role === 'TURF_OWNER' ? 'Owner' : 'Player'} Account
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
