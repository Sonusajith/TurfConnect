import React, { useState } from 'react';
import InvitationItem from '../features/community/InvitationItem';

const MOCK_INVITATIONS = [
  { id: 1, type: 'TEAM', fromName: 'Alex Smith', targetName: 'FC Thunder', dateSent: '2 hours ago', status: 'PENDING' },
  { id: 2, type: 'MATCH', fromName: 'Pitch Pirates', targetName: 'Weekend Warriors', dateSent: 'Yesterday', status: 'PENDING' },
  { id: 3, type: 'TEAM', fromName: 'Sarah J', targetName: 'Net Ninjas', dateSent: '3 days ago', status: 'ACCEPTED' },
];

const InvitationsPage = () => {
  const [invitations, setInvitations] = useState(MOCK_INVITATIONS);

  const handleAccept = (id) => {
    setInvitations(prev => prev.map(inv => inv.id === id ? { ...inv, status: 'ACCEPTED' } : inv));
  };

  const handleDecline = (id) => {
    setInvitations(prev => prev.map(inv => inv.id === id ? { ...inv, status: 'DECLINED' } : inv));
  };

  const pendingCount = invitations.filter(i => i.status === 'PENDING').length;

  return (
    <div className="p-margin-mobile md:p-margin-desktop animate-fade-in max-w-4xl mx-auto pb-24 md:pb-8">
      <div className="mb-8 border-b border-outline-variant/30 pb-6">
        <h1 className="font-headline-lg text-headline-lg text-on-surface flex items-center gap-3">
          Inbox
          {pendingCount > 0 && (
            <span className="bg-error text-white text-sm px-2.5 py-0.5 rounded-full font-bold">
              {pendingCount} New
            </span>
          )}
        </h1>
        <p className="text-on-surface-variant mt-2">Manage your team and match invitations.</p>
      </div>

      <div className="space-y-4">
        {invitations.length > 0 ? (
          invitations.map(invitation => (
            <InvitationItem 
              key={invitation.id}
              invitation={invitation}
              onAccept={handleAccept}
              onDecline={handleDecline}
            />
          ))
        ) : (
          <div className="text-center py-16 bg-surface-container-lowest rounded-xl border border-dashed border-outline-variant">
            <span className="material-symbols-outlined text-4xl text-on-surface-variant mb-4">inbox</span>
            <p className="text-on-surface-variant font-body-md">You're all caught up! No pending invitations.</p>
          </div>
        )}
      </div>
    </div>
  );
};

export default InvitationsPage;
