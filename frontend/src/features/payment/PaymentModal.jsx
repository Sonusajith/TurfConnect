import React, { useState } from 'react';
import Modal from '../../components/Modal';
import Button from '../../components/Button';
import Input from '../../components/Input';
import { formatCurrency } from '../../utils/formatters';

const PaymentModal = ({ isOpen, onClose, booking, onPaymentComplete }) => {
  const [cardNumber, setCardNumber] = useState('');
  const [expiry, setExpiry] = useState('');
  const [cvv, setCvv] = useState('');
  const [loading, setLoading] = useState(false);
  const [statusText, setStatusText] = useState('');
  const [error, setError] = useState('');
  const [success, setSuccess] = useState(false);

  if (!booking) return null;

  const handlePayment = async (e) => {
    e.preventDefault();
    if (!cardNumber || !expiry || !cvv) {
      setError('Please fill in all mock card fields.');
      return;
    }

    setLoading(true);
    setError('');
    
    try {
      setStatusText('Initiating payment gateway session...');
      const response = await onPaymentComplete(booking);
      setSuccess(true);
      setStatusText('Payment confirmed!');
    } catch (e) {
      setError(e.message || 'Payment failed. Please retry.');
    } finally {
      setLoading(false);
    }
  };

  return (
    <Modal isOpen={isOpen} onClose={onClose} title="Secure Mock Checkout">
      {success ? (
        <div className="text-center py-6 space-y-4">
          <div className="mx-auto flex items-center justify-center h-16 w-16 rounded-full bg-green-100 border border-green-200 text-green-600 text-3xl">
            ✓
          </div>
          <h2 className="text-xl font-bold text-gray-900">Payment Successful!</h2>
          <p className="text-sm text-gray-500">
            Your booking is confirmed. You can view it under "My Bookings".
          </p>
          <div className="pt-4">
            <Button variant="primary" className="w-full" onClick={onClose}>
              Dismiss
            </Button>
          </div>
        </div>
      ) : (
        <form onSubmit={handlePayment} className="space-y-5">
          <div className="flex justify-between items-center bg-gray-50 rounded-xl p-4 border">
            <span className="text-gray-600 text-sm font-semibold">Amount Due</span>
            <span className="text-primary font-extrabold text-lg">
              {formatCurrency(booking.totalPrice)}
            </span>
          </div>

          <div className="space-y-4">
            <Input
              label="Card Number (Mock)"
              id="cardNumber"
              placeholder="4111 2222 3333 4444"
              maxLength="19"
              value={cardNumber}
              onChange={(e) => setCardNumber(e.target.value)}
              disabled={loading}
            />

            <div className="grid grid-cols-2 gap-4">
              <Input
                label="Expiry Date"
                id="cardExpiry"
                placeholder="MM/YY"
                maxLength="5"
                value={expiry}
                onChange={(e) => setExpiry(e.target.value)}
                disabled={loading}
              />
              <Input
                label="CVV"
                id="cardCvv"
                type="password"
                placeholder="***"
                maxLength="3"
                value={cvv}
                onChange={(e) => setCvv(e.target.value)}
                disabled={loading}
              />
            </div>
          </div>

          {loading && (
            <div className="text-center py-2 text-xs font-semibold text-primary animate-pulse">
              ⚙️ {statusText}
            </div>
          )}

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
              variant="secondary"
              className="flex-1 font-bold uppercase tracking-wider text-xs"
              isLoading={loading}
            >
              Simulate Pay
            </Button>
          </div>
        </form>
      )}
    </Modal>
  );
};

export default PaymentModal;
