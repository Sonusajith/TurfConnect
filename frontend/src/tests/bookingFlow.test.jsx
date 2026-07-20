import React from 'react';
import { render, screen, fireEvent, waitFor } from '@testing-library/react';
import { describe, test, expect, vi } from 'vitest';
import CheckoutModal from '../features/booking/CheckoutModal';

describe('CheckoutModal Component', () => {
  const mockSlot = {
    id: 'slot-1',
    date: '2026-07-15',
    startTime: '06:00:00',
    endTime: '07:00:00',
    price: 120.0,
  };

  const mockTurf = {
    id: 'turf-1',
    name: 'Camp Nou Arena',
  };

  test('renders checkout item fields correctly', () => {
    render(
      <CheckoutModal
        isOpen={true}
        onClose={vi.fn()}
        slot={mockSlot}
        turf={mockTurf}
        onConfirm={vi.fn()}
      />
    );

    expect(screen.getByText('Camp Nou Arena')).toBeInTheDocument();
    expect(screen.getByText('6:00 AM - 7:00 AM')).toBeInTheDocument();
    expect(screen.getByText(/120\.00/)).toBeInTheDocument();
  });

  test('invokes confirm callback when button is clicked', async () => {
    const handleConfirm = vi.fn();
    render(
      <CheckoutModal
        isOpen={true}
        onClose={vi.fn()}
        slot={mockSlot}
        turf={mockTurf}
        onConfirm={handleConfirm}
      />
    );

    fireEvent.click(screen.getByRole('button', { name: /confirm & pay/i }));
    
    await waitFor(() => {
      expect(handleConfirm).toHaveBeenCalledWith(mockSlot, mockTurf);
    });
  });

  test('shows member contribution split and recalculates per member amount', () => {
    render(
      <CheckoutModal
        isOpen={true}
        onClose={vi.fn()}
        slot={mockSlot}
        turf={mockTurf}
        onConfirm={vi.fn()}
      />
    );

    expect(screen.getByText('Team contribution split')).toBeInTheDocument();
    expect(screen.getByText(/You \+ 5 teammates/i)).toBeInTheDocument();
    expect(screen.getByText('Each contributes').parentElement).toHaveTextContent(/20\.00/);

    fireEvent.change(screen.getByLabelText(/number of members/i), {
      target: { value: '4' },
    });

    expect(screen.getByText(/You \+ 3 teammates/i)).toBeInTheDocument();
    expect(screen.getByText('Each contributes').parentElement).toHaveTextContent(/30\.00/);
  });
});
