package com.turfconnect.turf;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
class TurfApplicationTests {

    @Test
    void contextLoads() {
        // Verification that the application context successfully loads under the test profile
    }
}
