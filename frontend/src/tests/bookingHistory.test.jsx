import React from 'react';
import { fireEvent, render, screen, waitFor } from '@testing-library/react';
import { MemoryRouter } from 'react-router-dom';
import { beforeEach, describe, expect, test, vi } from 'vitest';
import BookingTable from '../features/booking/BookingTable';
import { createSplitPlan, saveBookingSplitPlan } from '../utils/splitPlans';

describe('BookingTable split contributions', () => {
  const booking = {
    id: 'booking-1',
    turfId: 'turf-1',
    date: '2026-07-20',
    startTime: '18:00:00',
    endTime: '19:00:00',
    totalPrice: 1200,
    status: 'CONFIRMED',
  };

  beforeEach(() => {
    window.localStorage.clear();
  });

  test('shows suggested member contributions when no saved split exists', () => {
    render(
      <MemoryRouter>
        <BookingTable bookings={[booking]} loading={false} error={null} />
      </MemoryRouter>
    );

    expect(screen.getByText('Suggested split contributions')).toBeInTheDocument();
    expect(screen.getByText('No saved split yet, showing a 6-player planning split')).toBeInTheDocument();
    expect(screen.getByText('Player 6')).toBeInTheDocument();
    expect(screen.getByText('Each contributes').parentElement).toHaveTextContent(/200\.00/);
  });

  test('shows saved member names and contribution status', () => {
    saveBookingSplitPlan(booking.id, createSplitPlan({
      totalAmount: 1200,
      memberCount: 3,
      memberNames: ['You', 'Asha', 'Rohit'],
    }));

    render(
      <MemoryRouter>
        <BookingTable bookings={[booking]} loading={false} error={null} />
      </MemoryRouter>
    );

    expect(screen.getByText('Saved split contributions')).toBeInTheDocument();
    expect(screen.getByText('Asha')).toBeInTheDocument();
    expect(screen.getByText('Rohit')).toBeInTheDocument();
    expect(screen.getByText('1/3 paid')).toBeInTheDocument();
  });

  test('sends updated split contribution when a member is marked paid', async () => {
    const onUpdateSplitContribution = vi.fn().mockResolvedValue({});
    const bookingWithSplit = {
      ...booking,
      splitContribution: createSplitPlan({
        totalAmount: 1200,
        memberCount: 3,
        memberNames: ['You', 'Asha', 'Rohit'],
      }),
    };

    render(
      <MemoryRouter>
        <BookingTable
          bookings={[bookingWithSplit]}
          loading={false}
          error={null}
          onUpdateSplitContribution={onUpdateSplitContribution}
        />
      </MemoryRouter>
    );

    fireEvent.click(screen.getAllByRole('button', { name: /mark paid/i })[0]);

    await waitFor(() => {
      expect(onUpdateSplitContribution).toHaveBeenCalledWith(
        'booking-1',
        expect.objectContaining({
          members: expect.arrayContaining([
            expect.objectContaining({ name: 'Asha', status: 'PAID' }),
          ]),
        })
      );
    });
  });
});
