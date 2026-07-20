import React, { useState } from 'react';
import Modal from '../../components/Modal';
import Button from '../../components/Button';
import { formatCurrency, formatTime, formatDate } from '../../utils/formatters';

const CheckoutModal = ({ isOpen, onClose, slot, turf, onConfirm }) => {
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');
  const [memberCount, setMemberCount] = useState(6);

  if (!slot || !turf) return null;

  const totalAmount = Number(slot.price || 0);
  const contributionAmount = memberCount > 0 ? totalAmount / memberCount : totalAmount;
  const teammateCount = Math.max(memberCount - 1, 0);

  const updateMemberCount = (value) => {
    const parsedValue = Number(value);
    if (Number.isNaN(parsedValue)) return;
    setMemberCount(Math.min(30, Math.max(1, parsedValue)));
  };

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
        <div className="flex flex-col gap-2 rounded-xl border border-green-100 bg-primary-light p-4">
          <div className="flex items-start justify-between">
            <span className="text-xs font-semibold uppercase tracking-wide text-gray-500">Arena</span>
            <span className="text-sm font-bold text-gray-900">{turf.name}</span>
          </div>
          <div className="flex items-start justify-between">
            <span className="text-xs font-semibold uppercase tracking-wide text-gray-500">Date</span>
            <span className="text-sm font-bold text-gray-900">{formatDate(slot.date)}</span>
          </div>
          <div className="flex items-start justify-between">
            <span className="text-xs font-semibold uppercase tracking-wide text-gray-500">Time Slot</span>
            <span className="text-sm font-bold text-gray-900">
              {formatTime(slot.startTime)} - {formatTime(slot.endTime)}
            </span>
          </div>
        </div>

        <div className="flex items-center justify-between border-t border-dashed pt-4">
          <span className="text-base font-bold text-gray-700">Total Amount</span>
          <span className="text-xl font-extrabold text-primary">
            {formatCurrency(slot.price)}
          </span>
        </div>

        <div className="space-y-4 rounded-xl border border-orange-100 bg-orange-50/70 p-4">
          <div className="flex flex-col gap-3 sm:flex-row sm:items-center sm:justify-between">
            <div>
              <h4 className="text-sm font-extrabold text-gray-900">Team contribution split</h4>
              <p className="text-xs font-medium text-gray-500">
                {memberCount === 1
                  ? 'Solo booking, you cover the full slot amount.'
                  : `You + ${teammateCount} teammate${teammateCount > 1 ? 's' : ''}`}
              </p>
            </div>

            <div className="flex items-center gap-2">
              <Button
                type="button"
                variant="outline"
                className="h-9 w-9 px-0"
                onClick={() => updateMemberCount(memberCount - 1)}
                disabled={loading || memberCount <= 1}
                aria-label="Decrease members"
              >
                -
              </Button>
              <input
                type="number"
                min="1"
                max="30"
                value={memberCount}
                onChange={(e) => updateMemberCount(e.target.value)}
                disabled={loading}
                aria-label="Number of members"
                className="h-9 w-16 rounded-lg border border-orange-200 bg-white text-center text-sm font-bold text-gray-900 outline-none focus:border-accent focus:ring-2 focus:ring-orange-100"
              />
              <Button
                type="button"
                variant="outline"
                className="h-9 w-9 px-0"
                onClick={() => updateMemberCount(memberCount + 1)}
                disabled={loading || memberCount >= 30}
                aria-label="Increase members"
              >
                +
              </Button>
            </div>
          </div>

          <div className="grid grid-cols-1 gap-3 sm:grid-cols-2">
            <div className="rounded-lg border border-orange-100 bg-white/80 p-3">
              <span className="block text-[10px] font-bold uppercase tracking-wide text-gray-400">Members</span>
              <span className="text-lg font-extrabold text-gray-900">{memberCount}</span>
            </div>
            <div className="rounded-lg border border-orange-100 bg-white/80 p-3">
              <span className="block text-[10px] font-bold uppercase tracking-wide text-gray-400">Each contributes</span>
              <span className="text-lg font-extrabold text-accent">
                {formatCurrency(contributionAmount)}
              </span>
            </div>
          </div>
        </div>

        {error && (
          <div className="rounded-lg border border-red-100 bg-red-50 p-3 text-xs font-semibold text-red-600">
            Error: {error}
          </div>
        )}

        <div className="flex gap-3">
          <Button variant="outline" className="flex-1" onClick={onClose} disabled={loading}>
            Cancel
          </Button>
          <Button
            variant="accent"
            className="flex-1 text-xs font-bold uppercase tracking-wider"
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
