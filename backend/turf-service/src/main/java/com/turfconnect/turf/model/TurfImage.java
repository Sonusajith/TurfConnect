package com.turfconnect.turf.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TurfImage {
    private String imageId;
    private String imageUrl;
    private Integer displayOrder;
    private LocalDateTime uploadedAt;
}
