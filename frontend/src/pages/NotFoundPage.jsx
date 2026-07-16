import React from 'react';
import { useNavigate } from 'react-router-dom';
import Button from '../components/Button';
import { ROUTES } from '../constants/routes';

const NotFoundPage = () => {
  const navigate = useNavigate();

  return (
    <div className="text-center py-24 flex flex-col items-center justify-center space-y-6">
      <div className="text-6xl">⚽</div>
      <h1 className="text-4xl font-extrabold text-gray-900 tracking-tight">404 - Page Not Found</h1>
      <p className="text-base text-gray-500 max-w-md">
        Oops! The page you are looking for does not exist or has been moved to another location.
      </p>
      <div className="pt-4">
        <Button variant="primary" onClick={() => navigate(ROUTES.DASHBOARD)}>
          Go back to Dashboard
        </Button>
      </div>
    </div>
  );
};

export default NotFoundPage;
