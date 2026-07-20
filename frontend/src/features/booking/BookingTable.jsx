import React, { useState } from 'react';
import { Link } from 'react-router-dom';
import Badge from '../../components/Badge';
import Button from '../../components/Button';
import LoadingSkeleton from '../../components/LoadingSkeleton';
import { formatCurrency, formatTime, formatDate } from '../../utils/formatters';
import { ROUTES } from '../../constants/routes';
import SplitContributionPanel from './SplitContributionPanel';
import CancelBookingModal from './CancelBookingModal';
import {
  createSplitPlan,
  getBookingSplitPlan,
  saveBookingSplitPlan,
  toSplitContributionRequest,
  updateMemberStatus,
} from '../../utils/splitPlans';

const BookingTable = ({ bookings, loading, error, onCancelBooking, onUpdateSplitContribution }) => {
  if (loading) {
    return (
      <div className="mt-6 rounded-lg border border-primary/10 bg-white p-6 shadow-sm">
        <div className="flex flex-col gap-4">
          <LoadingSkeleton variant="text" />
          <LoadingSkeleton variant="text" />
          <LoadingSkeleton variant="text" />
        </div>
      </div>
    );
  }

  if (error) {
    return (
      <div className="mt-6 rounded-lg border border-red-100 bg-red-50 py-8 text-center">
        <p className="mb-2 font-extrabold text-red-600">Failed to load bookings</p>
        <p className="text-sm font-medium text-red-500">{error}</p>
      </div>
    );
  }

  if (!bookings || bookings.length === 0) {
    return (
      <div className="mt-6 rounded-lg border border-dashed border-primary/20 bg-white py-12 text-center shadow-sm">
        <p className="text-lg font-extrabold text-gray-600">No bookings yet</p>
        <p className="mt-1 text-sm font-medium text-gray-400">Once you complete a booking, it will show up here.</p>
      </div>
    );
  }

  return (
    <div className="mt-6 grid gap-4">
      {bookings.map((booking) => (
        <BookingCard
          key={booking.id}
          booking={booking}
          onCancelBooking={onCancelBooking}
          onUpdateSplitContribution={onUpdateSplitContribution}
        />
      ))}
    </div>
  );
};

const BookingCard = ({ booking, onCancelBooking, onUpdateSplitContribution }) => {
  const [isCancelOpen, setIsCancelOpen] = useState(false);
  const [actionError, setActionError] = useState('');
  const [updatingMemberId, setUpdatingMemberId] = useState('');
  const savedSplitPlan = getBookingSplitPlan(booking.id);
  const splitPlan = booking.splitContribution || savedSplitPlan || createSplitPlan({
    totalAmount: booking.totalPrice,
    memberCount: 6,
    paidMemberIds: booking.status === 'CONFIRMED' ? ['member-1'] : [],
  });
  const turfDetailsPath = booking.turfId
    ? ROUTES.TURF_DETAILS.replace(':turfId', booking.turfId)
    : ROUTES.EXPLORE;
  const canCancel = booking.status === 'PENDING' || booking.status === 'CONFIRMED';
  const hasSavedSplit = Boolean(booking.splitContribution || savedSplitPlan);

  const handleMemberStatusChange = async (memberId, status) => {
    if (!onUpdateSplitContribution) return;

    setUpdatingMemberId(memberId);
    setActionError('');
    const nextSplitPlan = updateMemberStatus(splitPlan, memberId, status);

    try {
      await onUpdateSplitContribution(booking.id, toSplitContributionRequest(nextSplitPlan));
      saveBookingSplitPlan(booking.id, nextSplitPlan);
    } catch (e) {
      setActionError(e.message || 'Failed to update split contribution');
    } finally {
      setUpdatingMemberId('');
    }
  };

  const handleCancel = async () => {
    if (!onCancelBooking) return;
    setActionError('');
    await onCancelBooking(booking.id);
  };

  return (
    <article className="rounded-lg border border-primary/10 bg-white p-5 shadow-sm transition hover:border-primary/20 hover:shadow-md">
      <div className="flex flex-col gap-4 lg:flex-row lg:items-start lg:justify-between">
        <div className="min-w-0">
          <div className="flex flex-wrap items-center gap-2">
            <Badge status={booking.status} />
            <span className="rounded-full bg-[#f4faff] px-3 py-1 text-xs font-extrabold uppercase tracking-wide text-primary-dark">
              {formatDate(booking.date)}
            </span>
          </div>
          <h3 className="mt-3 break-all font-mono text-sm font-extrabold text-gray-950">
            Booking {booking.id}
          </h3>
          <p className="mt-2 text-sm font-medium text-gray-500">
            {formatTime(booking.startTime)} - {formatTime(booking.endTime)}
            {booking.turfId ? ` at Turf ${booking.turfId}` : ''}
          </p>
        </div>

        <div className="grid min-w-0 grid-cols-2 gap-3 sm:min-w-80">
          <Metric label="Amount" value={formatCurrency(booking.totalPrice)} />
          <Metric label="Split members" value={splitPlan.memberCount} />
        </div>
      </div>

      <div className="mt-5">
        <SplitContributionPanel
          splitPlan={splitPlan}
          title={hasSavedSplit ? 'Saved split contributions' : 'Suggested split contributions'}
          subtitle={booking.splitContribution
            ? `${splitPlan.memberCount} members saved with this booking`
            : savedSplitPlan
              ? `${splitPlan.memberCount} members attached to this booking`
            : 'No saved split yet, showing a 6-player planning split'}
          onMemberStatusChange={booking.status !== 'CANCELLED' ? handleMemberStatusChange : undefined}
          updatingMemberId={updatingMemberId}
        />
      </div>

      {actionError && (
        <div className="mt-4 rounded-lg border border-red-100 bg-red-50 p-3 text-xs font-semibold text-red-600">
          {actionError}
        </div>
      )}

      <div className="mt-5 flex flex-col gap-3 border-t border-primary/10 pt-4 sm:flex-row sm:items-center sm:justify-between">
        <p className="text-xs font-medium text-gray-500">
          {hasSavedSplit
            ? 'Split contributions are saved with this booking.'
            : 'Use the member actions to save this suggested split for the booking.'}
        </p>
        <div className="flex flex-col gap-2 sm:flex-row">
          {canCancel && (
            <Button variant="danger" size="sm" onClick={() => setIsCancelOpen(true)}>
              Cancel Booking
            </Button>
          )}
          <Link
            to={turfDetailsPath}
            className="inline-flex items-center justify-center rounded-lg border border-gray-300 bg-white px-3 py-1.5 text-sm font-medium text-gray-700 transition-colors hover:bg-gray-50 focus:outline-none focus:ring-2 focus:ring-gray-500 focus:ring-offset-2"
          >
            View Turf
          </Link>
        </div>
      </div>

      <CancelBookingModal
        isOpen={isCancelOpen}
        onClose={() => setIsCancelOpen(false)}
        onConfirm={handleCancel}
        booking={booking}
      />
    </article>
  );
};

const Metric = ({ label, value }) => (
  <div className="rounded-lg border border-primary/10 bg-[#f4faff] p-3">
    <p className="text-[10px] font-bold uppercase tracking-wide text-gray-500">{label}</p>
    <p className="mt-1 truncate text-lg font-extrabold text-gray-950">{value}</p>
  </div>
);

export default BookingTable;
