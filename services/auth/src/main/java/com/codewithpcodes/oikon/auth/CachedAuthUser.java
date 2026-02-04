package com.codewithpcodes.oikon.auth;

import com.codewithpcodes.oikon.user.Role;
import com.codewithpcodes.oikon.user.User;
import lombok.Builder;
import lombok.Data;

import java.util.UUID;

@Data
@Builder
public class CachedAuthUser {

    private UUID userId;
    private String email;
    private Role role;

    private boolean accountNonLocked;
    private boolean emailVerified;

    /**
     * Factory method to safely create a cache projection
     * from the authoritative User entity.
     */
    public static CachedAuthUser from(User user) {
        return CachedAuthUser.builder()
                .userId(user.getId())
                .email(user.getEmail())
                .role(user.getRole())
                .emailVerified(user.isEmailVerified())
                .accountNonLocked(user.isAccountNonLocked())
                .build();
    }
}
