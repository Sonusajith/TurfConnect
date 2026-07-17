import React from 'react';
import { Link, Outlet, useLocation, useNavigate } from 'react-router-dom';
import { useAuth } from '../contexts/AuthContext';
import { ROUTES } from '../constants/routes';

const AppLayout = () => {
  const { user, logout } = useAuth();
  const location = useLocation();
  const navigate = useNavigate();

  const handleLogout = () => {
    logout();
    navigate(ROUTES.LOGIN);
  };

  const navItems = [
    { label: '🏟️ Dashboard', path: ROUTES.DASHBOARD },
    { label: '📅 My Bookings', path: ROUTES.BOOKINGS },
  ];

  return (
    <div className="min-h-screen bg-background flex flex-col font-sans">
      {/* Header / Navbar */}
      <header className="bg-white border-b border-gray-100 sticky top-0 z-30">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
          <div className="flex justify-between h-16 items-center">
            <div className="flex items-center gap-8">
              <Link to={ROUTES.DASHBOARD} className="text-xl font-bold text-primary flex items-center gap-1.5">
                ⚽ TurfConnect
              </Link>
              
              <nav className="hidden md:flex space-x-4">
                {navItems.map((item) => {
                  const isActive = location.pathname === item.path;
                  return (
                    <Link
                      key={item.path}
                      to={item.path}
                      className={`px-3 py-2 rounded-lg text-sm font-semibold transition-colors ${
                        isActive
                          ? 'bg-primary-light text-primary'
                          : 'text-gray-600 hover:bg-gray-50 hover:text-gray-900'
                      }`}
                    >
                      {item.label}
                    </Link>
                  );
                })}
              </nav>
            </div>

            {/* Right profile info & logout */}
            <div className="flex items-center gap-4">
              <div className="text-right hidden sm:block">
                <p className="text-sm font-semibold text-gray-900">{user?.email}</p>
                <p className="text-xs text-gray-500 font-medium capitalize">{user?.role?.replace('_', ' ')}</p>
              </div>
              <button
                onClick={handleLogout}
                className="px-3.5 py-1.5 text-sm font-semibold border border-red-200 text-red-600 bg-red-50 hover:bg-red-100 rounded-lg transition-colors"
              >
                Logout
              </button>
            </div>
          </div>
        </div>
      </header>

      {/* Main Content Area */}
      <main className="flex-1 max-w-7xl w-full mx-auto px-4 sm:px-6 lg:px-8 py-8">
        <Outlet />
      </main>

      {/* Footer */}
      <footer className="bg-white border-t border-gray-100 py-6 text-center text-sm text-gray-500">
        <p>&copy; 2026 TurfConnect. All rights reserved.</p>
      </footer>
    </div>
  );
};

export default AppLayout;
