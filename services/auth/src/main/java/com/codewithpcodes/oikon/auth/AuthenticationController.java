package com.codewithpcodes.oikon.auth;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthenticationController {

    private final AuthenticationService service;
    /**
     * Endpoint for user registration.
     */
    @PostMapping("/register")
    public ResponseEntity<AuthenticationResponse> register(
            @Valid @RequestBody RegisterRequest request
    ) {
        AuthenticationResponse response = service.register(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    /**
     * 2. AUTHENTICATE (Login Phase 1)
     * Checks Email & Password. Checks if Account is Locked.
     * Triggers MFA if enabled.
     * Returns: { "mfa_enabled": true } if PIN sent, OR Tokens if no MFA.
     */
    @PostMapping("/authenticate")
    public ResponseEntity<AuthenticationResponse> authenticate(
            @Valid @RequestBody AuthenticationRequest request
    ) {
        // If authentication fails, the AuthenticationManager handles the exception (e.g., returns 401 Unauthorized)
        return ResponseEntity.ok(service.authenticate(request));
    }

    /**
     * 3. VERIFY (Login Phase 2)
     * Consumes the 6-digit PIN sent to Email/SMS.
     * Returns: Final JWT Access Tokens.
     */
    @PostMapping("/verify-otp")
    public ResponseEntity<AuthenticationResponse> verify(
            @Valid @RequestBody VerifyRequest request
    ) {
        return ResponseEntity.ok(service.verifyCode(request));
    }


    /**
     * Endpoint for refreshing the JWT access token using a valid refresh token.
     * The service handles reading the token from the header and writing the response.
     */
    @PostMapping("/refresh-token")
    public void refreshToken(
            HttpServletRequest request,
            HttpServletResponse response
    ) throws IOException {
        service.refreshToken(request, response);
    }

    /**
     * Endpoint for refreshing the JWT access token using a valid refresh token.
     * The service handles reading the token from the header and writing the response.
     */
    @PostMapping("/verify-email")
    public void verifyEmailVerificationToken(@RequestBody @Valid VerifyRequest request) {
        service.verifyEmailVerificationToken(request);
    }
}
