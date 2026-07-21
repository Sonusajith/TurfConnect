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
    expect(screen.getByText(/120\.00/)).toBeInTheDocument();
    expect(screen.getByText(/razorpay's secure checkout/i)).toBeInTheDocument();
    expect(screen.getByRole('button', { name: /pay with razorpay/i })).toBeInTheDocument();
  });

  test('renders local test gateway payment prompt', () => {
    render(
      <PaymentModal
        isOpen={true}
        onClose={vi.fn()}
        booking={mockBooking}
        onPaymentComplete={vi.fn()}
        paymentProvider="MOCK"
      />
    );

    expect(screen.getByText(/local test gateway/i)).toBeInTheDocument();
    expect(screen.getByText(/sonusajith02@oksbi/i)).toBeInTheDocument();
    expect(screen.getByRole('button', { name: /complete upi demo payment/i })).toBeInTheDocument();
  });

  test('shows demo card details after selecting card payment', () => {
    render(
      <PaymentModal
        isOpen={true}
        onClose={vi.fn()}
        booking={mockBooking}
        onPaymentComplete={vi.fn()}
        paymentProvider="MOCK"
      />
    );

    fireEvent.click(screen.getByRole('button', { name: /demo card/i }));

    expect(screen.getAllByText(/4111 1111 1111 1111/i)).toHaveLength(2);
    expect(screen.getByText(/12\/30/i)).toBeInTheDocument();
    expect(screen.getByText(/123456/i)).toBeInTheDocument();
    expect(screen.getByRole('button', { name: /complete card demo payment/i })).toBeInTheDocument();
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
