import React, { useState, useRef, useEffect } from 'react';

const Select = ({ options, value, onChange, placeholder = 'Select an option', icon, className = '', buttonClassName = '' }) => {
  const [isOpen, setIsOpen] = useState(false);
  const containerRef = useRef(null);

  useEffect(() => {
    const handleOutsideClick = (e) => {
      if (containerRef.current && !containerRef.current.contains(e.target)) {
        setIsOpen(false);
      }
    };
    document.addEventListener('mousedown', handleOutsideClick);
    return () => document.removeEventListener('mousedown', handleOutsideClick);
  }, []);

  const selectedOption = options.find(opt => opt.value === value);

  return (
    <div className={`relative w-full ${className}`} ref={containerRef}>
      <button
        type="button"
        onClick={() => setIsOpen(!isOpen)}
        className={`w-full flex items-center justify-between bg-white border border-gray-300 rounded-lg p-2.5 text-sm font-semibold text-gray-800 focus:outline-none focus:ring-2 focus:ring-primary focus:border-primary transition-all shadow-sm ${buttonClassName}`}
      >
        <div className="flex items-center gap-2 truncate">
          {icon && <span className="text-primary flex-shrink-0">{icon}</span>}
          <span className="truncate">{selectedOption ? selectedOption.label : <span className="text-gray-500">{placeholder}</span>}</span>
        </div>
        <span className="material-symbols-outlined text-gray-400 flex-shrink-0">
          {isOpen ? 'expand_less' : 'expand_more'}
        </span>
      </button>

      {isOpen && (
        <div className="absolute z-50 w-full mt-1 bg-white border border-gray-200 rounded-lg shadow-xl max-h-60 overflow-y-auto transform origin-top transition-all scale-100 opacity-100">
          <ul className="py-1">
            {options.length === 0 && (
              <li className="px-4 py-3 text-sm text-gray-500 text-center">No options available</li>
            )}
            {options.map((option) => (
              <li key={option.value}>
                <button
                  type="button"
                  className={`w-full text-left px-4 py-2.5 text-sm font-medium transition-colors hover:bg-primary-light hover:text-primary-dark focus:bg-primary-light focus:outline-none ${value === option.value ? 'bg-primary/10 text-primary-dark font-bold border-l-2 border-primary' : 'text-gray-700 border-l-2 border-transparent'}`}
                  onClick={() => {
                    onChange(option.value);
                    setIsOpen(false);
                  }}
                >
                  {option.label}
                </button>
              </li>
            ))}
          </ul>
        </div>
      )}
    </div>
  );
};

export default Select;
