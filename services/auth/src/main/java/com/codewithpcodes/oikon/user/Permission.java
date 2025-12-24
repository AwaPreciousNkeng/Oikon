package com.codewithpcodes.oikon.user;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum Permission {

    // ===== User Management =====
    USER_READ("user:read"),
    USER_CREATE("user:create"),
    USER_UPDATE("user:update"),
    USER_DELETE("user:delete"),

    // ===== Audit =====
    AUDIT_READ("audit:read"),
    AUDIT_EXPORT("audit:export"),

    // Define specific permissions for the Admin role's management API
    ADMIN_READ("admin:read"),
    ADMIN_UPDATE("admin:update"),
    ADMIN_CREATE("admin:create"),
    ADMIN_DELETE("admin:delete");


    private final String permission;
}
