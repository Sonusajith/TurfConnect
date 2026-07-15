package com.turfconnect.shared.dto.turf;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TurfDTO {
    private String id;
    private String name;
    private String location;
    private String city;
    private List<String> sportsSupported;
    private Double rating;
}
