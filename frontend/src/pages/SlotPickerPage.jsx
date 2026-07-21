import React, { useState, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { turfService } from '../services/turfService';
import { bookingService } from '../services/bookingService';
import { paymentService } from '../services/paymentService';
import { useSlots } from '../hooks/useSlots';
import { useSlotSocket } from '../hooks/useSlotSocket';
import { useToast } from '../hooks/useToast';
import { useAuth } from '../hooks/useAuth';
import SlotGrid from '../features/slots/SlotGrid';
import CheckoutModal from '../features/booking/CheckoutModal';
import PaymentModal from '../features/payment/PaymentModal';
import DatePicker from '../components/ui/DatePicker';
import { ROUTES } from '../constants/routes';
import { formatCurrency } from '../utils/formatters';
import { saveBookingSplitPlan, toSplitContributionRequest } from '../utils/splitPlans';

const statusItems = [
  ['Available', 'bg-primary text-white'],
  ['Locked', 'bg-yellow-400 text-yellow-950'],
  ['Booked', 'bg-gray-400 text-white'],
];

const SlotPickerPage = () => {
  const { turfId } = useParams();
  const navigate = useNavigate();
  const { addToast } = useToast();
  const { user } = useAuth();
  const [activePaymentProvider, setActivePaymentProvider] = useState(paymentService.getConfiguredProvider());

  const [date, setDate] = useState(() => {
    const today = new Date();
    return today.toISOString().split('T')[0];
  });

  const [turf, setTurf] = useState(null);
  const [turfLoading, setTurfLoading] = useState(true);
  const [selectedSlot, setSelectedSlot] = useState(null);
  const [activeBooking, setActiveBooking] = useState(null);

  const [isCheckoutOpen, setIsCheckoutOpen] = useState(false);
  const [isPaymentOpen, setIsPaymentOpen] = useState(false);

  const { slots, loading: slotsLoading, error: slotsError, updateSlotInList } = useSlots(turfId, date);

  useSlotSocket(turfId, date, (updatedSlot) => {
    console.log('[WS] Received slot status change', updatedSlot);
    updateSlotInList(updatedSlot);

    if (selectedSlot && selectedSlot.id === updatedSlot.id && updatedSlot.status !== 'AVAILABLE') {
      addToast('Sorry, this slot has just been locked by another user.', 'warning');
      setSelectedSlot(null);
      setIsCheckoutOpen(false);
    }
  });

  useEffect(() => {
    const fetchTurfDetails = async () => {
      setTurfLoading(true);
      try {
        const response = await turfService.get(turfId);
        if (response && response.success) {
          setTurf(response.data);
        }
      } catch {
        addToast('Failed to load venue details', 'error');
        navigate(ROUTES.DASHBOARD);
      } finally {
        setTurfLoading(false);
      }
    };
    fetchTurfDetails();
  }, [turfId, navigate, addToast]);

  const handleSelectSlot = (slot) => {
    setSelectedSlot(slot);
    setIsCheckoutOpen(true);
  };

  const handleConfirmCheckout = async (slot, _turfDetails, splitPlan) => {
    try {
      const response = await bookingService.create(slot.id, slot.price, toSplitContributionRequest(splitPlan));
      if (response && response.success) {
        saveBookingSplitPlan(response.data.id, response.data.splitContribution || splitPlan);
        setActiveBooking(response.data);
        setIsCheckoutOpen(false);
        setIsPaymentOpen(true);
      } else {
        throw new Error(response?.message || 'Booking creation failed');
      }
    } catch (e) {
      addToast(e.message || 'Failed to checkout timeslot', 'error');
      throw e;
    }
  };

  const handlePaymentComplete = async (booking, options = {}) => {
    try {
      const providerToUse = options.demoPayment ? 'MOCK' : activePaymentProvider;
      const initiateRes = await paymentService.initiate(booking.id, booking.totalPrice, 'INR', providerToUse);
      if (!initiateRes.success) {
        throw new Error(initiateRes.message || 'Payment initiation failed on the server. Please try a different method.');
      }

      const { transactionId, amount, provider, orderId, keyId } = initiateRes.data;
      const activeProvider = provider || providerToUse;
      const activeOrderId = orderId || transactionId;

      if (activeProvider === 'MOCK') {
        const verifyRes = await paymentService.verifyPayment(transactionId);
        addToast(options.demoPayment ? 'Dummy Razorpay payment confirmed successfully!' : 'Booking confirmed successfully!', 'success');

        setIsPaymentOpen(false);
        setSelectedSlot(null);
        setActiveBooking(null);
        navigate(ROUTES.BOOKINGS);
        return verifyRes.data;
      }

      const razorpayKeyId = keyId || import.meta.env.VITE_RAZORPAY_KEY_ID;
      if (!razorpayKeyId || !window.Razorpay) {
        throw new Error('Razorpay checkout is unavailable. Add Razorpay test keys on the backend or use VITE_PAYMENT_PROVIDER=MOCK.');
      }

      return new Promise((resolve, reject) => {
        const options = {
          key: razorpayKeyId,
          amount: amount * 100,
          currency: 'INR',
          name: 'TurfConnect',
          description: `Booking for ${turf.name}`,
          order_id: activeOrderId,
          handler: async function (razorpayResponse) {
            try {
              const verifyRes = await paymentService.verifyPayment(transactionId, {
                razorpayOrderId: razorpayResponse.razorpay_order_id,
                razorpayPaymentId: razorpayResponse.razorpay_payment_id,
                razorpaySignature: razorpayResponse.razorpay_signature,
              });
              addToast('Booking confirmed successfully!', 'success');

              setIsPaymentOpen(false);
              setSelectedSlot(null);
              setActiveBooking(null);
              navigate(ROUTES.BOOKINGS);
              resolve(verifyRes.data);
            } catch (err) {
              addToast('Payment verification failed on server', 'error');
              reject(err);
            }
          },
          prefill: {
            name: user?.name || 'TurfConnect Player',
            email: user?.email || 'player@turfconnect.test',
            contact: '9999999999',
          },
          theme: {
            color: '#1b5e20',
          },
          modal: {
            ondismiss: function () {
              addToast('Payment cancelled', 'warning');
              reject(new Error('Payment cancelled'));
            },
          },
        };

        const rzp = new window.Razorpay(options);
        rzp.on('payment.failed', function (response) {
          addToast(response.error.description || 'Payment failed', 'error');
          reject(new Error('Payment failed'));
        });
        rzp.open();
      });
    } catch (e) {
      addToast(e.message || 'Checkout payment flow failed', 'error');
      throw e;
    }
  };

  const handlePaymentModalClose = () => {
    setIsPaymentOpen(false);
    setSelectedSlot(null);
    setActiveBooking(null);
    navigate(ROUTES.BOOKINGS);
  };

  const availableCount = slots?.filter((slot) => slot.status === 'AVAILABLE').length || 0;
  const startingPrice = slots?.length ? Math.min(...slots.map((slot) => Number(slot.price || 0))) : turf?.hourlyRate;

  return (
    <div className="space-y-6 pb-10">
      {turfLoading ? (
        <div className="h-40 animate-pulse rounded-lg border border-primary/10 bg-white" />
      ) : turf && (
        <section className="rounded-lg border border-primary/10 bg-white p-6 shadow-sm">
          <div className="flex flex-col gap-6 lg:flex-row lg:items-start lg:justify-between">
            <div className="space-y-4">
              <button
                type="button"
                onClick={() => navigate(ROUTES.TURF_DETAILS.replace(':turfId', turfId))}
                className="text-sm font-extrabold text-primary hover:text-primary-dark"
              >
                Back to venue details
              </button>

              <div>
                <p className="text-xs font-extrabold uppercase tracking-wider text-accent">Book your slot</p>
                <h1 className="mt-1 text-3xl font-extrabold tracking-tight text-gray-950">{turf.name}</h1>
                <p className="mt-2 max-w-2xl text-sm font-medium text-gray-500">
                  {turf.address}, {turf.city}
                </p>
              </div>

              <div className="flex flex-wrap gap-2">
                {turf.sportTypes?.map((sport) => (
                  <span key={sport} className="rounded-full border border-primary/15 bg-primary-light px-3 py-1 text-xs font-extrabold uppercase tracking-wide text-primary-dark">
                    {sport}
                  </span>
                ))}
              </div>
            </div>

            <div className="grid gap-3 sm:grid-cols-3 lg:w-[28rem]">
              <div className="rounded-lg border border-primary/10 bg-[#f4faff] p-4">
                <p className="text-xs font-bold uppercase tracking-wide text-gray-500">Available</p>
                <p className="mt-2 text-2xl font-extrabold text-primary-dark">{availableCount}</p>
              </div>
              <div className="rounded-lg border border-primary/10 bg-[#f4faff] p-4">
                <p className="text-xs font-bold uppercase tracking-wide text-gray-500">Date</p>
                <p className="mt-2 text-sm font-extrabold text-gray-950">{date}</p>
              </div>
              <div className="rounded-lg border border-primary/10 bg-[#f4faff] p-4">
                <p className="text-xs font-bold uppercase tracking-wide text-gray-500">From</p>
                <p className="mt-2 text-lg font-extrabold text-primary-dark">{formatCurrency(startingPrice)}</p>
              </div>
            </div>
          </div>
        </section>
      )}

      <section className="rounded-lg border border-primary/10 bg-white p-5 shadow-sm">
        <div className="flex flex-col gap-4 border-b border-primary/10 pb-5 sm:flex-row sm:items-center sm:justify-between">
          <div>
            <h2 className="text-xl font-extrabold tracking-tight text-gray-950">Select Timeslot</h2>
            <p className="mt-1 text-sm font-medium text-gray-500">Live slot status updates in Asia/Kolkata timezone.</p>
          </div>

          <div className="flex flex-col gap-3 sm:items-end">
            <label htmlFor="slotDatePicker" className="text-xs font-extrabold uppercase tracking-wide text-gray-500">
              Booking Date
            </label>
            <DatePicker
              id="slotDatePicker"
              value={date}
              min={new Date().toISOString().split('T')[0]}
              onChange={setDate}
              className="w-40 sm:w-48"
            />
          </div>
        </div>

        <div className="mt-5 flex flex-wrap gap-3">
          {statusItems.map(([label, className]) => (
            <div key={label} className="flex items-center gap-2 text-xs font-bold uppercase tracking-wide text-gray-500">
              <span className={`h-2.5 w-2.5 rounded-full ${className}`} />
              {label}
            </div>
          ))}
        </div>

        <SlotGrid
          slots={slots}
          loading={slotsLoading}
          error={slotsError}
          onSelectSlot={handleSelectSlot}
        />
      </section>

      {isCheckoutOpen && (
        <CheckoutModal
          isOpen={isCheckoutOpen}
          onClose={() => setIsCheckoutOpen(false)}
          slot={selectedSlot}
          turf={turf}
          onConfirm={handleConfirmCheckout}
        />
      )}

      {isPaymentOpen && (
        <PaymentModal
          isOpen={isPaymentOpen}
          onClose={handlePaymentModalClose}
          booking={activeBooking}
          onPaymentComplete={handlePaymentComplete}
          paymentProvider={activePaymentProvider}
          onSwitchToMock={() => {
            setActivePaymentProvider('MOCK');
            addToast('Switched to Mock Test Payment. Please retry.', 'info');
          }}
        />
      )}
    </div>
  );
};

export default SlotPickerPage;
