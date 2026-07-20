import React, { useState, useEffect } from 'react';
import Modal from '../../components/Modal';
import Select from '../../components/ui/Select';

const TeamCreateModal = ({ isOpen, onClose, onSubmit, initialData = null }) => {
  const [name, setName] = useState(initialData?.name || '');
  const [sportType, setSportType] = useState(initialData?.sportType || 'Football');
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [errorMsg, setErrorMsg] = useState('');

  useEffect(() => {
    if (isOpen) {
      setName(initialData?.name || '');
      setSportType(initialData?.sportType || 'Football');
      setErrorMsg('');
    }
  }, [isOpen, initialData]);

  const handleSubmit = async (e) => {
    e.preventDefault();
    if (!name.trim()) return;
    setIsSubmitting(true);
    setErrorMsg('');
    try {
      const success = await onSubmit({ name, sportType });
      if (success) {
        setName('');
        setErrorMsg('');
        onClose();
      } else {
        setErrorMsg(initialData ? 'Failed to save changes. Please try again.' : 'Failed to create team. Please try again.');
      }
    } catch (err) {
      setErrorMsg(err.message || 'Failed to save team. Please try again.');
    } finally {
      setIsSubmitting(false);
    }
  };

  return (
    <Modal isOpen={isOpen} onClose={() => { setErrorMsg(''); onClose(); }} title={initialData ? "Edit Team" : "Create a New Team"}>
      <form onSubmit={handleSubmit} className="space-y-4">
        {errorMsg && (
          <div className="bg-red-50 text-red-600 p-3 rounded-lg text-sm font-semibold">
            {errorMsg}
          </div>
        )}
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
          <Select
            value={sportType}
            onChange={setSportType}
            options={[
              { value: 'Football', label: 'Football' },
              { value: 'Cricket', label: 'Cricket' },
              { value: 'Badminton', label: 'Badminton' },
              { value: 'Basketball', label: 'Basketball' },
            ]}
          />
        </div>
        <div className="flex justify-end gap-3 mt-6 pt-4 border-t border-gray-100">
          <button type="button" onClick={() => { setErrorMsg(''); onClose(); }} className="px-4 py-2 font-bold text-gray-600 hover:bg-gray-50 rounded-lg transition-colors">
            Cancel
          </button>
          <button type="submit" disabled={isSubmitting} className="px-5 py-2 font-bold text-white bg-primary hover:bg-primary-dark rounded-lg transition-colors disabled:opacity-50">
            {isSubmitting ? (initialData ? 'Saving...' : 'Creating...') : (initialData ? 'Save Changes' : 'Create Team')}
          </button>
        </div>
      </form>
    </Modal>
  );
};

export default TeamCreateModal;
