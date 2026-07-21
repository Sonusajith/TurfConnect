import React, { useState } from 'react';
import Input from '../../components/Input';
import Button from '../../components/Button';
import { ROUTES } from '../../constants/routes';
import { validateEmail, validatePassword } from '../../utils/validators';

const LoginForm = ({ onSubmit, isLoading }) => {
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [errors, setErrors] = useState({});

  const handleSubmit = (e) => {
    e.preventDefault();

    const emailErr = validateEmail(email);
    const passErr = validatePassword(password);

    if (emailErr || passErr) {
      setErrors({ email: emailErr, password: passErr });
      return;
    }

    setErrors({});
    onSubmit({ email, password });
  };

  return (
    <form onSubmit={handleSubmit} className="space-y-6">
      <Input
        label="Email Address"
        type="email"
        id="email"
        name="email"
        autoComplete="email"
        placeholder="name@example.com"
        value={email}
        onChange={(e) => setEmail(e.target.value)}
        error={errors.email}
      />

      <div className="space-y-2">
        <div className="flex items-center justify-between">
          <label htmlFor="password" className="text-sm font-extrabold tracking-wide text-gray-700">
            Password
          </label>
          <button type="button" className="text-xs font-bold text-primary hover:text-primary-dark">
            Forgot Password?
          </button>
        </div>
        <Input
          type="password"
          id="password"
          name="password"
          autoComplete="current-password"
          placeholder="Enter your password"
          value={password}
          onChange={(e) => setPassword(e.target.value)}
          error={errors.password}
        />
      </div>

      <label className="flex items-center gap-3 text-sm font-semibold text-gray-600">
        <input
          type="checkbox"
          className="h-5 w-5 rounded border-gray-300 text-primary focus:ring-primary"
        />
        Remember Me
      </label>

      <Button
        type="submit"
        variant="primary"
        size="lg"
        className="w-full rounded-full py-4 shadow-lg shadow-primary/20"
        isLoading={isLoading}
      >
        Sign In
        <span aria-hidden="true" className="ml-2 text-xl leading-none">-&gt;</span>
      </Button>

      <div className="flex items-center gap-4">
        <div className="h-px flex-1 bg-gray-200" />
        <span className="text-xs font-semibold text-gray-500">Or continue with</span>
        <div className="h-px flex-1 bg-gray-200" />
      </div>

      <button
        type="button"
        aria-label="Continue with Google"
        className="flex w-full items-center justify-center gap-3 rounded-full border border-gray-300 bg-white px-5 py-3 text-sm font-extrabold tracking-wide text-gray-700 transition hover:bg-gray-50"
      >
        <span className="text-lg font-black text-primary">G</span>
        Sign in with Google
      </button>

      <p className="pt-6 text-center text-sm font-semibold text-gray-500">
        Do not have an account? <a href={ROUTES.REGISTER} className="font-extrabold text-primary hover:underline">Sign Up</a>
      </p>
    </form>
  );
};

export default LoginForm;
