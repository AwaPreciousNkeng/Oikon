package com.codewithpcodes.oikon.audit;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

@Service
public class AuditSignatureService {

    private static final String HMAC_ALGO = "HmacSHA256";
    private final SecretKeySpec keySpec;

    public AuditSignatureService(
            @Value("${audit.signature.secret}")
            String secret
    ) {
        this.keySpec = new SecretKeySpec(
                secret.getBytes(StandardCharsets.UTF_8),
                HMAC_ALGO
        );
    }

    public String sign(AuditLog log) {
        String payload = String.join(
                "|",
                log.getServiceName(),
                log.getAction(),
                log.getUsername(),
                log.getStatus().name(),
                log.getTimestamp().toString(),
                log.getTraceId()
        );

        try {
            Mac mac = Mac.getInstance(HMAC_ALGO);
            mac.init(keySpec);
            return Base64.getEncoder().encodeToString(
                    mac.doFinal(payload.getBytes(StandardCharsets.UTF_8))
            );
        } catch (Exception e) {
            throw new IllegalStateException("Failed to sign audit log", e);
        }
    }

}
