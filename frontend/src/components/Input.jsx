import React from 'react';

const Input = ({
  label,
  type = 'text',
  error,
  id,
  className = '',
  ...props
}) => {
  return (
    <div className={`flex flex-col gap-1.5 w-full ${className}`}>
      {label && (
        <label htmlFor={id} className="text-sm font-extrabold tracking-wide text-gray-700">
          {label}
        </label>
      )}
      <input
        type={type}
        id={id}
        className={`
          w-full px-4 py-3 border rounded-lg bg-[#eaf6ff] text-gray-900 placeholder-gray-500 focus:outline-none focus:ring-2 transition-all
          ${error
            ? 'border-red-500 focus:ring-red-200 focus:border-red-500'
            : 'border-gray-300 focus:ring-primary-light focus:border-primary'
          }
        `}
        {...props}
      />
      {error && <span className="text-xs text-red-500 font-medium">{error}</span>}
    </div>
  );
};

export default Input;
