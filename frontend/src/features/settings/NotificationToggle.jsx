import React from 'react';

const NotificationToggle = ({ label, description, enabled, onChange }) => {
  return (
    <div className="flex items-center justify-between p-4 bg-surface-container-lowest border border-outline-variant/40 rounded-xl hover:shadow-sm transition-shadow">
      <div className="pr-4">
        <h4 className="font-headline-md text-on-surface text-base">{label}</h4>
        <p className="text-label-sm text-on-surface-variant mt-1">{description}</p>
      </div>
      
      <button 
        type="button"
        role="switch"
        aria-checked={enabled}
        onClick={() => onChange(!enabled)}
        className={`relative inline-flex h-6 w-11 flex-shrink-0 cursor-pointer rounded-full border-2 border-transparent transition-colors duration-200 ease-in-out focus:outline-none focus:ring-2 focus:ring-primary focus:ring-offset-2 ${enabled ? 'bg-primary' : 'bg-surface-container-high'}`}
      >
        <span
          aria-hidden="true"
          className={`pointer-events-none inline-block h-5 w-5 transform rounded-full bg-white shadow ring-0 transition duration-200 ease-in-out ${enabled ? 'translate-x-5' : 'translate-x-0'}`}
        />
      </button>
    </div>
  );
};

export default NotificationToggle;
