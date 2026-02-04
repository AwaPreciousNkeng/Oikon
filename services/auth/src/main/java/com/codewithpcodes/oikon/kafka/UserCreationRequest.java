package com.codewithpcodes.oikon.kafka;

import java.time.LocalDate;
import java.util.UUID;

public record UserCreationRequest(
        UUID userId,
        String firstName,
        String lastName,
        String email,
        String phoneNumber,
        LocalDate dateOfBirth,
        int zipCode,
        String street,
        String city,
        String state,
        String country,
        String traceId
) {
}
