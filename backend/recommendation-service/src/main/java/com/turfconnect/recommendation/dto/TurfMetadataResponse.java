package com.turfconnect.recommendation.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TurfMetadataResponse {
    private String id;
    private String name;
    private String city;
    private String location;
    private List<String> sportTypes;
    private boolean active; // to map 'isActive'
}
