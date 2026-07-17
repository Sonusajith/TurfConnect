import React, { useState } from 'react';
import Modal from '../../components/Modal';
import Button from '../../components/Button';
import { formatCurrency, formatTime, formatDate } from '../../utils/formatters';

const CheckoutModal = ({ isOpen, onClose, slot, turf, onConfirm }) => {
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');

  if (!slot || !turf) return null;

  const handleCheckout = async () => {
    setLoading(true);
    setError('');
    try {
      await onConfirm(slot, turf);
    } catch (e) {
      setError(e.message || 'Checkout failed. Please try again.');
    } finally {
      setLoading(false);
    }
  };

  return (
    <Modal isOpen={isOpen} onClose={onClose} title="Checkout Confirmation">
      <div className="space-y-5">
        <div className="bg-primary-light rounded-xl p-4 border border-green-100 flex flex-col gap-2">
          <div className="flex justify-between items-start">
            <span className="text-gray-500 font-semibold text-xs uppercase tracking-wide">Arena</span>
            <span className="text-gray-900 font-bold text-sm">{turf.name}</span>
          </div>
          <div className="flex justify-between items-start">
            <span className="text-gray-500 font-semibold text-xs uppercase tracking-wide">Date</span>
            <span className="text-gray-900 font-bold text-sm">{formatDate(slot.date)}</span>
          </div>
          <div className="flex justify-between items-start">
            <span className="text-gray-500 font-semibold text-xs uppercase tracking-wide">Time Slot</span>
            <span className="text-gray-900 font-bold text-sm">
              {formatTime(slot.startTime)} - {formatTime(slot.endTime)}
            </span>
          </div>
        </div>

        <div className="border-t border-dashed pt-4 flex justify-between items-center">
          <span className="text-gray-700 font-bold text-base">Total Amount</span>
          <span className="text-primary font-extrabold text-xl">
            {formatCurrency(slot.price)}
          </span>
        </div>

        {error && (
          <div className="p-3 bg-red-50 text-red-600 text-xs font-semibold rounded-lg border border-red-100">
            ⚠️ {error}
          </div>
        )}

        <div className="flex gap-3">
          <Button variant="outline" className="flex-1" onClick={onClose} disabled={loading}>
            Cancel
          </Button>
          <Button
            variant="accent"
            className="flex-1 font-bold uppercase tracking-wider text-xs"
            onClick={handleCheckout}
            isLoading={loading}
          >
            Confirm & Pay
          </Button>
        </div>
      </div>
    </Modal>
  );
};

export default CheckoutModal;
