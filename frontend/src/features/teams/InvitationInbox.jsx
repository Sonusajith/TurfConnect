import React from 'react';
import { Card, CardContent } from '../../components/ui/Card';

const InvitationInbox = ({ invitations, onRespond }) => {
  if (!invitations || invitations.length === 0) {
    return (
      <div className="text-center py-12 bg-white rounded-2xl border border-primary/10 shadow-sm">
        <span className="material-symbols-outlined text-4xl text-gray-300 mb-2">inbox</span>
        <p className="text-gray-500 font-medium">You have no pending invitations.</p>
      </div>
    );
  }

  return (
    <div className="space-y-4">
      {invitations.map((inv, i) => (
        <Card key={inv.id || i} className="border border-outline-variant/30">
          <CardContent className="p-4 sm:p-5 flex flex-col sm:flex-row sm:items-center justify-between gap-4">
            <div className="flex items-center gap-4">
              <div className="w-12 h-12 rounded-full bg-accent-light text-accent flex items-center justify-center">
                <span className="material-symbols-outlined font-bold text-xl">group_add</span>
              </div>
              <div>
                <p className="font-semibold text-gray-900">
                  <span className="font-extrabold">{inv.senderName}</span> invited you to join <span className="font-extrabold text-primary-dark">{inv.teamName}</span>
                </p>
                <p className="text-xs text-gray-500 font-medium mt-0.5">{new Date(inv.createdAt || Date.now()).toLocaleDateString()}</p>
              </div>
            </div>
            
            <div className="flex items-center gap-2 sm:ml-auto">
              <button 
                onClick={() => onRespond(inv.id, 'ACCEPTED')}
                className="flex-1 sm:flex-none px-4 py-2 bg-primary text-white font-bold text-sm rounded-lg hover:bg-primary-dark transition-colors"
              >
                Accept
              </button>
              <button 
                onClick={() => onRespond(inv.id, 'DECLINED')}
                className="flex-1 sm:flex-none px-4 py-2 bg-gray-100 text-gray-700 font-bold text-sm rounded-lg hover:bg-gray-200 transition-colors"
              >
                Decline
              </button>
            </div>
          </CardContent>
        </Card>
      ))}
    </div>
  );
};

export default InvitationInbox;
