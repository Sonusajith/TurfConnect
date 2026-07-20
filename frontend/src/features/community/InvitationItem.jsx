import React from 'react';

const InvitationItem = ({ invitation, onAccept, onDecline }) => {
  const { id, type, fromName, targetName, dateSent, status } = invitation;

  return (
    <div className="flex flex-col sm:flex-row sm:items-center justify-between p-md bg-surface-container-lowest border border-outline-variant/40 rounded-xl hover:shadow-sm transition-shadow gap-4">
      <div className="flex items-start gap-4">
        <div className={`w-12 h-12 rounded-full flex items-center justify-center flex-shrink-0 ${type === 'TEAM' ? 'bg-primary-container text-primary' : 'bg-secondary-container text-secondary'}`}>
          <span className="material-symbols-outlined">
            {type === 'TEAM' ? 'group_add' : 'sports_score'}
          </span>
        </div>
        <div>
          <h4 className="font-body-md text-on-surface">
            <span className="font-bold">{fromName}</span> invited you to join <span className="font-bold">{targetName}</span>
          </h4>
          <p className="text-label-sm text-on-surface-variant mt-1">
            Sent {dateSent} • {type === 'TEAM' ? 'Team Invitation' : 'Match Request'}
          </p>
        </div>
      </div>
      
      {status === 'PENDING' ? (
        <div className="flex gap-2 sm:ml-auto">
          <button 
            onClick={() => onDecline(id)}
            className="px-4 py-2 text-on-surface-variant border border-outline-variant hover:bg-surface-container rounded-lg font-label-md transition-colors"
          >
            Decline
          </button>
          <button 
            onClick={() => onAccept(id)}
            className="px-4 py-2 bg-primary text-white hover:bg-primary-dark rounded-lg font-label-md shadow-sm transition-colors"
          >
            Accept
          </button>
        </div>
      ) : (
        <div className="sm:ml-auto px-3 py-1 rounded bg-surface-container-high text-on-surface-variant font-label-sm uppercase tracking-wider">
          {status}
        </div>
      )}
    </div>
  );
};

export default InvitationItem;
