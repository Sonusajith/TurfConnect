package com.turfconnect.auth.model;

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
@Document(collection = "refresh_tokens")
public class RefreshToken {

    @Id
    private String id;
    
    @Indexed(unique = true)
    private String tokenId; // A unique identifier for this token issuance
    
    private String userId;
    
    private String tokenHash; // Storing only the hash of the actual token
    
    private LocalDateTime issuedAt;
    private LocalDateTime expiresAt;
    
    private String deviceId;
    private String deviceName;
    private String ipAddress;
    
    private boolean revoked;
    private LocalDateTime lastUsedAt;
}
