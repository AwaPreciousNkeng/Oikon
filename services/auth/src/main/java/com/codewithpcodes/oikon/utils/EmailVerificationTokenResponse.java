package com.codewithpcodes.oikon.utils;

import java.time.Instant;

public record EmailVerificationTokenResponse(
        String token,
        String username,
        Instant expiresAt
) {
}
