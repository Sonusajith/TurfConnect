import React, { useState, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { turfService } from '../services/turfService';
import { bookingService } from '../services/bookingService';
import { paymentService } from '../services/paymentService';
import { useSlots } from '../hooks/useSlots';
import { useSlotSocket } from '../hooks/useSlotSocket';
import { useToast } from '../contexts/ToastContext';
import SlotGrid from '../features/slots/SlotGrid';
import CheckoutModal from '../features/booking/CheckoutModal';
import PaymentModal from '../features/payment/PaymentModal';
import Button from '../components/Button';
import { ROUTES } from '../constants/routes';

const SlotPickerPage = () => {
  const { turfId } = useParams();
  const navigate = useNavigate();
  const { addToast } = useToast();

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

  // Fetch Slots
  const { slots, loading: slotsLoading, error: slotsError, updateSlotInList } = useSlots(turfId, date);

  // Real-time updates via WebSockets
  useSlotSocket(turfId, date, (updatedSlot) => {
    console.log('[WS] Received slot status change', updatedSlot);
    updateSlotInList(updatedSlot);
    
    // Alert user if their selected slot gets locked by someone else while looking
    if (selectedSlot && selectedSlot.id === updatedSlot.id && updatedSlot.status !== 'AVAILABLE') {
      addToast('Sorry, this slot has just been locked by another user.', 'warning');
      setSelectedSlot(null);
      setIsCheckoutOpen(false);
    }
  });

  // Fetch Turf Details
  useEffect(() => {
    const fetchTurfDetails = async () => {
      setTurfLoading(true);
      try {
        const response = await turfService.get(turfId);
        if (response && response.success) {
          setTurf(response.data);
        }
      } catch (e) {
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

  const handleConfirmCheckout = async (slot, turfDetails) => {
    try {
      // 1. Create PENDING Booking (this requests a lock under the hood)
      const response = await bookingService.create(slot.id, slot.price);
      if (response && response.success) {
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

  const handlePaymentComplete = async (booking) => {
    try {
      // 1. Initiate Payment Session (Backend creates Razorpay Order)
      const initiateRes = await paymentService.initiate(booking.id, booking.totalPrice);
      if (!initiateRes.success) {
        throw new Error(initiateRes.message || 'Payment initiation failed');
      }
      
      const { transactionId, amount } = initiateRes.data;

      // 2. Open Razorpay Checkout Widget
      return new Promise((resolve, reject) => {
        const options = {
          key: import.meta.env.VITE_RAZORPAY_KEY_ID, 
          amount: amount * 100, // Razorpay expects paise (but backend already sets it, though frontend just uses it for display)
          currency: "INR",
          name: "TurfConnect",
          description: `Booking for ${turf.name}`,
          order_id: transactionId, 
          handler: async function (response) {
            try {
              // 3. Verify Payment with Backend
              const verifyRes = await paymentService.verifyPayment(transactionId);
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
            name: "Test User",
            email: "test@example.com",
            contact: "9999999999"
          },
          theme: {
            color: "#16a34a" // TurfConnect Primary Green
          },
          modal: {
            ondismiss: function() {
              addToast('Payment cancelled', 'warning');
              reject(new Error('Payment cancelled'));
            }
          }
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

  return (
    <div className="space-y-6">
      {/* Venue Header */}
      {turfLoading ? (
        <div className="h-28 bg-gray-100 rounded-2xl animate-pulse" />
      ) : turf && (
        <div className="bg-white border rounded-2xl p-6 shadow-sm flex flex-col md:flex-row md:justify-between md:items-center gap-4">
          <div className="space-y-1">
            <h1 className="text-2xl font-bold text-gray-900">{turf.name}</h1>
            <p className="text-gray-500 text-sm">📍 {turf.address}, {turf.city}</p>
            <div className="flex gap-2 pt-2">
              {turf.sportTypes?.map((sport) => (
                <span key={sport} className="px-2 py-0.5 bg-green-50 text-green-700 border border-green-150 text-xs font-semibold rounded-full uppercase">
                  {sport}
                </span>
              ))}
            </div>
          </div>
          <div>
            <Button variant="outline" onClick={() => navigate(ROUTES.DASHBOARD)}>
              ← Back to Venues
            </Button>
          </div>
        </div>
      )}

      {/* Date Picker Selector */}
      <div className="bg-white border rounded-2xl p-6 shadow-sm space-y-4">
        <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4">
          <div>
            <h2 className="text-lg font-bold text-gray-900">Select Timeslot</h2>
            <p className="text-xs text-gray-400 font-medium">Slots generated daily. Timeslots are in Asia/Kolkata timezone.</p>
          </div>
          <div className="flex items-center gap-2">
            <label htmlFor="slotDatePicker" className="text-sm font-semibold text-gray-700">Date:</label>
            <input
              type="date"
              id="slotDatePicker"
              value={date}
              min={new Date().toISOString().split('T')[0]}
              onChange={(e) => setDate(e.target.value)}
              className="px-3 py-1.5 border rounded-lg focus:outline-none focus:ring-2 focus:ring-primary-light focus:border-primary font-medium"
            />
          </div>
        </div>

        {/* Slot Grid display */}
        <SlotGrid
          slots={slots}
          loading={slotsLoading}
          error={slotsError}
          onSelectSlot={handleSelectSlot}
        />
      </div>

      {/* Checkout and Payment Dialog Popups */}
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
        />
      )}
    </div>
  );
};

export default SlotPickerPage;
