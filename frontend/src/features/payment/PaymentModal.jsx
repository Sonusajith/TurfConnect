import React, { useState } from 'react';
import Modal from '../../components/Modal';
import Button from '../../components/Button';
import { formatCurrency } from '../../utils/formatters';

const providerContent = {
  MOCK: {
    body: 'Complete this booking with the local test gateway.',
    button: 'Complete Test Payment',
  },
  RAZORPAY: {
    body: "You will be redirected to Razorpay's secure checkout to complete your payment.",
    button: 'Pay with Razorpay',
  },
};

const PaymentModal = ({ isOpen, onClose, booking, onPaymentComplete, paymentProvider = 'RAZORPAY', onSwitchToMock }) => {
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');
  const [success, setSuccess] = useState(false);

  if (!booking) return null;

  const content = providerContent[paymentProvider] || providerContent.RAZORPAY;

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
        <div className="space-y-4 py-6 text-center">
          <div className="mx-auto flex h-16 w-16 items-center justify-center rounded-full border border-green-200 bg-green-100 text-2xl font-extrabold text-green-600">
            OK
          </div>
          <h2 className="text-xl font-bold text-gray-900">Payment Successful!</h2>
          <p className="text-sm text-gray-500">
            Your booking is confirmed. You can view it under "My Bookings".
          </p>
        </div>
      ) : (
        <form onSubmit={handlePayment} className="space-y-6">
          <div className="flex items-center justify-between rounded-xl border bg-gray-50 p-4">
            <span className="text-sm font-semibold text-gray-600">Total Amount</span>
            <span className="text-2xl font-extrabold text-primary">
              {formatCurrency(booking.totalPrice)}
            </span>
          </div>

          <div className="px-4 text-center">
            <p className="mb-4 text-sm text-gray-500">
              {content.body}
            </p>
          </div>

          {error && (
            <div className="rounded-lg border border-red-100 bg-red-50 p-4">
              <p className="text-sm font-semibold text-red-600 mb-2">Payment Error</p>
              <p className="text-xs text-red-500">{error}</p>
              {paymentProvider !== 'MOCK' && onSwitchToMock && (
                <button
                  type="button"
                  onClick={onSwitchToMock}
                  className="mt-3 text-xs font-bold text-primary hover:text-primary-dark underline"
                >
                  Gateway failing? Retry with Mock Test Payment
                </button>
              )}
            </div>
          )}

          <div className="flex gap-3 pt-2">
            <Button variant="outline" className="flex-1" onClick={onClose} disabled={loading}>
              Cancel
            </Button>
            <Button
              type="submit"
              variant="primary"
              className="flex-1 text-sm font-bold uppercase tracking-wider"
              isLoading={loading}
            >
              {content.button}
            </Button>
          </div>
        </form>
      )}
    </Modal>
  );
};

export default PaymentModal;
