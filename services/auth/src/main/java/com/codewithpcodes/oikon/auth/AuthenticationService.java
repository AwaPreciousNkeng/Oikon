package com.codewithpcodes.oikon.auth;

import com.codewithpcodes.oikon.config.JwtService;
import com.codewithpcodes.oikon.domainEvents.EmailVerificationRequestedEvent;
import com.codewithpcodes.oikon.exception.DuplicateResourceException;
import com.codewithpcodes.oikon.kafka.AuditProducer;
import com.codewithpcodes.oikon.utils.EmailVerificationTokenResponse;
import com.codewithpcodes.oikon.utils.PinEmailVerificationTokenService;
import com.codewithpcodes.oikon.user.Role;
import com.codewithpcodes.oikon.user.User;
import com.codewithpcodes.oikon.user.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.authentication.ott.GenerateOneTimeTokenRequest;
import org.springframework.security.authentication.ott.OneTimeTokenAuthenticationToken;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthenticationService {

    private static final String SERVICE = "AUTH-SERVICE";

    private final RedisTemplate<String, Object> redisTemplate;
    private final UserRepository repository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final PinEmailVerificationTokenService emailVerificationService;
    private final ApplicationEventPublisher eventPublisher;
    private final AuditProducer auditProducer;

    // =========================================================
    // 1. REGISTRATION (NO JWT HERE)
    // =========================================================
    public AuthenticationResponse register(RegisterRequest request) {

        if (repository.existsByEmail(request.email())) {
            auditProducer.recordEvent(
                    SERVICE,
                    "REGISTRATION_FAILED",
                    request.email(),
                    "FAILURE",
                    Map.of("failureReason", "User email already exists"));
            throw new DuplicateResourceException("Email already in use");
        }

        var user = User.builder()
                .email(request.email())
                .password(passwordEncoder.encode(request.password()))
                .role(Role.USER)
                .enabled(true)
                .emailVerified(false)
                .accountNonLocked(true)
                .failedLoginAttempts(0)
                .build();

        repository.save(user);

        CachedAuthUser cachedUser = CachedAuthUser.builder()
                .userId(user.getId())
                .email(request.email())
                .role(user.getRole())
                .accountNonLocked(user.isAccountNonLocked())
                .emailVerified(false)
                .build();
        redisTemplate.opsForValue().set("auth:user:" + request.email(), cachedUser, Duration.ofMinutes(10));

        EmailVerificationTokenResponse verificationTokenResponse = emailVerificationService.generateToken(request.email());
        if (verificationTokenResponse.token() != null) {
            eventPublisher.publishEvent(
                    new EmailVerificationRequestedEvent(
                            this,
                            user.getEmail(),
                            verificationTokenResponse.token()
                    )
            );
        }
        auditProducer.recordEvent(
                SERVICE,
                "EMAIL_VERIFICATION_SENT & REGISTRATION_SUCCESSFUL",
                request.email(),
                "SUCCESS",
                null
        );

        // Issue the tokens
        var jwtToken = jwtService.generateToken(user);
        var refreshToken = jwtService.generateRefreshToken(user);

        return AuthenticationResponse.builder()
                .accessToken(jwtToken)
                .refreshToken(refreshToken)
                .build();
    }

    // =========================================================
    // 2. LOGIN – PHASE 1 (PASSWORD)
    // =========================================================
    public AuthenticationResponse authenticate(AuthenticationRequest request) {

        //We first check the cache for the info
        String cacheKey = "auth:user:" + request.email();
        CachedAuthUser cachedUser = (CachedAuthUser) redisTemplate.opsForValue().get(cacheKey);

        if (cachedUser != null && !cachedUser.isAccountNonLocked()) {
            auditProducer.recordEvent(
                    SERVICE,
                    "LOGIN_BLOCKED",
                    request.email(),
                    "FAILURE",
                    Map.of("failureReason", "User account is locked(cache)")
            );
            throw new LockedException("User account is locked");
        }

        // Fetch from the db
        User user = repository.findByEmail(request.email())
                .orElseThrow(() -> {
                    auditProducer.recordEvent(
                            SERVICE,
                            "LOGIN_FAILED",
                            request.email(),
                            "FAILURE",
                            Map.of("failureReason", "User can't be found using provided email")
                    );
                    return new UsernameNotFoundException("User not found");
                });

        if (!user.isAccountNonLocked()) {
            if (!IsAccountAutoUnlocked(user)) {
                auditProducer.recordEvent(
                        SERVICE,
                        "LOGIN_FAILED",
                        request.email(),
                        "FAILURE",
                        Map.of(
                                "failureReason", "Account is locked",
                                "lockUntil", user.getLockUntil()
                        )
                );
            }
            throw new LockedException("Account is locked until " + user.getLockUntil());
        }

        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.email(),
                            request.password()
                    )
            );

            if (user.getFailedLoginAttempts() > 0) {
                user.setFailedLoginAttempts(0);
                repository.save(user);
            }

        } catch (BadCredentialsException ex) {
            increaseFailedAttempts(user);
            auditProducer.recordEvent(
                    SERVICE,
                    "LOGIN_FAILED",
                    request.email(),
                    "FAILURE",
                    Map.of(
                            "failureReason", "Invalid credentials",
                            "attempts", user.getFailedLoginAttempts()
                    )
            );
            throw ex;
        }

        //Update cache
        CachedAuthUser updatedCache = CachedAuthUser.from(user);
        redisTemplate.opsForValue().set(cacheKey, updatedCache, Duration.ofMinutes(10));

        // Issue the tokens
        var accessToken = jwtService.generateToken(user);
        var refreshToken = jwtService.generateRefreshToken(user);

        return AuthenticationResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .build();
    }

    // =========================================================
    // 6. VERIFY EMAIL VERIFICATION
    // =========================================================

    public void verifyEmailVerificationToken(VerifyRequest request) {
        var verificationTokenResponse = emailVerificationService.consumeToken(request.getEmail(), request.getPin());
        String token = verificationTokenResponse.token();
        String combinedToken = request.getEmail() + ":" + request.getPin();
        if (token == null) {
            auditProducer.recordEvent(
                    SERVICE,
                    "EMAIL_VERIFICATION_FAILED",
                    combinedToken,
                    "FAILURE",
                    Map.of("failureReason", "Token is invalid or expired")
            );
            throw new BadCredentialsException("Invalid or expired PIN");
        }

        User user = repository.findByEmail(request.getEmail())
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        user.setEmailVerified(true);
        repository.save(user);
        auditProducer.recordEvent(
                SERVICE,
                "EMAIL_VERIFIED_SUCCESSFULLY",
                combinedToken,
                "SUCCESS",
                null
        );
    }

    // =========================================================
    // 7. REFRESH TOKEN
    // =========================================================
    public void refreshToken(
            HttpServletRequest request,
            HttpServletResponse response
    ) throws IOException {

        final String authHeader = request.getHeader("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            auditProducer.recordEvent(
                    SERVICE,
                    "REFRESH_TOKEN_FAILED",
                    null,
                    "FAILURE",
                    Map.of("failureReason", "Authorization header is null or does not start with 'Bearer '")
            );
            return;
        }

        final String refreshToken = authHeader.substring(7);
        final String userEmail = jwtService.extractUsername(refreshToken);

        if (userEmail == null) {
            auditProducer.recordEvent(
                    SERVICE,
                    "REFRESH_TOKEN_FAILED",
                    null,
                    "FAILURE",
                    Map.of("failureReason", "User email is null due to invalid or expired token")
            );
            return;
        }

        User user = repository.findByEmail(userEmail)
                .orElseThrow(() -> {
                    auditProducer.recordEvent(
                            SERVICE,
                            "REFRESH_TOKEN_FAILED",
                            userEmail,
                            "FAILURE",
                            Map.of("failureReason", "User not found")
                    );
                    return new UsernameNotFoundException("User not found");
                });

        if (jwtService.isTokenValid(refreshToken, user)) {
            var accessToken = jwtService.generateToken(user);
            auditProducer.recordEvent(
                    SERVICE,
                    "REFRESH_TOKEN_SUCCESS",
                    userEmail,
                    "SUCCESS",
                    null
            );

            var authResponse = AuthenticationResponse.builder()
                    .accessToken(accessToken)
                    .refreshToken(refreshToken)
                    .build();

            new ObjectMapper().writeValue(
                    response.getOutputStream(),
                    authResponse
            );
        }
    }


    // =========================================================
    // INTERNAL HELPERS
    // =========================================================

    private void increaseFailedAttempts(User user) {

        int attempts = user.getFailedLoginAttempts() + 1;
        user.setFailedLoginAttempts(attempts);
        if (attempts >= 5) {
            user.setAccountNonLocked(false);
            user.setLockUntil(Instant.now().plus(Duration.ofMinutes(15)));
            auditProducer.recordEvent(
                    SERVICE,
                    "ACCOUNT_LOCKED",
                    user.getEmail(),
                    "SECURITY_ALERT",
                    Map.of("attempts", attempts)
            );
        }

        repository.save(user);
    }

    /* When the user tries multiple failed login attempts,
    * the account is locked temporarily and unlocked after some time
    * So this function checks if the lock period is over then unlocks it
    */
    private boolean IsAccountAutoUnlocked(User user) {
        Instant lockUntil = user.getLockUntil();
        if (lockUntil == null) {
            return false;
        }
        Instant now = Instant.now();
        if (now.isAfter(lockUntil)) {
            user.setAccountNonLocked(true);
            user.setLockUntil(null);
            user.setFailedLoginAttempts(0);

            repository.save(user);
            auditProducer.recordEvent(
                    SERVICE,
                    "ACCOUNT_AUTO_UNLOCKED",
                    user.getEmail(),
                    "SUCCESS",
                    Map.of("UnlockedAt", now.toString())
            );
            return true;
        }
        return false;
    }
}
