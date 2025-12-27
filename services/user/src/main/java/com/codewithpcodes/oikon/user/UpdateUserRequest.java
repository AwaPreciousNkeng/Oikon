package com.codewithpcodes.oikon.user;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record UpdateUserRequest(
        @NotNull(message = "User id is required.")
        UUID userId,
        @NotNull(message = "First name is required.")
        String firstName,
        @NotNull(message = "Last name is required.")
        String lastName,
        @NotNull(message = "Phone number is required.")
        String phoneNumber,
        @NotNull(message = "Email is required.")
        @Email(message = "Email should be valid.")
        String email
) {

}
