package com.codewithpcodes.oikon.kafka;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class AuditProducer {

    private final KafkaTemplate<String, AuditEventMessage> kafkaTemplate;
    private static final String TOPIC = "audit.events";

    /**
     * Publishes an audit log to Kafka asynchronously.
     * @param service Name of the microservice (e.g., "LEDGER-SERVICE")
     * @param action Business action (e.g., "TRANSFER_INITIATED")
     * @param username Who performed the action
     * @param status Result of the action (SUCCESS, FAILURE, SECURITY_ALERT)
     * @param metadata Extra details (Amount, IP Address, Recipient ID)
     */
    public void recordEvent(
            String service,
            String action,
            String username,
            String status,
            Map<String, Object> metadata
    ) {

        //1. Generate a unique trace ID for each audit log
        String traceId = UUID.randomUUID().toString();

        //2. Build the payload

        AuditEventMessage payload = new AuditEventMessage(service, action, username, status, traceId, metadata);

        //3. Build the kafka message
        Message<AuditEventMessage> message = MessageBuilder
                .withPayload(payload)
                .setHeader(KafkaHeaders.TOPIC, TOPIC)
                .setHeader(KafkaHeaders.KEY, username)
                .build();

        kafkaTemplate.send(message)
                .whenComplete((result, ex) -> {
                    if (ex != null) {
                        log.error("Failed to publish audit event: {}", ex.getMessage());
                    } else {
                        log.debug("Audit log sent: {} | {}", action, status);
                    }
                });
    }
}
