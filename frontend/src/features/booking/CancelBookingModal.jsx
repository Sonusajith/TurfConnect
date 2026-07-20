import React, { useState } from 'react';
import Modal from '../../components/Modal';
import Button from '../../components/Button';
import { formatCurrency, formatDate, formatTime } from '../../utils/formatters';

const CancelBookingModal = ({ isOpen, onClose, onConfirm, booking }) => {
  const [isConfirming, setIsConfirming] = useState(false);
  const [error, setError] = useState('');

  if (!booking) return null;

  const handleConfirm = async () => {
    setIsConfirming(true);
    setError('');

    try {
      await onConfirm(booking.id);
      onClose();
    } catch (e) {
      setError(e.message || 'Failed to cancel booking');
    } finally {
      setIsConfirming(false);
    }
  };

  return (
    <Modal isOpen={isOpen} onClose={onClose} title="Cancel Booking">
      <div className="space-y-6">
        <div className="flex gap-3 rounded-xl border border-red-100 bg-red-50 p-4">
          <span className="material-symbols-outlined mt-0.5 text-red-600">warning</span>
          <div>
            <h4 className="text-sm font-extrabold text-red-700">Cancellation and refund</h4>
            <p className="mt-1 text-sm font-medium text-red-600">
              Cancelling releases the slot. If a successful payment exists, the backend will trigger the refund flow.
            </p>
          </div>
        </div>

        <div className="rounded-xl border border-primary/10 bg-[#f4faff] p-4">
          <h5 className="break-all font-mono text-sm font-extrabold text-gray-900">{booking.id}</h5>
          <div className="mt-3 grid gap-3 text-sm sm:grid-cols-2">
            <Info label="Date" value={formatDate(booking.date)} />
            <Info label="Time" value={`${formatTime(booking.startTime)} - ${formatTime(booking.endTime)}`} />
            <Info label="Amount" value={formatCurrency(booking.totalPrice)} />
            <Info label="Status" value={booking.status} />
          </div>
        </div>

        {error && (
          <div className="rounded-lg border border-red-100 bg-red-50 p-3 text-xs font-semibold text-red-600">
            {error}
          </div>
        )}

        <p className="text-center text-sm font-semibold text-gray-700">Are you sure you want to cancel this booking?</p>

        <div className="flex justify-end gap-3 border-t border-primary/10 pt-4">
          <Button variant="outline" onClick={onClose} disabled={isConfirming}>
            Keep Booking
          </Button>
          <Button variant="danger" onClick={handleConfirm} isLoading={isConfirming}>
            Yes, Cancel
          </Button>
        </div>
      </div>
    </Modal>
  );
};

const Info = ({ label, value }) => (
  <div>
    <p className="text-[10px] font-bold uppercase tracking-wide text-gray-400">{label}</p>
    <p className="mt-1 font-bold text-gray-900">{value}</p>
  </div>
);

export default CancelBookingModal;
