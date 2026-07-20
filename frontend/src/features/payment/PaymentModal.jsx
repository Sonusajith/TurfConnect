import React, { useState } from 'react';
import Modal from '../../components/Modal';
import Button from '../../components/Button';
import { formatCurrency } from '../../utils/formatters';

const PaymentModal = ({ isOpen, onClose, booking, onPaymentComplete }) => {
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');
  const [success, setSuccess] = useState(false);

  if (!booking) return null;

  const handlePayment = async (e) => {
    e.preventDefault();
    setLoading(true);
    setError('');
    
    try {
      await onPaymentComplete(booking);
      setSuccess(true);
    } catch (e) {
      setError(e.message || 'Payment failed. Please retry.');
    } finally {
      setLoading(false);
    }
  };

  return (
    <Modal isOpen={isOpen} onClose={onClose} title="Complete Booking">
      {success ? (
        <div className="text-center py-6 space-y-4">
          <div className="mx-auto flex items-center justify-center h-16 w-16 rounded-full bg-green-100 border border-green-200 text-green-600 text-3xl">
            ✓
          </div>
          <h2 className="text-xl font-bold text-gray-900">Payment Successful!</h2>
          <p className="text-sm text-gray-500">
            Your booking is confirmed. You can view it under "My Bookings".
          </p>
        </div>
      ) : (
        <form onSubmit={handlePayment} className="space-y-6">
          <div className="flex justify-between items-center bg-gray-50 rounded-xl p-4 border">
            <span className="text-gray-600 text-sm font-semibold">Total Amount</span>
            <span className="text-primary font-extrabold text-2xl">
              {formatCurrency(booking.totalPrice)}
            </span>
          </div>
          
          <div className="text-center px-4">
            <p className="text-sm text-gray-500 mb-4">
              You will be redirected to Razorpay's secure checkout to complete your payment.
            </p>
          </div>

          {error && (
            <div className="p-3 bg-red-50 text-red-600 text-xs font-semibold rounded-lg border border-red-100">
              ⚠️ {error}
            </div>
          )}

          <div className="flex gap-3 pt-2">
            <Button variant="outline" className="flex-1" onClick={onClose} disabled={loading}>
              Cancel
            </Button>
            <Button
              type="submit"
              variant="primary"
              className="flex-1 font-bold uppercase tracking-wider text-sm"
              isLoading={loading}
            >
              Pay with Razorpay
            </Button>
          </div>
        </form>
      )}
    </Modal>
  );
};

export default PaymentModal;
