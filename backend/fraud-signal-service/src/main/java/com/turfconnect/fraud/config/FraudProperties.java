package com.turfconnect.fraud.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "fraud")
public class FraudProperties {

    private Thresholds thresholds = new Thresholds();
    private Flag flag = new Flag();

    @Data
    public static class Thresholds {
        private Rule booking = new Rule();
        private Rule cancellation = new Rule();
    }

    @Data
    public static class Rule {
        private int limit;
        private int windowHours;
    }

    @Data
    public static class Flag {
        private int ttlDays;
    }
}
