package com.codewithpcodes.oikon.mfa;

import com.codewithpcodes.oikon.kafka.MfaProducer;
import com.codewithpcodes.oikon.kafka.OtpNotificationRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class MfaEventListener {

    private final MfaProducer mfaProducer;

    //We'll run this in a separate thread so the user doesn't have to wait
    @Async
    @EventListener
    public void handleMfaTokenEvent(MfaTokenGeneratedEvent event) {

        //1. Prepare the payload
        var requestBuilder = OtpNotificationRequest.builder()
                .email(event.getUsername())
                .otpCode(event.getPin())
                .action("LOGIN_MFA")
                .build();
        try {
            mfaProducer.sendOtpNotification(requestBuilder);
            log.info("MFA OTP sent to user: {}", event.getUsername());
        } catch (Exception e) {
            log.error("Failed to send MFA OTP to user: {}", event.getUsername(), e);
        }
    }
}
