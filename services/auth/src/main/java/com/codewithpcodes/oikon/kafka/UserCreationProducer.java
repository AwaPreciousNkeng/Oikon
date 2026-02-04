package com.codewithpcodes.oikon.kafka;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class UserCreationProducer {
    private static final String TOPIC = "user.creation";

    private final KafkaTemplate<String, UserCreationRequest> kafkaTemplate;

    public void sendUserCreationInfo(UserCreationRequest payload) {

        Message<UserCreationRequest> message = MessageBuilder
                .withPayload(payload)
                .setHeader(KafkaHeaders.TOPIC, TOPIC)
                .setHeader(KafkaHeaders.KEY, payload.traceId())
                .build();

        kafkaTemplate.send(message)
                .whenComplete((res, ex) -> {
                    if (ex != null) {
                        log.error("Failed to send user creation information to Kafka {}", ex.getMessage());
                    } else {
                        log.debug("User information sent to Kafka with trace ID {}", payload.traceId());
                    }
                });
    }
}
