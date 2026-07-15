import { renderHook } from '@testing-library/react';
import { describe, test, expect, vi, beforeEach } from 'vitest';
import useSlotSocket from '../hooks/useSlotSocket';
import { Client } from '@stomp/stompjs';

const { mockActivate, mockDeactivate, mockSubscribe } = vi.hoisted(() => {
  return {
    mockActivate: vi.fn(),
    mockDeactivate: vi.fn(),
    mockSubscribe: vi.fn(),
  };
});

// Mock STOMP Client
vi.mock('@stomp/stompjs', () => {
  class MockClient {
    constructor(config) {
      this.config = config;
      this.activate = mockActivate;
      this.deactivate = mockDeactivate;
      this.subscribe = mockSubscribe;
    }
  }

  return {
    Client: MockClient,
  };
});

// Mock SockJS
vi.mock('sockjs-client', () => {
  const mockClose = vi.fn();
  function MockSockJS() {
    return {
      close: mockClose,
    };
  }
  return {
    default: MockSockJS,
  };
});

describe('useSlotSocket Custom Hook', () => {
  beforeEach(() => {
    vi.clearAllMocks();
  });

  test('activates STOMP client upon valid params', () => {
    const onSlotUpdate = vi.fn();
    
    renderHook(() => 
      useSlotSocket('turf-1', '2026-07-15', onSlotUpdate)
    );

    expect(mockActivate).toHaveBeenCalled();
  });

  test('deactivates STOMP client upon cleanup', () => {
    const onSlotUpdate = vi.fn();

    const { unmount } = renderHook(() =>
      useSlotSocket('turf-1', '2026-07-15', onSlotUpdate)
    );

    unmount();

    expect(mockDeactivate).toHaveBeenCalled();
  });
});
