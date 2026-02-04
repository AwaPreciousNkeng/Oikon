package com.codewithpcodes.oikon.user;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static com.codewithpcodes.oikon.user.Permission.*;

@RequiredArgsConstructor
@Getter
public enum Role {

    USER(Set.of()),
    BUSINESS(Set.of()),

    ADMIN(Set.of(
            ADMIN_READ,
            ADMIN_UPDATE,
            ADMIN_CREATE,
            ADMIN_DELETE
    ));

    private final Set<Permission> permissions;

    public List<SimpleGrantedAuthority> getAuthorities() {

        // Permissions → Authorities
        List<SimpleGrantedAuthority> authorities = permissions.stream()
                .map(permission ->
                        new SimpleGrantedAuthority("PERM_" + permission.getPermission())
                )
                .collect(Collectors.toList());

        // Role → Authority
        authorities.add(
                new SimpleGrantedAuthority("ROLE_" + this.name())
        );

        return authorities;
    }
}
