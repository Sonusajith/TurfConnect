package com.turfconnect.auth.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "users")
public class User {
    
    @Id
    private String id;
    
    private String name;
    private String email;
    private String mobileNumber;
    private String passwordHash;
    private String role; // PLAYER, TURF_OWNER, etc.
    
    private String accountStatus; // ACTIVE, LOCKED, DISABLED
    
    private boolean emailVerified;
    private boolean mobileVerified;
    
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime lastLogin;
    
    private int failedLoginAttempts;
    private LocalDateTime accountLockedUntil;
    
    private String profileImage;
}
