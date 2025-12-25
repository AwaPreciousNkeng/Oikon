package com.codewithpcodes.oikon.eventListeners;

import com.codewithpcodes.oikon.domainEvents.EmailVerificationRequestedEvent;
import com.codewithpcodes.oikon.domainEvents.MfaTokenGeneratedEvent;
import com.codewithpcodes.oikon.kafka.EmailNotificationProducer;
import com.codewithpcodes.oikon.kafka.EmailNotificationRequest;
import com.codewithpcodes.oikon.kafka.EmailNotificationType;
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

    //We'll run this in a separate thread so the user doesn't have to wait
    @Async
    @EventListener
    public void handleMfaTokenEvent(MfaTokenGeneratedEvent event) {

        String traceId = UUID.randomUUID().toString();

        //1. Prepare the payload
        var requestBuilder = new EmailNotificationRequest(
                event.getUsername(),
                event.getPin(),
                EmailNotificationType.MFA_TOKEN_GENERATION,
                "Multifactor Authentication Token",
                traceId
        );

        try {
            emailNotificationProducer.sendEmailNotification(requestBuilder);
            log.info("MFA OTP sent to user: {} with traceId: {}", event.getUsername(), traceId);
        } catch (Exception e) {
            log.error("Failed to send MFA OTP to user: {}", event.getUsername(), e);
        }
    }

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
}
