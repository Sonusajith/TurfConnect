import React from 'react';

export const Card = ({ children, className = '', interactive = false, ...props }) => {
  const interactiveStyles = interactive ? 'hover:-translate-y-1 hover:shadow-lg cursor-pointer transition-all duration-300' : '';
  
  return (
    <div 
      className={`bg-surface rounded-2xl shadow-sm border border-gray-100 overflow-hidden ${interactiveStyles} ${className}`}
      {...props}
    >
      {children}
    </div>
  );
};

export const CardHeader = ({ children, className = '' }) => (
  <div className={`px-6 py-4 border-b border-gray-50 flex justify-between items-center ${className}`}>
    {children}
  </div>
);

export const CardTitle = ({ children, className = '' }) => (
  <h3 className={`text-lg font-bold text-gray-900 ${className}`}>
    {children}
  </h3>
);

export const CardContent = ({ children, className = '' }) => (
  <div className={`p-6 ${className}`}>
    {children}
  </div>
);

export const CardFooter = ({ children, className = '' }) => (
  <div className={`px-6 py-4 bg-gray-50/50 border-t border-gray-50 ${className}`}>
    {children}
  </div>
);
