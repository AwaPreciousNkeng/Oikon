package com.codewithpcodes.oikon.config;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Service
public class JwtService {

    @Value("application.security.jwt.secret")
    private String secretKey;

    @Value("application.security.jwt.expiration")
    private String jwtExpiration;

    @Value("application.security.jwt.refresh-token.expiration")
    private String refreshExpiration;

    /**
     * Extracts the username (Subject claim) from a given JWT.
     * @param token The JWT string.
     * @return The username.
     */
    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    /**
     * Extracts a specific claim from the JWT using a resolver function.
     * @param token The JWT string.
     * @param claimsResolver Function to resolve the desired claim (e.g., Claims::getExpiration).
     * @return The resolved claim value.
     * @param <T> The type of the claim value.
     */
    private <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    /**
     * Parses the JWT, verifies the signature, and extracts the claims payload.
     * It uses the non-deprecated .verifyWith(Key) method.
     */
    private Claims extractAllClaims(String token) {
        return Jwts
                .parser()
                .verifyWith(getSignInKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    /**
     * Decodes the Base64 secret key and creates an HMAC-SHA SecretKey for signing/verification.
     * @return The SecretKey.
     */
    // required imports:
// import io.jsonwebtoken.io.Decoders;
// import io.jsonwebtoken.security.Keys;
// import javax.crypto.SecretKey;
// import java.nio.charset.StandardCharsets;

    private SecretKey getSignInKey() {
        byte[] keyBytes;

        // try standard Base64
        try {
            keyBytes = Decoders.BASE64.decode(secretKey);
        } catch (Exception e1) {
            // try URL-safe Base64
            try {
                keyBytes = Decoders.BASE64URL.decode(secretKey);
            } catch (Exception e2) {
                // fallback to raw UTF-8 bytes (use only for dev/testing or when secret is actually a plain string)
                keyBytes = secretKey.getBytes(StandardCharsets.UTF_8);
            }
        }

        // Ensure the key is long enough for HS256 (need at least 32 bytes)
        if (keyBytes.length < 32) {
            throw new IllegalArgumentException(
                    "JWT secret is too short (" + keyBytes.length +
                            " bytes). Provide a 32+ byte key (e.g. 32 random bytes base64-encoded)."
            );
        }

        return Keys.hmacShaKeyFor(keyBytes);
    }


    /**
     * Generates a standard access token with no extra claims.
     */
    public String generateToken(UserDetails userDetails) {
        return generateToken(new HashMap<>(), userDetails);
    }

    /**
     * Generates a standard access token with custom extra claims.
     */
    public String generateToken(
            Map<String, Object> extraClaims,
            UserDetails userDetails
    ) {
        return buildToken(extraClaims, userDetails, Long.valueOf(jwtExpiration));
    }

    /**
     * The core method to build and sign a JWT string.
     * It uses the non-deprecated .signWith(Key) method.
     */
    private String buildToken(
            Map<String, Object> extraClaims,
            UserDetails userDetails,
            Long expiration
    ) {
        return Jwts
                .builder()
                .claims(extraClaims)
                .subject(userDetails.getUsername())
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(getSignInKey())
                .compact();
    }

    /**
     * Generates a refresh token.
     */
    public String generateRefreshToken(
            UserDetails userDetails
    ) {
        return buildToken(new HashMap<>(), userDetails, Long.valueOf(refreshExpiration));
    }

    /**
     * Validates if the token is valid for the given user (username match and not expired).
     */
    public boolean isTokenValid(String token, UserDetails userDetails) {
        final String username = extractUsername(token);
        return (username.equals(userDetails.getUsername())) && !isTokenExpired(token);
    }

    /**
     * Checks if the token's expiration date is before the current date.
     */
    private boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    private Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

}
