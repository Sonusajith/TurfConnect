export const validateEmail = (email) => {
  if (!email) return 'Email is required';
  const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
  if (!emailRegex.test(email)) return 'Invalid email address';
  return '';
};

export const validatePassword = (password) => {
  if (!password) return 'Password is required';
  if (password.length < 6) return 'Password must be at least 6 characters long';
  return '';
};

export const validateRegistrationPassword = (password) => {
  if (!password) return 'Password is required';
  if (password.length < 8) return 'Password must be at least 8 characters long';
  if (!/[A-Z]/.test(password)) return 'Password must contain one uppercase letter';
  if (!/[a-z]/.test(password)) return 'Password must contain one lowercase letter';
  if (!/\d/.test(password)) return 'Password must contain one number';
  if (!/[@$!%*?&]/.test(password)) return 'Password must contain one special character';
  return '';
};
