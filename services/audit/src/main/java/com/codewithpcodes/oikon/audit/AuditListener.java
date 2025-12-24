package com.codewithpcodes.oikon.audit;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuditListener {

    private final AuditLogRepository auditLogRepository;
    private final AuditSignatureService auditSignatureService;

    @KafkaListener(
            topics = "audit.events",
            groupId = "audit-service-group",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void handleAuditEvent(AuditEventMessage eventData) {
        log.info("Processing audit event: [{}] - [{}]", eventData.service(), eventData.action());

        try {
            AuditLog auditLog = AuditLog.builder()
                    .serviceName(eventData.service())
                    .action(eventData.action())
                    .username(eventData.username())
                    .status(AuditStatus.valueOf(eventData.status())) // String -> Enum
                    .metadata(eventData.metadata())
                    .traceId(eventData.traceId())
                    .timestamp(Instant.now())
                    .build();

            // 1. Sign the log (Tamper-Proofing)
            String signature = auditSignatureService.sign(auditLog);
            auditLog.setSignature(signature);

            // If MongoDB fails, this throws an exception, and Kafka will retry the message.
            auditLogRepository.save(auditLog).block();

            log.debug("Audit log persisted successfully.");

        } catch (Exception e) {
            log.error("Failed to process audit event. Message will be retried. Error: {}", e.getMessage());
            // Rethrowing ensures Kafka doesn't commit the offset, triggering a retry
            throw new RuntimeException("Audit Persistence Failed", e);
        }
    }
}