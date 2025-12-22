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
    public void handleAuditEvent(AuditEventMessage evenData) {
        log.info("Received audit event: {}", evenData);

        AuditLog auditLog = AuditLog.builder()
                .serviceName(evenData.service())
                .action(evenData.action())
                .username(evenData.username())
                .status(AuditStatus.valueOf(evenData.status()))
                .metadata(evenData.metadata())
                .traceId(evenData.traceId())
                .timestamp(Instant.now())
                .build();

        auditLog.setSignature(auditSignatureService.sign(auditLog));
        auditLogRepository.save(auditLog)
                .doOnSuccess(saved ->
                        log.debug("Audit log saved successfully: [{}]", saved))
                .doOnError(error ->
                        log.error("Failed to persist audit log: ", error))
                .subscribe();

    }
}
