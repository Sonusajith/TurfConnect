import React from 'react';
import Modal from '../../components/Modal';

const TournamentDetailsModal = ({ isOpen, onClose, tournament }) => {
  if (!tournament) return null;

  return (
    <Modal isOpen={isOpen} onClose={onClose} title={tournament.name}>
      <div className="space-y-6">
        <div className="bg-gradient-to-br from-primary via-primary-dark to-secondary p-6 rounded-lg text-white shadow-inner relative overflow-hidden">
          <div className="absolute inset-0 opacity-10 bg-[url('data:image/svg+xml;base64,PHN2ZyB3aWR0aD0iMjAiIGhlaWdodD0iMjAiIHhtbG5zPSJodHRwOi8vd3d3LnczLm9yZy8yMDAwL3N2ZyI+PGNpcmNsZSBjeD0iMiIgY3k9IjIiIHI9IjIiIGZpbGw9IiNmZmZmZmYiLz48L3N2Zz4=')] bg-[length:20px_20px]"></div>
          <div className="relative z-10">
            <h3 className="text-2xl font-headline-lg font-bold drop-shadow-md">{tournament.name}</h3>
            <p className="text-white/80 font-medium mt-1">{tournament.sport} Tournament</p>
          </div>
          <span className="material-symbols-outlined text-white/20 text-8xl absolute -right-4 -bottom-8 transform rotate-12">emoji_events</span>
        </div>

        <div className="grid grid-cols-2 gap-4">
          <div className="bg-[#f4faff] border border-primary/10 p-4 rounded-lg">
            <p className="text-xs font-bold text-gray-500 uppercase tracking-wider mb-1">Schedule</p>
            <p className="text-sm font-semibold text-gray-900">{tournament.startDate}</p>
            <p className="text-sm font-semibold text-gray-900">to {tournament.endDate}</p>
          </div>
          <div className="bg-[#f4faff] border border-primary/10 p-4 rounded-lg">
            <p className="text-xs font-bold text-gray-500 uppercase tracking-wider mb-1">Prize Pool</p>
            <p className="text-xl font-extrabold text-primary">{tournament.prizePool}</p>
          </div>
        </div>

        <div>
          <h4 className="text-sm font-extrabold text-gray-900 mb-2 border-b pb-2">Tournament Information</h4>
          <ul className="space-y-3 text-sm text-gray-700">
            <li className="flex items-start gap-2">
              <span className="material-symbols-outlined text-primary text-[18px] mt-0.5">groups</span>
              <div>
                <span className="font-bold">Teams Registered:</span> {tournament.teamsRegistered} / {tournament.maxTeams}
                {tournament.teamsRegistered >= tournament.maxTeams && <span className="ml-2 text-xs font-bold text-red-500 bg-red-50 px-2 py-0.5 rounded">(Full)</span>}
              </div>
            </li>
            <li className="flex items-start gap-2">
              <span className="material-symbols-outlined text-primary text-[18px] mt-0.5">sports_soccer</span>
              <div>
                <span className="font-bold">Sport Format:</span> Standard {tournament.sport} Rules
              </div>
            </li>
            <li className="flex items-start gap-2">
              <span className="material-symbols-outlined text-primary text-[18px] mt-0.5">info</span>
              <div>
                <span className="font-bold">Status:</span> {tournament.status.replace(/_/g, ' ')}
              </div>
            </li>
          </ul>
        </div>

        <div className="bg-yellow-50 border border-yellow-200 p-4 rounded-lg flex items-start gap-3">
          <span className="material-symbols-outlined text-yellow-600 mt-0.5">warning</span>
          <p className="text-xs text-yellow-800 font-medium">
            Only Team Captains can register their team for this tournament. Ensure your team roster is complete before the start date.
          </p>
        </div>

        <div className="flex justify-end pt-4 border-t border-gray-100">
          <button type="button" onClick={onClose} className="px-5 py-2 font-bold text-white bg-primary hover:bg-primary-dark rounded-lg transition-colors">
            Close
          </button>
        </div>
      </div>
    </Modal>
  );
};

export default TournamentDetailsModal;
