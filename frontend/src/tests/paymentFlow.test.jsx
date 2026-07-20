import React from 'react';
import { render, screen, fireEvent, waitFor } from '@testing-library/react';
import { describe, test, expect, vi } from 'vitest';
import PaymentModal from '../features/payment/PaymentModal';

describe('PaymentModal Component', () => {
  const mockBooking = {
    id: 'booking-1',
    totalPrice: 120.0,
  };

  test('renders Razorpay checkout prompt with booking amount', () => {
    render(
      <PaymentModal
        isOpen={true}
        onClose={vi.fn()}
        booking={mockBooking}
        onPaymentComplete={vi.fn()}
      />
    );

    expect(screen.getByText(/total amount/i)).toBeInTheDocument();
    expect(screen.getByText('₹120.00')).toBeInTheDocument();
    expect(screen.getByText(/razorpay's secure checkout/i)).toBeInTheDocument();
    expect(screen.getByRole('button', { name: /pay with razorpay/i })).toBeInTheDocument();
  });

  test('triggers callback when Razorpay payment button is clicked', async () => {
    const handleComplete = vi.fn().mockResolvedValue({ success: true });
    
    render(
      <PaymentModal
        isOpen={true}
        onClose={vi.fn()}
        booking={mockBooking}
        onPaymentComplete={handleComplete}
      />
    );

    fireEvent.click(screen.getByRole('button', { name: /pay with razorpay/i }));

    await waitFor(() => {
      expect(handleComplete).toHaveBeenCalledWith(mockBooking);
      expect(screen.getByText(/payment successful/i)).toBeInTheDocument();
    });
  });
});
