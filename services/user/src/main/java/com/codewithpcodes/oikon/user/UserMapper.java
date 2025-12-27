package com.codewithpcodes.oikon.user;

import org.springframework.stereotype.Service;

@Service
public class UserMapper {
    public UserResponse toResponse(User user) {
        return new UserResponse(
                user.getFirstName(),
                user.getLastName(),
                user.getPhoneNumber(),
                user.getEmail(),
                UserStatus.ACTIVE
        );
    }
}
