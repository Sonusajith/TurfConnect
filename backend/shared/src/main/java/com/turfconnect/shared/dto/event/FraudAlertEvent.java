package com.turfconnect.shared.dto.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FraudAlertEvent implements Serializable {
    private static final long serialVersionUID = 1L;

    private String userId;
    private FraudAlertType alertType;
    private int triggeredThreshold;
    private int currentCounterValue;
    private LocalDateTime timestamp;
}
