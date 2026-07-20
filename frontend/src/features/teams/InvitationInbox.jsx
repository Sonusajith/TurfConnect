import React from 'react';

const InvitationInbox = () => {
  return (
    <section className="rounded-lg border border-primary/10 bg-white p-5 shadow-sm">
      <p className="text-xs font-extrabold uppercase tracking-wider text-gray-500">Invitations</p>
      <h2 className="mt-1 text-xl font-extrabold tracking-tight text-gray-950">Invitation Inbox</h2>
      <p className="mt-2 text-sm font-medium text-gray-500">
        Pending invitations with accept and decline actions will be wired here.
      </p>
    </section>
  );
};

export default InvitationInbox;
