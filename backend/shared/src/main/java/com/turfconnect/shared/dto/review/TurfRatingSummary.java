package com.turfconnect.shared.dto.review;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TurfRatingSummary implements Serializable {
    private static final long serialVersionUID = 1L;

    private Double averageRating;
    private Integer totalReviews;
}
