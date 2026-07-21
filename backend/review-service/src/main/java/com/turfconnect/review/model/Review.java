package com.turfconnect.review.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "reviews")
public class Review {

    @Id
    private String id;

    @Indexed
    private String userId;
    private String userName;
    private String userEmail;

    @Indexed(unique = true)
    private String bookingId;

    @Indexed
    private String turfId;

    private Integer rating;
    private String comment;
    
    @Builder.Default
    private ReviewStatus status = ReviewStatus.ACTIVE;
    
    @Builder.Default
    private Boolean isEdited = false;
    
    @Builder.Default
    private Boolean isDeleted = false;
    
    private String ownerReply;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
