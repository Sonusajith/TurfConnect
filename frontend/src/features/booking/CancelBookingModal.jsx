import React, { useState } from 'react';
import Modal from '../../components/Modal';
import Button from '../../components/Button';

const CancelBookingModal = ({ isOpen, onClose, onConfirm, booking }) => {
  const [isConfirming, setIsConfirming] = useState(false);

  if (!booking) return null;

  const handleConfirm = async () => {
    setIsConfirming(true);
    try {
      await onConfirm(booking.id);
      onClose();
    } finally {
      setIsConfirming(false);
    }
  };

  return (
    <Modal isOpen={isOpen} onClose={onClose} title="Cancel Booking">
      <div className="space-y-6">
        <div className="bg-error/10 p-4 rounded-xl border border-error/20 flex gap-3">
          <span className="material-symbols-outlined text-error mt-0.5">warning</span>
          <div>
            <h4 className="font-headline-md text-error text-sm">Cancellation Policy</h4>
            <p className="text-sm text-on-surface-variant mt-1">
              Cancellations made more than 24 hours in advance will receive a full refund. 
              Cancellations within 24 hours are subject to a 50% cancellation fee.
            </p>
          </div>
        </div>

        <div>
          <p className="text-sm text-on-surface-variant mb-3">You are about to cancel the following booking:</p>
          <div className="bg-surface-container-lowest border border-outline-variant/30 rounded-xl p-4">
            <h5 className="font-bold text-on-surface">{booking.turfName}</h5>
            <div className="flex items-center gap-4 mt-2 text-sm text-on-surface-variant">
              <span className="flex items-center gap-1">
                <span className="material-symbols-outlined text-[16px]">calendar_today</span>
                {booking.date}
              </span>
              <span className="flex items-center gap-1">
                <span className="material-symbols-outlined text-[16px]">schedule</span>
                {booking.time}
              </span>
            </div>
            <div className="mt-3 pt-3 border-t border-outline-variant/30 flex justify-between items-center">
              <span className="text-sm font-medium">Estimated Refund:</span>
              <span className="font-bold text-primary">{booking.amount}</span>
            </div>
          </div>
        </div>

        <p className="text-sm text-on-surface font-medium text-center">Are you sure you want to proceed?</p>

        <div className="flex justify-end gap-3 pt-4 border-t border-outline-variant/30">
          <Button variant="outline" onClick={onClose} disabled={isConfirming}>
            Keep Booking
          </Button>
          <Button variant="primary" className="!bg-error hover:!bg-error/90 !text-white" onClick={handleConfirm} isLoading={isConfirming}>
            Yes, Cancel Booking
          </Button>
        </div>
      </div>
    </Modal>
  );
};

export default CancelBookingModal;
