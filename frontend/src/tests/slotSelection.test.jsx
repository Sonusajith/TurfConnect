import React from 'react';
import { render, screen, fireEvent } from '@testing-library/react';
import { describe, test, expect, vi } from 'vitest';
import SlotGrid from '../features/slots/SlotGrid';

describe('SlotGrid Component', () => {
  test('renders slot grid item with details and formats', () => {
    const mockSlots = [
      {
        id: 'slot-1',
        turfId: 'turf-1',
        date: '2026-07-15',
        startTime: '06:00:00',
        endTime: '07:00:00',
        price: 120.0,
        status: 'AVAILABLE',
      },
    ];

    const handleSelectSlot = vi.fn();

    render(
      <SlotGrid
        slots={mockSlots}
        loading={false}
        error={null}
        onSelectSlot={handleSelectSlot}
      />
    );

    const slotBtn = screen.getByRole('button');
    expect(slotBtn).toBeInTheDocument();
    expect(screen.getByText('6:00 AM')).toBeInTheDocument();
    expect(screen.getByText('₹120')).toBeInTheDocument();

    fireEvent.click(slotBtn);
    expect(handleSelectSlot).toHaveBeenCalledWith(mockSlots[0]);
  });

  test('disables slot card selection button if slot is not available', () => {
    const mockSlots = [
      {
        id: 'slot-1',
        turfId: 'turf-1',
        date: '2026-07-15',
        startTime: '06:00:00',
        endTime: '07:00:00',
        price: 120.0,
        status: 'BOOKED',
      },
    ];

    render(
      <SlotGrid
        slots={mockSlots}
        loading={false}
        error={null}
        onSelectSlot={vi.fn()}
      />
    );

    const slotBtn = screen.getByRole('button');
    expect(slotBtn).toBeDisabled();
    expect(screen.getByText('BOOKED')).toBeInTheDocument();
  });
});
