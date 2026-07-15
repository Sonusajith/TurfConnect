import React from 'react';

const Card = ({ children, className = '', onClick }) => {
  return (
    <div
      onClick={onClick}
      className={`
        bg-white rounded-2xl shadow-sm border border-gray-100 p-5 
        ${onClick ? 'cursor-pointer hover:shadow-md hover:border-gray-200 transition-all' : ''}
        ${className}
      `}
    >
      {children}
    </div>
  );
};

export default Card;
