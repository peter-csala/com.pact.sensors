package com.pact.sensors.helpers;

import java.time.Instant;

public interface TimeProvider {
    Instant getNow();
}
