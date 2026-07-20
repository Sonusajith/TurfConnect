import React, { useState, useRef, useEffect } from 'react';

const DatePicker = ({ value, onChange, min, className = '', id = '' }) => {
  const [isOpen, setIsOpen] = useState(false);
  const [currentMonth, setCurrentMonth] = useState(() => {
    return value ? new Date(value) : new Date();
  });
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

  const getDaysInMonth = (date) => {
    const year = date.getFullYear();
    const month = date.getMonth();
    const days = new Date(year, month + 1, 0).getDate();
    const firstDay = new Date(year, month, 1).getDay();
    return { days, firstDay };
  };

  const { days, firstDay } = getDaysInMonth(currentMonth);
  const minDate = min ? new Date(min) : new Date(0);
  minDate.setHours(0, 0, 0, 0);

  const prevMonth = () => {
    setCurrentMonth(new Date(currentMonth.getFullYear(), currentMonth.getMonth() - 1, 1));
  };

  const nextMonth = () => {
    setCurrentMonth(new Date(currentMonth.getFullYear(), currentMonth.getMonth() + 1, 1));
  };

  const selectDate = (day) => {
    const selected = new Date(currentMonth.getFullYear(), currentMonth.getMonth(), day);
    // Format to YYYY-MM-DD
    const year = selected.getFullYear();
    const month = String(selected.getMonth() + 1).padStart(2, '0');
    const d = String(selected.getDate()).padStart(2, '0');
    onChange(`${year}-${month}-${d}`);
    setIsOpen(false);
  };

  // Format display value like "July 20, 2026"
  const displayValue = value ? new Date(value).toLocaleDateString('en-US', { month: 'long', day: 'numeric', year: 'numeric' }) : 'Select Date';
  const monthName = currentMonth.toLocaleDateString('en-US', { month: 'long', year: 'numeric' });

  return (
    <div className={`relative w-full ${className}`} ref={containerRef}>
      <button
        type="button"
        id={id}
        onClick={() => setIsOpen(!isOpen)}
        className="w-full flex items-center bg-white border border-gray-300 rounded-lg pl-10 pr-4 py-2.5 text-sm font-semibold text-gray-800 focus:outline-none focus:ring-2 focus:ring-primary focus:border-primary transition-all shadow-sm"
      >
        <div className="absolute inset-y-0 left-0 pl-3 flex items-center pointer-events-none text-primary">
          <span className="material-symbols-outlined text-lg">calendar_month</span>
        </div>
        <span className="truncate">{displayValue}</span>
      </button>

      {isOpen && (
        <div className="absolute z-50 mt-1 bg-white border border-gray-200 rounded-lg shadow-xl p-3 w-72 transform origin-top transition-all scale-100 opacity-100 left-0">
          <div className="flex justify-between items-center mb-3">
            <button type="button" onClick={prevMonth} className="p-1 hover:bg-gray-100 rounded text-gray-600 transition-colors">
              <span className="material-symbols-outlined text-sm">chevron_left</span>
            </button>
            <div className="text-sm font-bold text-gray-800">{monthName}</div>
            <button type="button" onClick={nextMonth} className="p-1 hover:bg-gray-100 rounded text-gray-600 transition-colors">
              <span className="material-symbols-outlined text-sm">chevron_right</span>
            </button>
          </div>
          
          <div className="grid grid-cols-7 gap-1 text-center mb-2">
            {['Su', 'Mo', 'Tu', 'We', 'Th', 'Fr', 'Sa'].map(d => (
              <div key={d} className="text-xs font-bold text-gray-400">{d}</div>
            ))}
          </div>
          
          <div className="grid grid-cols-7 gap-1">
            {Array.from({ length: firstDay }).map((_, i) => (
              <div key={`empty-${i}`} className="p-2"></div>
            ))}
            {Array.from({ length: days }).map((_, i) => {
              const day = i + 1;
              const dateToCheck = new Date(currentMonth.getFullYear(), currentMonth.getMonth(), day);
              dateToCheck.setHours(0, 0, 0, 0);
              const isDisabled = dateToCheck < minDate;
              
              const isSelected = value && 
                new Date(value).getDate() === day && 
                new Date(value).getMonth() === currentMonth.getMonth() && 
                new Date(value).getFullYear() === currentMonth.getFullYear();

              return (
                <button
                  key={day}
                  type="button"
                  disabled={isDisabled}
                  onClick={() => selectDate(day)}
                  className={`p-1.5 text-sm rounded-md transition-colors ${
                    isDisabled ? 'text-gray-300 cursor-not-allowed' :
                    isSelected ? 'bg-primary text-white font-bold shadow' :
                    'text-gray-700 hover:bg-primary-light hover:text-primary-dark'
                  }`}
                >
                  {day}
                </button>
              );
            })}
          </div>
        </div>
      )}
    </div>
  );
};

export default DatePicker;
