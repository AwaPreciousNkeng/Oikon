package com.codewithpcodes.oikon.audit;

import java.util.Map;

public record AuditEventMessage(
        String service,
        String action,
        String username,
        String status,
        String traceId,
        Map<String, Object> metadata
) {
}
