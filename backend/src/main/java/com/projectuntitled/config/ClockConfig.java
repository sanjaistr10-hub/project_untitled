package com.projectuntitled.config;

import java.time.Clock;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ClockConfig {

    /** Inject this instead of calling Instant.now() so time-based logic stays testable. */
    @Bean
    public Clock clock() {
        return Clock.systemUTC();
    }
}
