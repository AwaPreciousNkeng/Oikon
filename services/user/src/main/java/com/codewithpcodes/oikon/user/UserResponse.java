package com.codewithpcodes.oikon.user;

public record UserResponse(
        String firstName,
        String lastName,
        String phoneNumber,
        String email,
        UserStatus status
) {
}
