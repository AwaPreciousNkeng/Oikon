package com.codewithpcodes.oikon.mfa;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.ott.OneTimeToken;
import org.springframework.security.web.authentication.ott.OneTimeTokenGenerationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.io.IOException;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class OttSuccessHandler implements OneTimeTokenGenerationSuccessHandler {

    private static final Logger log = LoggerFactory.getLogger(OttSuccessHandler.class);
    private final ApplicationEventPublisher publisher;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void handle(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            OneTimeToken oneTimeToken
    ) throws IOException, ServletException {
        String username = oneTimeToken.getUsername();
        String pin = oneTimeToken.getTokenValue();

        log.info("MFA triggered for user: {}", username);
        //1. Publish the event
        MfaTokenGeneratedEvent event = new MfaTokenGeneratedEvent(this, username, pin);
        publisher.publishEvent(event);

        //2. Sends a JSON response
        response.setStatus(HttpServletResponse.SC_OK);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);

        Map<String, String> responseBody = Map.of(
                "message", "OTP sent successfully. Please check your email/SMS.",
                "expiry", oneTimeToken.getExpiresAt().toString()
        );

        objectMapper.writeValue(response.getWriter(), responseBody);
    }
}
