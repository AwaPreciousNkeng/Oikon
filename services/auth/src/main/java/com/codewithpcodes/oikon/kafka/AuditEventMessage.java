package com.codewithpcodes.oikon.kafka;

import java.util.Map;

public record AuditEventMessage(
        String service,     // e.g., "AUTH-SERVICE"
        String action,      // e.g., "LOGIN_ATTEMPT"
        String username,    // e.g., "pcodes@gmail.com"
        String status,      // e.g., "SUCCESS" or "FAILURE"
        String traceId,     // e.g., "b3-trace-id" (for debugging)
        Map<String, Object> metadata // Flexible payload (failure reason, Amount, Device)
) {
}
