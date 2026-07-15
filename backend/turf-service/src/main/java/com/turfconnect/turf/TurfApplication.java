package com.turfconnect.turf;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages = {"com.turfconnect.turf", "com.turfconnect.shared"})
public class TurfApplication {
    public static void main(String[] args) {
        SpringApplication.run(TurfApplication.class, args);
    }
}
