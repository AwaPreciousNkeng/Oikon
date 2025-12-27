package com.codewithpcodes.oikon.user;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;

public record UserCreationRequest(
        @Email(message = "Email should be valid.")
        @NotNull(message = "Email is required.")
        String email,

        @NotNull(message = "First name is required.")
        String firstName,

        @NotNull(message = "Last name is required.")
        String lastName,

        @NotNull(message = "Phone number is required.")
        String phoneNumber
) {
}
