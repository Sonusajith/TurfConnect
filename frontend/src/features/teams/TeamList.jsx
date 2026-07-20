import React, { useState } from 'react';
import { Card, CardContent } from '../../components/ui/Card';
import Modal from '../../components/Modal';
import { useToast } from '../../hooks/useToast';
import TeamCreateModal from './TeamCreateModal';
import TeamDetailsModal from './TeamDetailsModal';

const TeamList = ({ teams, loading, error, onSendInvite, onUpdateTeam }) => {
  const [inviteModalData, setInviteModalData] = useState({ isOpen: false, teamId: null, teamName: '' });
  const [editModalData, setEditModalData] = useState({ isOpen: false, team: null });
  const [detailsModalData, setDetailsModalData] = useState({ isOpen: false, team: null });
  const [inviteEmail, setInviteEmail] = useState('');
  const [isSubmitting, setIsSubmitting] = useState(false);
  const { addToast } = useToast();

  const handleInviteSubmit = async (e) => {
    e.preventDefault();
    if (!inviteEmail || !inviteEmail.includes('@')) {
      addToast('Please enter a valid email address', 'error');
      return;
    }
    
    setIsSubmitting(true);
    try {
      await onSendInvite(inviteModalData.teamId, inviteEmail, `You have been invited to join ${inviteModalData.teamName}!`);
      addToast(`Invitation sent successfully to ${inviteEmail}`, 'success');
      setInviteModalData({ isOpen: false, teamId: null, teamName: '' });
      setInviteEmail('');
    } catch (err) {
      addToast(err.message || 'Failed to send invitation', 'error');
    } finally {
      setIsSubmitting(false);
    }
  };

  if (loading) return <div className="text-gray-500 font-medium">Loading teams...</div>;
  if (error) return <div className="text-red-500 font-medium">Error: {error}</div>;
  
  if (!teams || teams.length === 0) {
    return (
      <div className="text-center py-12 bg-white rounded-2xl border border-primary/10 shadow-sm">
        <span className="material-symbols-outlined text-4xl text-gray-300 mb-2">groups</span>
        <p className="text-gray-500 font-medium">You are not part of any teams yet.</p>
      </div>
    );
  }

  return (
    <>
      <div className="grid gap-4 sm:grid-cols-2 lg:grid-cols-3">
        {teams.map((team, i) => (
          <Card key={team.id || i} className="border border-outline-variant/30 hover:border-primary/30 transition-colors">
            <CardContent className="p-5">
              <div className="flex items-start justify-between">
                <div className="flex items-center gap-3">
                  <div className="w-10 h-10 rounded-lg bg-primary-light text-primary flex items-center justify-center font-extrabold text-lg">
                    {team.name ? team.name[0].toUpperCase() : 'T'}
                  </div>
                  <div>
                    <h3 className="font-bold text-gray-900 leading-tight">{team.name}</h3>
                    <p className="text-xs font-semibold text-gray-500">{team.sportType || 'General'}</p>
                  </div>
                </div>
                {team.role === 'CAPTAIN' && (
                  <span className="px-2 py-1 bg-secondary-light text-secondary-dark text-[10px] font-bold uppercase tracking-wider rounded-md">Captain</span>
                )}
              </div>
              
              <div className="mt-4 pt-4 border-t border-gray-100 flex items-center justify-between text-sm">
                <div className="flex items-center gap-1.5 text-gray-600 font-medium">
                  <span className="material-symbols-outlined text-lg">group</span>
                  {team.memberCount || 1} Members
                </div>
                <div className="flex items-center gap-2">
                  {team.role === 'CAPTAIN' && (
                    <>
                      <button 
                        onClick={() => setEditModalData({ isOpen: true, team })}
                        className="text-gray-500 font-bold hover:text-gray-700 transition-colors mr-2"
                      >
                        Edit
                      </button>
                      <button 
                        onClick={() => setInviteModalData({ isOpen: true, teamId: team.id, teamName: team.name })}
                        className="text-accent font-bold hover:text-accent-dark transition-colors"
                      >
                        Invite
                      </button>
                    </>
                  )}
                  <button 
                    onClick={() => setDetailsModalData({ isOpen: true, team })}
                    className="text-primary font-bold hover:text-primary-dark transition-colors"
                  >
                    View
                  </button>
                </div>
              </div>
            </CardContent>
          </Card>
        ))}
      </div>

      <Modal 
        isOpen={inviteModalData.isOpen} 
        onClose={() => setInviteModalData({ isOpen: false, teamId: null, teamName: '' })} 
        title={`Invite to ${inviteModalData.teamName}`}
      >
        <form onSubmit={handleInviteSubmit} className="space-y-4">
          <div>
            <label className="block text-sm font-semibold text-gray-700 mb-1">Email Address</label>
            <input
              type="email"
              required
              className="w-full border border-gray-300 rounded-lg p-3 text-sm focus:ring-2 focus:ring-primary focus:outline-none"
              placeholder="player@example.com"
              value={inviteEmail}
              onChange={(e) => setInviteEmail(e.target.value)}
            />
          </div>
          <div className="flex justify-end gap-3 mt-6">
            <button type="button" onClick={() => setInviteModalData({ isOpen: false, teamId: null, teamName: '' })} className="px-4 py-2 font-bold text-gray-600 hover:bg-gray-50 rounded-lg">Cancel</button>
            <button type="submit" disabled={isSubmitting || !inviteEmail} className="px-4 py-2 font-bold text-white bg-primary hover:bg-primary-dark rounded-lg disabled:opacity-50">
              {isSubmitting ? 'Sending...' : 'Send Invite'}
            </button>
          </div>
        </form>
      </Modal>

      {editModalData.isOpen && editModalData.team && (
        <TeamCreateModal 
          isOpen={editModalData.isOpen}
          onClose={() => setEditModalData({ isOpen: false, team: null })}
          initialData={editModalData.team}
          onSubmit={async (data) => {
            try {
              if (onUpdateTeam) {
                await onUpdateTeam(editModalData.team.id, data);
                addToast('Team updated successfully!', 'success');
                setEditModalData({ isOpen: false, team: null });
                return true;
              }
            } catch (err) {
              addToast(err.message || 'Failed to update team', 'error');
              return false;
            }
          }}
        />
      )}

      {detailsModalData.isOpen && detailsModalData.team && (
        <TeamDetailsModal 
          isOpen={detailsModalData.isOpen}
          onClose={() => setDetailsModalData({ isOpen: false, team: null })}
          team={detailsModalData.team}
          onUpdateTeam={onUpdateTeam}
          onSendInvite={onSendInvite}
        />
      )}
    </>
  );
};

export default TeamList;
