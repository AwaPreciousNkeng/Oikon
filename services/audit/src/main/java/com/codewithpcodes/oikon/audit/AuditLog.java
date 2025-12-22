package com.codewithpcodes.oikon.audit;


import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.Map;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Document(collection = "audit_logs")
public class AuditLog {
    @Id
    private String id;

    private String serviceName;
    private String action;
    private String username;
    private AuditStatus status;
    private String traceId;

    //Contextual data
    private Map<String, Object> metadata;
    private Instant timestamp; //Set by the Audit service only
    /*
     * Integrity signature (HMAC-SHA256 over canonical fields)
     * Prevents silent tampering of audit logs.
     */
    private String signature;
}
