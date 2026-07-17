package com.turfconnect.turf.controller;

import com.turfconnect.shared.dto.turf.SlotDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

/**
 * Broadcasts real-time slot status changes to WebSocket subscribers.
 *
 * Frontend subscribes to: /topic/slots/{turfId}/{date}
 * When a slot changes status (AVAILABLE → LOCKED → BOOKED), this service
 * pushes the updated SlotDTO to all connected clients — no polling needed.
 *
 * This is injected into TurfService so any status update automatically
 * triggers a broadcast.
 */
@Component
@RequiredArgsConstructor
public class SlotBroadcaster {

    private final SimpMessagingTemplate messagingTemplate;

    /**
     * Sends updated slot data to all subscribers watching that turf/date combo.
     *
     * @param slot the updated SlotDTO after status change
     */
    public void broadcastSlotUpdate(SlotDTO slot) {
        // Topic: /topic/slots/{turfId}/{date}  e.g. /topic/slots/abc123/2026-07-15
        String destination = "/topic/slots/" + slot.getTurfId() + "/" + slot.getDate();
        messagingTemplate.convertAndSend(destination, slot);
    }
}
