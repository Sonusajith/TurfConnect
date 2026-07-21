import React, { useMemo, useState } from 'react';
import Modal from '../../components/Modal';
import Input from '../../components/Input';
import Button from '../../components/Button';

const SPORT_OPTIONS = [
  { value: 'FOOTBALL', label: 'Football' },
  { value: 'CRICKET', label: 'Cricket' },
  { value: 'TENNIS', label: 'Tennis' },
  { value: 'BADMINTON', label: 'Badminton' },
  { value: 'BASKETBALL', label: 'Basketball' },
];

const AMENITY_OPTIONS = [
  { key: 'floodlightsAvailable', label: 'Floodlights' },
  { key: 'parkingAvailable', label: 'Parking' },
  { key: 'changingRoomsAvailable', label: 'Changing Rooms' },
  { key: 'washroomsAvailable', label: 'Washrooms' },
  { key: 'drinkingWaterAvailable', label: 'Drinking Water' },
  { key: 'equipmentRentalAvailable', label: 'Equipment Rental' },
  { key: 'foodAvailable', label: 'Food Counter' },
  { key: 'coachingAvailable', label: 'Coaching' },
];

const DEFAULT_DAYS = ['MONDAY', 'TUESDAY', 'WEDNESDAY', 'THURSDAY', 'FRIDAY', 'SATURDAY', 'SUNDAY'];

