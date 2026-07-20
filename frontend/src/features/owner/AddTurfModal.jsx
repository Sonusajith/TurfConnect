import React, { useState } from 'react';
import Modal from '../../components/Modal';
import Input from '../../components/Input';
import Button from '../../components/Button';

const SPORT_OPTIONS = ['Football', 'Cricket', 'Tennis', 'Badminton', 'Basketball'];

const AddTurfModal = ({ isOpen, onClose, onSubmit }) => {
  const [formData, setFormData] = useState({
    name: '',
    address: '',
    city: '',
    hourlyRate: '',
    currency: 'INR',
    sportTypes: []
  });
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);

  const handleChange = (e) => {
    const { name, value } = e.target;
    setFormData((prev) => ({ ...prev, [name]: value }));
  };

  const handleSportToggle = (sport) => {
    setFormData((prev) => {
      const isSelected = prev.sportTypes.includes(sport);
      if (isSelected) {
        return { ...prev, sportTypes: prev.sportTypes.filter((s) => s !== sport) };
      }
      return { ...prev, sportTypes: [...prev.sportTypes, sport] };
    });
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    if (!formData.name || !formData.address || !formData.city || !formData.hourlyRate) {
      setError('Please fill in all required fields.');
      return;
    }
    if (formData.sportTypes.length === 0) {
      setError('Please select at least one sport.');
      return;
    }

    setError(null);
    setLoading(true);
    try {
      await onSubmit({
        ...formData,
        hourlyRate: Number(formData.hourlyRate),
        status: 'ACTIVE'
      });
      // Reset form
      setFormData({
        name: '',
        address: '',
        city: '',
        hourlyRate: '',
        currency: 'INR',
        sportTypes: []
      });
      onClose();
    } catch (err) {
      setError(err.message || 'Failed to create venue.');
    } finally {
      setLoading(false);
    }
  };

  return (
    <Modal isOpen={isOpen} onClose={onClose} title="Add New Venue">
      <form onSubmit={handleSubmit} className="space-y-5 pt-4">
        {error && <div className="text-sm font-bold text-red-500">{error}</div>}

        <Input
          label="Venue Name"
          name="name"
          placeholder="e.g. Sports Arena 5"
          value={formData.name}
          onChange={handleChange}
          required
        />

        <div className="grid grid-cols-2 gap-4">
          <Input
            label="City"
            name="city"
            placeholder="e.g. Hyderabad"
            value={formData.city}
            onChange={handleChange}
            required
          />
          <Input
            label="Hourly Rate"
            name="hourlyRate"
            type="number"
            min="0"
            placeholder="e.g. 1500"
            value={formData.hourlyRate}
            onChange={handleChange}
            required
          />
        </div>

        <Input
          label="Full Address"
          name="address"
          placeholder="Street, locality, landmark"
          value={formData.address}
          onChange={handleChange}
          required
        />

        <div>
          <label className="mb-2 block text-xs font-extrabold uppercase tracking-wide text-gray-500">
            Available Sports
          </label>
          <div className="flex flex-wrap gap-2">
            {SPORT_OPTIONS.map((sport) => (
              <button
                key={sport}
                type="button"
                onClick={() => handleSportToggle(sport)}
                className={`rounded-lg border px-3 py-1.5 text-sm font-bold transition-all ${
                  formData.sportTypes.includes(sport)
                    ? 'border-primary bg-primary text-white shadow-md'
                    : 'border-gray-200 bg-white text-gray-600 hover:border-gray-300'
                }`}
              >
                {sport}
              </button>
            ))}
          </div>
        </div>

        <div className="mt-8 flex justify-end gap-3 border-t border-gray-100 pt-5">
          <Button type="button" variant="outline" onClick={onClose} disabled={loading}>
            Cancel
          </Button>
          <Button type="submit" variant="primary" isLoading={loading}>
            Create Venue
          </Button>
        </div>
      </form>
    </Modal>
  );
};

export default AddTurfModal;
