package com.codewithpcodes.oikon.eventListeners;

import com.codewithpcodes.oikon.domainEvents.EmailVerificationRequestedEvent;
import com.codewithpcodes.oikon.domainEvents.MfaTokenGeneratedEvent;
import com.codewithpcodes.oikon.domainEvents.UserCreationEvent;
import com.codewithpcodes.oikon.kafka.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class AuthDomainEventListener {

    private final EmailNotificationProducer emailNotificationProducer;
    private final UserCreationProducer userCreationProducer;

    @Async
    @EventListener
    public void handleEmailVerificationEvent(EmailVerificationRequestedEvent event) {

        String traceId = UUID.randomUUID().toString();
        var requestBuilder = new EmailNotificationRequest(
                event.getUsername(),
                event.getToken(),
                EmailNotificationType.EMAIL_VERIFICATION,
                "Email Verification",
                traceId
        );

        try {
            emailNotificationProducer.sendEmailNotification(requestBuilder);
            log.info("Email verification email sent to user: {} with traceId: {}", event.getUsername(), traceId);
        } catch (Exception e) {
            log.error("Failed to send email verification email to user: {}", event.getUsername(), e);
        }
    }

    @Async
    @EventListener
    public void handleUserCreationEvent(UserCreationEvent event) {

        String traceId = UUID.randomUUID().toString();
        var requestBuilder = new UserCreationRequest(
                event.getUserId(),
                event.getFirstName(),
                event.getLastName(),
                event.getLastName(),
                event.getPhoneNumber(),
                event.getDateOfBirth(),
                event.getZipCode(),
                event.getStreet(),
                event.getCity(),
                event.getState(),
                event.getCountry(),
                traceId
        );

        try {
            userCreationProducer.sendUserCreationInfo(requestBuilder);
            log.info("User creation information sent to user: {} with traceId: {}", event.getUserId(), traceId);
        } catch (Exception e) {
            log.error("Failed to send user creation information to user: {}", event.getUserId(), e);
        }
    }
}
