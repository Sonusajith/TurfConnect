import { useEffect, useRef } from 'react';
import { Client } from '@stomp/stompjs';
import SockJS from 'sockjs-client';
import { WEBSOCKET_URL } from '../constants/api';

/**
 * Custom hook to manage WebSockets for slot status updates.
 *
 * Connects to the STOMP server and subscribes to real-time status updates
 * for a specific turf and date.
 *
 * @param {string} turfId the turf id
 * @param {string} date the date string (YYYY-MM-DD)
 * @param {function} onSlotUpdate callback invoked when a slot changes status
 */
export const useSlotSocket = (turfId, date, onSlotUpdate) => {
  const stompClientRef = useRef(null);

  useEffect(() => {
    if (!turfId || !date) return;

    // Create SockJS connection & STOMP client
    const socket = new SockJS(WEBSOCKET_URL);
    const client = new Client({
      webSocketFactory: () => socket,
      debug: (str) => {
        console.log('[STOMP] ' + str);
      },
      reconnectDelay: 5000,
      heartbeatIncoming: 4000,
      heartbeatOutgoing: 4000,
    });

    client.onConnect = (frame) => {
      console.log('[STOMP] Connected');
      
      // Subscribe to the specific slot update topic
      const destination = `/topic/slots/${turfId}/${date}`;
      client.subscribe(destination, (message) => {
        try {
          const updatedSlot = JSON.parse(message.body);
          if (updatedSlot && onSlotUpdate) {
            onSlotUpdate(updatedSlot);
          }
        } catch (e) {
          console.error('Error parsing slot update socket message', e);
        }
      });
    };

    client.onStompError = (frame) => {
      console.error('[STOMP] Error', frame.headers['message'], frame.body);
    };

    client.activate();
    stompClientRef.current = client;

    // Disconnect on cleanup
    return () => {
      if (stompClientRef.current) {
        stompClientRef.current.deactivate();
        console.log('[STOMP] Deactivated');
      }
    };
  }, [turfId, date, onSlotUpdate]);

  return stompClientRef.current;
};
export default useSlotSocket;
