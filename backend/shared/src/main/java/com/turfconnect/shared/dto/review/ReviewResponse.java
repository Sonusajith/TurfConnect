package com.turfconnect.shared.dto.review;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReviewResponse {
    private String id;
    private String userId;
    private String userName;
    private String userEmail;
    private String bookingId;
    private String turfId;
    private Integer rating;
    private String comment;
    private String status;
    private Boolean isEdited;
    private Boolean isDeleted;
    private String ownerReply;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
