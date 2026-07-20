import React, { useState } from 'react';
import Modal from '../../components/Modal';

const TeamDetailsModal = ({ isOpen, onClose, team, onUpdateTeam }) => {
  const [isEditing, setIsEditing] = useState(false);
  const [members, setMembers] = useState(team?.members || []);
  const [copied, setCopied] = useState(false);

  // Update members state when team changes
  React.useEffect(() => {
    setMembers(team?.members || []);
    setIsEditing(false);
    setCopied(false);
  }, [team]);

  if (!team) return null;

  const handleRemoveMember = (memberId) => {
    setMembers(members.filter(m => m.userId !== memberId));
  };

  const handleSave = async () => {
    if (onUpdateTeam) {
      // Assuming onUpdateTeam takes the team id and the updated data
      const success = await onUpdateTeam(team.id, { ...team, members });
      if (success) {
        setIsEditing(false);
      }
    } else {
      setIsEditing(false);
    }
  };

  const inviteLink = `${window.location.origin}/join-team/${team?.id}`;
  
  const handleCopyLink = () => {
    navigator.clipboard.writeText(inviteLink);
    setCopied(true);
    setTimeout(() => setCopied(false), 2000);
  };

  return (
    <Modal isOpen={isOpen} onClose={onClose} title={`${team.name} Details`}>
      <div className="space-y-6">
        <div className="bg-[#f4faff] border border-primary/10 p-4 rounded-lg flex items-center justify-between">
          <div>
            <h3 className="font-bold text-gray-900 text-lg">{team.name}</h3>
            <p className="text-sm font-medium text-gray-600">{team.sportType || 'General'} • {members.length} Player{members.length !== 1 ? 's' : ''}</p>
          </div>
          <div className="w-12 h-12 rounded-full bg-primary-light text-primary flex items-center justify-center font-extrabold text-2xl shadow-sm">
            {team.name ? team.name[0].toUpperCase() : 'T'}
          </div>
        </div>

        <div>
          <div className="flex justify-between items-center mb-3 border-b pb-2">
            <h4 className="text-sm font-extrabold text-gray-900">Team Roster</h4>
            {team.role === 'CAPTAIN' && !isEditing && (
              <button 
                onClick={() => setIsEditing(true)}
                className="text-xs font-bold text-primary hover:text-primary-dark transition-colors flex items-center gap-1"
              >
                <span className="material-symbols-outlined text-[16px]">edit</span> Edit Roster
              </button>
            )}
            {team.role === 'CAPTAIN' && isEditing && (
              <button 
                onClick={handleSave}
                className="text-xs font-bold text-green-600 hover:text-green-700 transition-colors flex items-center gap-1"
              >
                <span className="material-symbols-outlined text-[16px]">save</span> Save Changes
              </button>
            )}
          </div>
          
          <div className="space-y-2 max-h-60 overflow-y-auto pr-2">
            {members.length === 0 ? (
              <p className="text-sm text-gray-500 italic text-center py-4">No members found.</p>
            ) : (
              members.map((member, idx) => (
                <div key={member.userId || idx} className="flex items-center justify-between p-3 bg-white border border-gray-100 rounded-lg shadow-sm">
                  <div className="flex items-center gap-3">
                    <div className="w-8 h-8 rounded-full bg-gray-200 text-gray-600 flex items-center justify-center text-xs font-bold">
                      {(member.name || member.email || 'P')[0].toUpperCase()}
                    </div>
                    <div>
                      <p className="text-sm font-bold text-gray-900">{member.name || member.email || `Player ${idx + 1}`}</p>
                      <p className="text-xs font-semibold text-gray-500">{member.role === 'CAPTAIN' ? 'Captain' : 'Player'}</p>
                    </div>
                  </div>
                  
                  {isEditing && member.role !== 'CAPTAIN' && (
                    <button 
                      onClick={() => handleRemoveMember(member.userId)}
                      className="text-red-500 hover:bg-red-50 p-1.5 rounded-lg transition-colors"
                      title="Remove Player"
                    >
                      <span className="material-symbols-outlined text-[18px]">person_remove</span>
                    </button>
                  )}
                  {!isEditing && member.role === 'CAPTAIN' && (
                     <span className="px-2 py-0.5 bg-secondary-light text-secondary-dark text-[10px] font-bold uppercase tracking-wider rounded-md">Captain</span>
                  )}
                </div>
              ))
            )}
          </div>
        </div>

        {team.role === 'CAPTAIN' && (
          <div className="bg-gray-50 border border-gray-200 p-4 rounded-lg mt-4">
            <h5 className="text-sm font-bold text-gray-900 mb-2 flex items-center gap-2">
              <span className="material-symbols-outlined text-[18px] text-primary">share</span>
              Shareable Invite Link
            </h5>
            <p className="text-xs text-gray-500 mb-3">Copy this link and share it on WhatsApp, Instagram, or anywhere else. Players can join just by clicking it!</p>
            <div className="flex gap-2">
              <input 
                type="text" 
                readOnly
                value={inviteLink}
                className="flex-1 bg-white border border-gray-300 rounded-lg p-2 text-xs text-gray-600 focus:outline-none"
              />
              <button 
                type="button" 
                onClick={handleCopyLink}
                className="px-4 py-2 font-bold text-white bg-primary hover:bg-primary-dark rounded-lg transition-colors flex items-center gap-1 text-sm whitespace-nowrap"
              >
                <span className="material-symbols-outlined text-[16px]">{copied ? 'check' : 'content_copy'}</span>
                {copied ? 'Copied!' : 'Copy Link'}
              </button>
            </div>
          </div>
        )}

        <div className="flex justify-end pt-4 border-t border-gray-100">
          <button type="button" onClick={onClose} className="px-5 py-2 font-bold text-gray-700 bg-gray-100 hover:bg-gray-200 rounded-lg transition-colors">
            Close
          </button>
        </div>
      </div>
    </Modal>
  );
};

export default TeamDetailsModal;
