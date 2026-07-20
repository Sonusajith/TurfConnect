import React, { useState, useEffect } from 'react';
import Modal from '../../components/Modal';
import Select from '../../components/ui/Select';
import { useTeams } from '../../hooks/useTeams';

const TournamentRegistrationModal = ({ isOpen, onClose, tournament, onRegister }) => {
  const { teams, fetchTeams, loading } = useTeams();
  const [selectedTeamId, setSelectedTeamId] = useState('');
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [errorMsg, setErrorMsg] = useState('');

  useEffect(() => {
    if (isOpen) {
      fetchTeams();
      setErrorMsg('');
      setSelectedTeamId('');
    }
  }, [isOpen, fetchTeams]);

  // Filter teams by sport if the tournament has a specific sport
  const eligibleTeams = teams?.filter(t => !tournament?.sport || t.sportType === tournament.sport) || [];

  const handleSubmit = async (e) => {
    e.preventDefault();
    if (!selectedTeamId) {
      setErrorMsg('Please select a team to register.');
      return;
    }
    
    setIsSubmitting(true);
    setErrorMsg('');
    try {
      await onRegister(tournament.id, selectedTeamId);
      onClose();
    } catch (err) {
      setErrorMsg(err.message || 'Registration failed. Please try again.');
    } finally {
      setIsSubmitting(false);
    }
  };

  if (!tournament) return null;

  return (
    <Modal isOpen={isOpen} onClose={onClose} title={`Register for ${tournament.name}`}>
      <form onSubmit={handleSubmit} className="space-y-4">
        {errorMsg && (
          <div className="bg-red-50 text-red-600 p-3 rounded-lg text-sm font-semibold">
            {errorMsg}
          </div>
        )}
        
        <div className="bg-[#f4faff] border border-primary/10 rounded-lg p-4 mb-4">
          <p className="text-sm font-semibold text-gray-800">Tournament Details</p>
          <ul className="text-xs text-gray-600 mt-2 space-y-1">
            <li><strong>Sport:</strong> {tournament.sport}</li>
            <li><strong>Dates:</strong> {tournament.startDate} to {tournament.endDate}</li>
            <li><strong>Prize Pool:</strong> {tournament.prizePool}</li>
          </ul>
        </div>

        <div>
          <label className="block text-sm font-semibold text-gray-700 mb-1">Select your Team</label>
          {loading ? (
            <p className="text-sm text-gray-500">Loading your teams...</p>
          ) : eligibleTeams.length === 0 ? (
            <p className="text-sm text-red-500 font-medium">You don't have any eligible {tournament.sport} teams to register. Please create a team first.</p>
          ) : (
            <Select
              value={selectedTeamId}
              onChange={setSelectedTeamId}
              placeholder="-- Choose a Team --"
              options={eligibleTeams.map(team => ({
                value: String(team.id),
                label: `${team.name} (${team.role})`
              }))}
            />
          )}
        </div>

        <div className="flex justify-end gap-3 mt-6 pt-4 border-t border-gray-100">
          <button type="button" onClick={onClose} className="px-4 py-2 font-bold text-gray-600 hover:bg-gray-50 rounded-lg transition-colors">
            Cancel
          </button>
          <button 
            type="submit" 
            disabled={isSubmitting || eligibleTeams.length === 0 || !selectedTeamId} 
            className="px-5 py-2 font-bold text-white bg-primary hover:bg-primary-dark rounded-lg transition-colors disabled:opacity-50"
          >
            {isSubmitting ? 'Registering...' : 'Confirm Registration'}
          </button>
        </div>
      </form>
    </Modal>
  );
};

export default TournamentRegistrationModal;
