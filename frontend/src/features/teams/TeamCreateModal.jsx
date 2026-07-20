import React, { useState } from 'react';
import Modal from '../../components/Modal';

const TeamCreateModal = ({ isOpen, onClose, onSubmit }) => {
  const [name, setName] = useState('');
  const [sportType, setSportType] = useState('Football');
  const [isSubmitting, setIsSubmitting] = useState(false);

  const handleSubmit = async (e) => {
    e.preventDefault();
    if (!name.trim()) return;
    setIsSubmitting(true);
    const success = await onSubmit({ name, sportType });
    setIsSubmitting(false);
    if (success) {
      setName('');
      onClose();
    }
  };

  return (
    <Modal isOpen={isOpen} onClose={onClose} title="Create a New Team">
      <form onSubmit={handleSubmit} className="space-y-4">
        <div>
          <label className="block text-sm font-semibold text-gray-700 mb-1">Team Name</label>
          <input
            type="text"
            required
            className="w-full border border-gray-300 rounded-lg p-2.5 text-sm focus:ring-2 focus:ring-primary focus:outline-none"
            placeholder="e.g. FC Thunder"
            value={name}
            onChange={(e) => setName(e.target.value)}
          />
        </div>
        <div>
          <label className="block text-sm font-semibold text-gray-700 mb-1">Sport Type</label>
          <select
            className="w-full border border-gray-300 rounded-lg p-2.5 text-sm focus:ring-2 focus:ring-primary focus:outline-none bg-white"
            value={sportType}
            onChange={(e) => setSportType(e.target.value)}
          >
            <option value="Football">Football</option>
            <option value="Cricket">Cricket</option>
            <option value="Badminton">Badminton</option>
            <option value="Basketball">Basketball</option>
          </select>
        </div>
        <div className="flex justify-end gap-3 mt-6 pt-4 border-t border-gray-100">
          <button type="button" onClick={onClose} className="px-4 py-2 font-bold text-gray-600 hover:bg-gray-50 rounded-lg transition-colors">
            Cancel
          </button>
          <button type="submit" disabled={isSubmitting} className="px-5 py-2 font-bold text-white bg-primary hover:bg-primary-dark rounded-lg transition-colors disabled:opacity-50">
            {isSubmitting ? 'Creating...' : 'Create Team'}
          </button>
        </div>
      </form>
    </Modal>
  );
};

export default TeamCreateModal;
