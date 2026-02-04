package com.codewithpcodes.oikon.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record AuthenticationRequest(
        @NotBlank(message = "Email is required.")
        @Email(message = "Email should be valid.")
        String email,
        @NotBlank(message = "Password is required.")
        @Size(min = 6, max = 64, message = "Password must be at least 6 characters")
        String password
) {
}
