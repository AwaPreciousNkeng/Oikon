package com.codewithpcodes.oikon.mfa;


import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.authentication.ott.*;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class PinOneTimeTokenService implements OneTimeTokenService {

    private final StringRedisTemplate redisTemplate;
    private final SecureRandom secureRandom = new SecureRandom();
    private static final String OTP_PREFIX = "oikon:mfa:otp:%s";
    private static final String RATE_PREFIX = "oikon:mfa:rate:";
    private static final int MAX_GENERATION_ATTEMPTS = 5;
    private static final int MAX_OTPS_PER_WINDOW = 3;
    private Duration rateWindow = Duration.ofMinutes(5);
    private Duration tokenExpiresIn = Duration.ofMinutes(5);

    @Override
    public @Nullable OneTimeToken generate(@NonNull GenerateOneTimeTokenRequest request) {
        Assert.notNull(request, "GenerateOneTimeTokenRequest cannot be null");
        Assert.hasText(request.getUsername(), "username cannot be empty");

        enforceRateLimit(request.getUsername());
        String pin;
        int attempts = 0;
        do {
            pin = generatePin();
            attempts++;
            if (attempts > MAX_GENERATION_ATTEMPTS) {
                throw new IllegalStateException("Failed to generate OTP after " + MAX_GENERATION_ATTEMPTS + " attempts");
            }
        } while (Boolean.TRUE.equals(redisTemplate.hasKey(OTP_PREFIX + pin)));

        //Bind OTP to username
        redisTemplate.opsForValue().set(OTP_PREFIX + request.getUsername(), pin, tokenExpiresIn.toSeconds(), TimeUnit.SECONDS);

        return new DefaultOneTimeToken(pin, request.getUsername(), Instant.now().plus(tokenExpiresIn));
    }

    private void enforceRateLimit(String username) {
        String rateKey = RATE_PREFIX + username;
        Long count = redisTemplate.opsForValue().increment(rateKey);
        if (count != null && count == 1) {
            redisTemplate.expire(rateKey, rateWindow);
        }

        if (count != null && count > MAX_OTPS_PER_WINDOW) {
            throw new IllegalStateException("Too many OTP requests. Please wait for " + rateWindow.toMinutes() + " minutes before retrying.");
        };
    }

    @Override
    public @Nullable OneTimeToken consume(@NonNull OneTimeTokenAuthenticationToken authenticationToken) {
        Assert.notNull(authenticationToken, "authenticationToken cannot be null");

        // We expect the AuthenticationToken to contain the 'username' (principal)
        // because the user must identify themselves to verify the code.
        String inputPin = authenticationToken.getTokenValue();
        String username = (String) authenticationToken.getPrincipal();
        if (inputPin == null || username == null) {
            return null;
        }
        String key = String.format(OTP_PREFIX, username);
        String storedPin = redisTemplate.opsForValue().get(key);
        if (storedPin == null || !storedPin.equals(inputPin)) {
            return null;
        }
        //Delete the Token
        redisTemplate.delete(key);
        return new DefaultOneTimeToken(inputPin, username, Instant.now());
    }

    public void setTokenExpiresIn(Duration tokenExpiresIn) {
        Assert.notNull(tokenExpiresIn, "tokenExpiresIn cannot be null");
        Assert.isTrue(!tokenExpiresIn.isNegative() && !tokenExpiresIn.isZero(),
                "tokenExpiresIn cannot be negative");
        this.tokenExpiresIn = tokenExpiresIn;
    }

    public void setRateWindow(Duration rateWindow) {
        Assert.notNull(rateWindow, "rateWindow cannot be null");
        this.rateWindow = rateWindow;
    }

    private String generatePin() {
        return String.format("%06d", secureRandom.nextInt(1_000_000));
    }
}
