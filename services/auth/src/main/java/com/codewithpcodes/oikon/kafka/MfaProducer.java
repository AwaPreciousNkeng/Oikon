package com.codewithpcodes.oikon.kafka;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class MfaProducer {
    private final KafkaTemplate<String, OtpNotificationRequest> kafkaTemplate;
    public void sendOtpNotification(OtpNotificationRequest otpNotificationRequest) {
        log.info("Sending OTP notification");
        Message<OtpNotificationRequest> message = MessageBuilder
                .withPayload(otpNotificationRequest)
                .setHeader(KafkaHeaders.TOPIC, "otp-notification-topic")
                .setHeader("source", "auth-service")
                .build();
        kafkaTemplate.send(message);
    }
}
