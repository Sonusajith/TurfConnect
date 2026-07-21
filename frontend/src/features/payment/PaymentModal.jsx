import React, { useState } from 'react';
import Modal from '../../components/Modal';
import Button from '../../components/Button';
import { formatCurrency } from '../../utils/formatters';

const UPI_QR_IMAGE = '/turfconnect-upi-qr.jpeg';
const DEMO_CARD = {
  number: '4111 1111 1111 1111',
  expiry: '12/30',
  cvv: '123',
  name: 'TURFCONNECT DEMO',
  otp: '123456',
};

const providerContent = {
  MOCK: {
    body: 'Complete this booking with the local test gateway.',
    button: 'Complete Demo Payment',
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
  const [paymentMethod, setPaymentMethod] = useState('UPI');

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

          <div className="space-y-4 rounded-xl border border-primary/10 bg-white p-4">
            <div className="grid grid-cols-2 gap-2 rounded-lg bg-gray-100 p-1">
              <button
                type="button"
                onClick={() => setPaymentMethod('UPI')}
                className={`rounded-md px-3 py-2 text-sm font-extrabold transition ${
                  paymentMethod === 'UPI'
                    ? 'bg-white text-primary shadow-sm'
                    : 'text-gray-500 hover:text-gray-900'
                }`}
              >
                UPI QR
              </button>
              <button
                type="button"
                onClick={() => setPaymentMethod('CARD')}
                className={`rounded-md px-3 py-2 text-sm font-extrabold transition ${
                  paymentMethod === 'CARD'
                    ? 'bg-white text-primary shadow-sm'
                    : 'text-gray-500 hover:text-gray-900'
                }`}
              >
                Demo Card
              </button>
            </div>

            {paymentMethod === 'UPI' ? (
              <div className="grid gap-4 sm:grid-cols-[12rem_1fr] sm:items-center">
                <div className="rounded-xl border border-gray-200 bg-gray-50 p-3">
                  <img
                    src={UPI_QR_IMAGE}
                    alt="UPI payment QR for TurfConnect demo"
                    className="mx-auto aspect-square w-full rounded-lg object-contain"
                  />
                </div>
                <div>
                  <p className="text-xs font-extrabold uppercase tracking-wide text-gray-500">UPI ID</p>
                  <p className="mt-1 break-all text-lg font-extrabold text-gray-950">sonusajith02@oksbi</p>
                  <p className="mt-3 text-sm font-semibold text-gray-500">
                    Scan this QR in any UPI app for the demo payment, then click the button below to mark this test payment successful.
                  </p>
                </div>
              </div>
            ) : (
              <div className="space-y-4">
                <div className="rounded-xl bg-gradient-to-br from-primary-dark to-primary p-5 text-white shadow-lg">
                  <p className="text-xs font-bold uppercase tracking-widest text-white/70">Demo card</p>
                  <p className="mt-5 text-xl font-extrabold tracking-wide">{DEMO_CARD.number}</p>
                  <div className="mt-5 flex items-end justify-between gap-4">
                    <div>
                      <p className="text-[10px] font-bold uppercase tracking-widest text-white/60">Card holder</p>
                      <p className="text-sm font-extrabold">{DEMO_CARD.name}</p>
                    </div>
                    <div className="text-right">
                      <p className="text-[10px] font-bold uppercase tracking-widest text-white/60">Valid thru</p>
                      <p className="text-sm font-extrabold">{DEMO_CARD.expiry}</p>
                    </div>
                  </div>
                </div>
                <div className="grid gap-3 sm:grid-cols-3">
                  <Info label="Card Number" value={DEMO_CARD.number} />
                  <Info label="CVV" value={DEMO_CARD.cvv} />
                  <Info label="OTP" value={DEMO_CARD.otp} />
                </div>
              </div>
            )}
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
              {paymentProvider === 'MOCK'
                ? `Complete ${paymentMethod === 'UPI' ? 'UPI' : 'Card'} Demo Payment`
                : content.button}
            </Button>
          </div>
        </form>
      )}
    </Modal>
  );
};

const Info = ({ label, value }) => (
  <div className="rounded-lg border border-gray-200 bg-gray-50 p-3">
    <p className="text-[10px] font-extrabold uppercase tracking-wide text-gray-500">{label}</p>
    <p className="mt-1 break-all text-sm font-extrabold text-gray-900">{value}</p>
  </div>
);

export default PaymentModal;
