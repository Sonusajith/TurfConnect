import React, { useEffect } from 'react';
import useTeams from '../hooks/useTeams';
import InvitationInbox from '../features/teams/InvitationInbox';

const InvitationsPage = () => {
  const { invitations, fetchInvitations, respondToInvitation } = useTeams();

  useEffect(() => {
    fetchInvitations();
  }, [fetchInvitations]);

  return (
    <div className="space-y-6 animate-fade-in">
      <div>
        <p className="text-xs font-extrabold uppercase tracking-wider text-accent">Community</p>
        <h1 className="text-3xl font-extrabold tracking-tight text-primary-dark sm:text-4xl">Invitations</h1>
        <p className="mt-1 text-sm font-medium text-gray-500">Pending team invites and match requests.</p>
      </div>

      <InvitationInbox 
        invitations={invitations} 
        onRespond={respondToInvitation} 
      />
    </div>
  );
};

export default InvitationsPage;
