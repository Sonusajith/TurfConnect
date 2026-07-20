import React from 'react';
import { Link, Outlet, useLocation, useNavigate } from 'react-router-dom';
import { useAuth } from '../contexts/AuthContext';
import { ROUTES } from '../constants/routes';

const BallIcon = ({ className = 'h-5 w-5' }) => (
  <svg className={className} viewBox="0 0 24 24" fill="none" aria-hidden="true">
    <circle cx="12" cy="12" r="9" stroke="currentColor" strokeWidth="2" />
    <path
      d="M8.5 5.8 12 8.3l3.5-2.5M12 8.3v4.4m0 0-4.2 2.8m4.2-2.8 4.2 2.8M7.8 15.5l1.4 3.9m7-3.9-1.4 3.9"
      stroke="currentColor"
      strokeWidth="2"
      strokeLinecap="round"
      strokeLinejoin="round"
    />
  </svg>
);

const MiniIcon = ({ children }) => (
  <span className="flex h-5 w-5 items-center justify-center rounded border border-current text-[10px] font-black">
    {children}
  </span>
);

const AppLayout = () => {
  const { user, logout } = useAuth();
  const location = useLocation();
  const navigate = useNavigate();

  const handleLogout = () => {
    logout();
    navigate(ROUTES.LOGIN);
  };

  const navItems = [
    { id: 'home', label: 'Home', path: ROUTES.DASHBOARD, icon: <MiniIcon>H</MiniIcon> },
    { id: 'book', label: 'Book Turf', path: ROUTES.DASHBOARD, icon: <BallIcon /> },
    { id: 'bookings', label: 'My Bookings', path: ROUTES.BOOKINGS, icon: <MiniIcon>B</MiniIcon> },
    { id: 'teams', label: 'Teams', path: ROUTES.TEAMS, icon: <MiniIcon>T</MiniIcon> },
    { id: 'matches', label: 'Matches', path: ROUTES.MATCHES, icon: <MiniIcon>M</MiniIcon> },
    { id: 'tournaments', label: 'Tournaments', path: ROUTES.TOURNAMENTS, icon: <MiniIcon>C</MiniIcon> },
    { id: 'analytics', label: 'Analytics', path: ROUTES.ADMIN_ANALYTICS, icon: <MiniIcon>A</MiniIcon> },
    { id: 'settings', label: 'Settings', path: ROUTES.SETTINGS, icon: <MiniIcon>S</MiniIcon> },
  ];

  const isActive = (item) => {
    if (item.id === 'home') return location.pathname === ROUTES.DASHBOARD;
    if (item.id === 'book') return location.pathname.startsWith('/turfs');
    return location.pathname === item.path;
  };

  return (
    <div className="min-h-screen bg-background font-sans text-gray-950 lg:flex">
      <aside className="hidden w-64 shrink-0 border-r border-primary/10 bg-[#e8f6ff] shadow-sm lg:sticky lg:top-0 lg:flex lg:h-screen lg:flex-col">
        <div className="px-7 py-8">
          <Link to={ROUTES.DASHBOARD} className="flex items-center gap-3 text-primary-dark">
            <span className="flex h-10 w-10 items-center justify-center rounded-lg bg-primary text-white">
              <BallIcon className="h-6 w-6" />
            </span>
            <span>
              <span className="block text-2xl font-extrabold leading-none">TurfConnect</span>
              <span className="mt-1 block text-sm font-semibold text-gray-500">Field Management</span>
            </span>
          </Link>
        </div>

        <nav className="flex-1 space-y-2 px-4 pt-8">
          {navItems.map((item) => (
            <Link
              key={item.id}
              to={item.path}
              className={`flex items-center gap-4 rounded-lg px-5 py-3 text-sm font-extrabold tracking-wide transition ${
                isActive(item)
                  ? 'bg-[#8bf28a] text-primary-dark shadow-sm'
                  : 'text-gray-700 hover:bg-white/70 hover:text-primary-dark'
              }`}
            >
              {item.icon}
              {item.label}
            </Link>
          ))}
        </nav>

        <div className="border-t border-primary/10 p-4">
          <button
            onClick={handleLogout}
            className="flex w-full items-center gap-4 rounded-lg px-5 py-3 text-sm font-extrabold tracking-wide text-gray-700 transition hover:bg-white/70 hover:text-red-600"
          >
            <MiniIcon>L</MiniIcon>
            Logout
          </button>
        </div>
      </aside>

      <div className="min-w-0 flex-1">
        <header className="sticky top-0 z-30 border-b border-primary/10 bg-white/95 backdrop-blur">
          <div className="mx-auto flex h-[74px] max-w-7xl items-center justify-between px-4 sm:px-6 lg:px-10">
            <Link to={ROUTES.DASHBOARD} className="flex items-center gap-2 text-primary-dark lg:hidden">
              <span className="flex h-9 w-9 items-center justify-center rounded-lg bg-primary text-white">
                <BallIcon />
              </span>
              <span className="text-lg font-extrabold">TurfConnect Pro</span>
            </Link>

            <div className="hidden lg:block">
              <p className="text-sm font-semibold text-gray-500">Welcome back, Athlete</p>
              <h1 className="text-2xl font-extrabold tracking-tight text-gray-950">TurfConnect Pro</h1>
            </div>

            <div className="flex items-center gap-3">
              <Link
                to={ROUTES.DASHBOARD}
                className="hidden rounded-lg bg-accent px-5 py-3 text-sm font-extrabold text-white shadow-sm shadow-accent/20 transition hover:bg-accent-dark sm:inline-flex"
              >
                Book a Turf
              </Link>
              <div className="hidden text-right sm:block">
                <p className="max-w-52 truncate text-sm font-bold text-gray-900">{user?.email}</p>
                <p className="text-xs font-semibold capitalize text-gray-500">{user?.role?.replace('_', ' ')}</p>
              </div>
              <button
                onClick={handleLogout}
                className="rounded-lg border border-red-200 bg-red-50 px-3 py-2 text-sm font-bold text-red-600 transition hover:bg-red-100 lg:hidden"
              >
                Logout
              </button>
            </div>
          </div>
        </header>

        <main className="mx-auto w-full max-w-7xl px-4 py-8 sm:px-6 lg:px-10">
          <Outlet />
        </main>
      </div>
    </div>
  );
};

export default AppLayout;
