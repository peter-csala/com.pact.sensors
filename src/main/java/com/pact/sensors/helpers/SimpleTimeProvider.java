package com.pact.sensors.helpers;

import org.springframework.stereotype.Component;

import java.time.Instant;

@Component
public class SimpleTimeProvider implements TimeProvider {
    @Override
    public Instant getNow() {
        return Instant.now();
    }
}