const AddTurfModal = ({ isOpen, onClose, onSubmit }) => {
  const initialFormData = useMemo(() => ({
    name: '',
    description: '',
    address: '',
    city: '',
    state: '',
    postalCode: '',
    country: 'India',
    longitude: '77.5946',
    latitude: '12.9716',
    timezone: 'Asia/Kolkata',
    hourlyRate: '',
    currency: 'INR',
    sportTypes: [],
    openTime: '06:00',
    closeTime: '22:00',
    slotDurationMinutes: '60',
    contactNumber: '',
    email: '',
    capacity: '10',
    surfaceType: 'Artificial Turf',
    indoorOrOutdoor: 'OUTDOOR',
    floodlightsAvailable: true,
    parkingAvailable: true,
    changingRoomsAvailable: false,
    washroomsAvailable: true,
    drinkingWaterAvailable: true,
    equipmentRentalAvailable: false,
    foodAvailable: false,
    coachingAvailable: false,
  }), []);

  const [formData, setFormData] = useState({
    ...initialFormData,
  });
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);

  const handleChange = (e) => {
    const { name, type, checked, value } = e.target;
    setFormData((prev) => ({ ...prev, [name]: type === 'checkbox' ? checked : value }));
  };

  const handleSportToggle = (sportValue) => {
    setFormData((prev) => {
      const isSelected = prev.sportTypes.includes(sportValue);
      if (isSelected) {
        return { ...prev, sportTypes: prev.sportTypes.filter((sport) => sport !== sportValue) };
      }
      return { ...prev, sportTypes: [...prev.sportTypes, sportValue] };
    });
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    if (!formData.name || !formData.address || !formData.city || !formData.hourlyRate || !formData.contactNumber) {
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
        longitude: Number(formData.longitude),
        latitude: Number(formData.latitude),
        slotDurationMinutes: Number(formData.slotDurationMinutes),
        capacity: Number(formData.capacity),
        availableDays: DEFAULT_DAYS,
        amenities: AMENITY_OPTIONS.filter((option) => formData[option.key]).map((option) => option.label),
      });
      setFormData({
        ...initialFormData,
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

        <Input
          label="Description"
          name="description"
          placeholder="Short venue description"
          value={formData.description}
          onChange={handleChange}
        />

        <div className="grid gap-4 sm:grid-cols-2">
          <Input
            label="City"
            name="city"
            placeholder="e.g. Hyderabad"
            value={formData.city}
            onChange={handleChange}
            required
          />
          <Input
            label="State"
            name="state"
            placeholder="e.g. Telangana"
            value={formData.state}
            onChange={handleChange}
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

        <div className="grid gap-4 sm:grid-cols-2">
          <Input
            label="Postal Code"
            name="postalCode"
            placeholder="e.g. 500081"
            value={formData.postalCode}
            onChange={handleChange}
          />
          <Input
            label="Contact Number"
            name="contactNumber"
            placeholder="e.g. 9876543210"
            value={formData.contactNumber}
            onChange={handleChange}
            required
          />
        </div>

        <div className="grid gap-4 sm:grid-cols-2">
          <Input
            label="Email"
            name="email"
            type="email"
            placeholder="owner@example.com"
            value={formData.email}
            onChange={handleChange}
          />
          <Input
            label="Hourly Rate"
            name="hourlyRate"
            type="number"
            min="1"
            placeholder="e.g. 1500"
            value={formData.hourlyRate}
            onChange={handleChange}
            required
          />
        </div>

        <div className="grid gap-4 sm:grid-cols-2">
          <Input
            label="Open Time"
            name="openTime"
            type="time"
            value={formData.openTime}
            onChange={handleChange}
            required
          />
          <Input
            label="Close Time"
            name="closeTime"
            type="time"
            value={formData.closeTime}
            onChange={handleChange}
            required
          />
        </div>

        <div className="grid gap-4 sm:grid-cols-2">
          <Input
            label="Slot Duration"
            name="slotDurationMinutes"
            type="number"
            min="5"
            max="1440"
            value={formData.slotDurationMinutes}
            onChange={handleChange}
            required
          />
          <Input
            label="Capacity"
            name="capacity"
            type="number"
            min="1"
            value={formData.capacity}
            onChange={handleChange}
          />
        </div>

        <div className="grid gap-4 sm:grid-cols-2">
          <Input
            label="Longitude"
            name="longitude"
            type="number"
            step="0.0001"
            min="-180"
            max="180"
            value={formData.longitude}
            onChange={handleChange}
            required
          />
          <Input
            label="Latitude"
            name="latitude"
            type="number"
            step="0.0001"
            min="-90"
            max="90"
            value={formData.latitude}
            onChange={handleChange}
            required
          />
        </div>

        <div className="grid gap-4 sm:grid-cols-2">
          <label className="flex flex-col gap-1.5 text-sm font-extrabold tracking-wide text-gray-700">
            Surface Type
            <select
              name="surfaceType"
              value={formData.surfaceType}
              onChange={handleChange}
              className="w-full rounded-lg border border-gray-300 bg-[#eaf6ff] px-4 py-3 text-gray-900 focus:border-primary focus:outline-none focus:ring-2 focus:ring-primary-light"
            >
              <option value="Artificial Turf">Artificial Turf</option>
              <option value="Synthetic Grass">Synthetic Grass</option>
              <option value="Matting">Matting</option>
              <option value="Wooden Court">Wooden Court</option>
              <option value="Clay Court">Clay Court</option>
            </select>
          </label>
          <label className="flex flex-col gap-1.5 text-sm font-extrabold tracking-wide text-gray-700">
            Venue Type
            <select
              name="indoorOrOutdoor"
              value={formData.indoorOrOutdoor}
              onChange={handleChange}
              className="w-full rounded-lg border border-gray-300 bg-[#eaf6ff] px-4 py-3 text-gray-900 focus:border-primary focus:outline-none focus:ring-2 focus:ring-primary-light"
            >
              <option value="OUTDOOR">Outdoor</option>
              <option value="INDOOR">Indoor</option>
            </select>
          </label>
        </div>

        <div>
          <label className="mb-2 block text-xs font-extrabold uppercase tracking-wide text-gray-500">
            Available Sports
          </label>
          <div className="flex flex-wrap gap-2">
            {SPORT_OPTIONS.map((sport) => (
              <button
                key={sport.value}
                type="button"
                onClick={() => handleSportToggle(sport.value)}
                className={`rounded-lg border px-3 py-1.5 text-sm font-bold transition-all ${
                  formData.sportTypes.includes(sport.value)
                    ? 'border-primary bg-primary text-white shadow-md'
                    : 'border-gray-200 bg-white text-gray-600 hover:border-gray-300'
                }`}
              >
                {sport.label}
              </button>
            ))}
          </div>
        </div>

        <div>
          <label className="mb-2 block text-xs font-extrabold uppercase tracking-wide text-gray-500">
            Amenities
          </label>
          <div className="grid gap-2 sm:grid-cols-2">
            {AMENITY_OPTIONS.map((option) => (
              <label key={option.key} className="flex items-center gap-2 rounded-lg border border-gray-200 bg-white px-3 py-2 text-sm font-semibold text-gray-700">
                <input
                  type="checkbox"
                  name={option.key}
                  checked={formData[option.key]}
                  onChange={handleChange}
                  className="h-4 w-4 rounded border-gray-300 text-primary focus:ring-primary"
                />
                {option.label}
              </label>
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
