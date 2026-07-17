package com.turfconnect.orgadmin.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "franchises")
public class Franchise {
    @Id
    private String id;
    
    private String organizationId;
    private String name;
    private String location;
    
    @Builder.Default
    private List<String> adminIds = new ArrayList<>();
    
    private String status; // ACTIVE, INACTIVE
    
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
