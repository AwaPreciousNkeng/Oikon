package com.codewithpcodes.oikon.utils;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class PinEmailVerificationTokenService {

    private final StringRedisTemplate redisTemplate;
    private static final String TOKEN_PREFIX = "oikon:email:verification:%s";
    private static final String RATE_PREFIX = "oikon:email:verification:rate:";
    private static final int MAX_TOKENS_PER_WINDOW = 3;
    private final Duration rateWindow = Duration.ofMinutes(5);
    private final Duration tokenExpiresIn = Duration.ofHours(24);

    public EmailVerificationTokenResponse generateToken(String username) {
        Assert.hasText(username, "username cannot be empty");
        enforceRateLimit(username);
        String pin;
        int attempts = 0;
        do {
            pin = generatePin();
            attempts++;
            if (attempts > MAX_TOKENS_PER_WINDOW) {
                throw new IllegalStateException("Failed to generate email verification token after " + MAX_TOKENS_PER_WINDOW + " attempts");
            }
        } while (Boolean.TRUE.equals(redisTemplate.hasKey(String.format(TOKEN_PREFIX, pin))));

        redisTemplate.opsForValue().set(TOKEN_PREFIX + username, pin, tokenExpiresIn.toSeconds(), TimeUnit.SECONDS);

        return new EmailVerificationTokenResponse(pin, username, Instant.now().plus(tokenExpiresIn));

    }

    private String generatePin() {
        return String.format("%06d", (int)(Math.random() * 1_000_000));
    }

    public void enforceRateLimit(String username) {
        String rateKey = RATE_PREFIX + username;
        Long count = redisTemplate.opsForValue().increment(rateKey);
        if (count != null && count == 1) {
            redisTemplate.expire(rateKey, rateWindow);
        }

        if (count != null && count > MAX_TOKENS_PER_WINDOW) {
            throw new IllegalStateException("Too many email verification requests. Please wait for " + rateWindow.toMinutes() + " minutes before retrying.");
        }
    }

    public EmailVerificationTokenResponse consumeToken(String username, String inputPin) {

        if (inputPin == null || username == null) {
            return null;
        }

        String key = String.format(TOKEN_PREFIX, username);
        String storedPin = redisTemplate.opsForValue().get(key);
        if (storedPin == null || !storedPin.equals(inputPin)) {
            return null;
        }
        redisTemplate.delete(key);
        return new EmailVerificationTokenResponse(storedPin, username, Instant.now());
    }
}
