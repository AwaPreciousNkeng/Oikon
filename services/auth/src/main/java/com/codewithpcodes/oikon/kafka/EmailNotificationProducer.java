package com.codewithpcodes.oikon.kafka;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class EmailNotificationProducer {
    private static final String TOPIC = "notification.emails";

    private final KafkaTemplate<String, EmailNotificationRequest> kafkaTemplate;

    public void sendEmailNotification(EmailNotificationRequest payload) {

        String traceId = UUID.randomUUID().toString();

        Message<EmailNotificationRequest> message = MessageBuilder
                .withPayload(payload)
                .setHeader(KafkaHeaders.TOPIC, TOPIC)
                .setHeader(KafkaHeaders.KEY, traceId)
                .build();

        kafkaTemplate.send(message)
                .whenComplete((result, ex) -> {
                    if (ex != null) {
                        log.error("Failed to send email notification to Kafka {}", ex.getMessage());
                    } else {
                        log.debug("Email notification sent to Kafka with trace ID: {}", traceId);
                    }
                });
    }
}
