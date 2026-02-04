package com.codewithpcodes.oikon.auth;

import com.codewithpcodes.oikon.validator.Adult;
import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.*;

import java.time.LocalDate;

public record RegisterRequest(
        @NotBlank(message = "First name is required")
        String firstName,
        @NotBlank(message = "Last name is required")
        String lastName,
        @NotBlank(message = "Email is required")
        @Email(message = "Email should be valid")
        String email,
        @NotBlank(message = "Password is required")
        @Size(min = 8, message = "Password must be at least 6 characters long")
        @Pattern(
                regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&#^()_+=\\-]).{8,}$",
                message = "Password must contain at least one uppercase letter, one lowercase letter, one number, and one special character"
        )
        @Pattern(regexp = "^\\S+$", message = "Password must not contain spaces")
        String password,
        @NotBlank(message = "Phone number is required")
        @Pattern(
                regexp = "^\\+[1-9]\\d{7,14}$",
                message = "Phone number has invalid format"
        )
        String phoneNumber,
        @NotNull(message = "Date of birth is required")
        @Past(message = "Date of birth must be in the past")
        @JsonFormat(pattern = "dd/MM/yyyy")
        @Adult()
        LocalDate  dateOfBirth,
        @NotBlank(message = "Street name is required")
        String street,
        @NotBlank(message = "City is required")
        String city,
        @NotBlank(message = "State is required")
        String state,
        @NotBlank(message = "Zip code is required")
        int zipCode,
        @NotBlank(message = "Country is required")
        String country
) {
}
