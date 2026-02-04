package com.codewithpcodes.oikon.kafka;

import java.time.Instant;
import java.util.UUID;

public record UserUpdatedEvent(
        UUID userId,
        String email,
        String status,
        Instant timestamp
) {
}
