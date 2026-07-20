import React from 'react';
import { render, screen } from '@testing-library/react';
import { MemoryRouter, Routes, Route } from 'react-router-dom';
import { describe, test, expect } from 'vitest';
import ProtectedRoute from '../router/ProtectedRoute';
import { AuthProvider } from '../contexts/AuthContext';

describe('ProtectedRoute Router', () => {
  test('redirects unauthenticated users to login', () => {
    const mockAuthValue = { user: null, loading: false };
    
    render(
      <MemoryRouter initialEntries={['/dashboard']}>
        <AuthProvider value={mockAuthValue}>
          <Routes>
            <Route element={<ProtectedRoute />}>
              <Route path="/dashboard" element={<div>Dashboard Page</div>} />
            </Route>
            <Route path="/login" element={<div>Login Page</div>} />
          </Routes>
        </AuthProvider>
      </MemoryRouter>
    );

    expect(screen.queryByText('Dashboard Page')).not.toBeInTheDocument();
    expect(screen.getByText('Login Page')).toBeInTheDocument();
  });

  test('allows authenticated users to view child outlet pages', () => {
    const mockAuthValue = { user: { email: 'user@example.com' }, loading: false };

    render(
      <MemoryRouter initialEntries={['/dashboard']}>
        <AuthProvider value={mockAuthValue}>
          <Routes>
            <Route element={<ProtectedRoute />}>
              <Route path="/dashboard" element={<div>Dashboard Page</div>} />
            </Route>
            <Route path="/login" element={<div>Login Page</div>} />
          </Routes>
        </AuthProvider>
      </MemoryRouter>
    );

    expect(screen.getByText('Dashboard Page')).toBeInTheDocument();
    expect(screen.queryByText('Login Page')).not.toBeInTheDocument();
  });
});
