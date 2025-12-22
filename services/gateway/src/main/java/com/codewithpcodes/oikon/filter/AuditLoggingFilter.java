package com.codewithpcodes.oikon.filter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.server.reactive.ServerHttpRequest;
import reactor.core.publisher.Mono;


@Configuration
public class AuditLoggingFilter {

    public static final Logger logger = LoggerFactory.getLogger(AuditLoggingFilter.class);

    @Bean
    @Order(-1)
    public GlobalFilter globalTraceFilter() {
        return (exchange, chain) -> {
            ServerHttpRequest request = exchange.getRequest();

            //1. Capture details
            String ip = request.getRemoteAddress() != null
                    ? request.getRemoteAddress().getAddress().getHostAddress()
                    : "UNKNOWN";
            request.getMethod();
            String method = request.getMethod().name();
            String path = request.getPath().toString();
            String traceId = exchange.getRequest()
                    .getHeaders()
                    .getFirst("X-Request-ID");
            if (traceId != null) {
                traceId = java.util.UUID.randomUUID().toString();
            }
            long startTime = System.currentTimeMillis();

            //2. Log the incoming request
            logger.info("GATEWAY_ENTRY | Trace: {} | IP: {} | METHOD: {} | PATH: {}", traceId, ip, method, path);

            //3. Process the request and log the result
            String finalTraceId = traceId;
            return chain.filter(exchange).then(Mono.fromRunnable(() -> {
                long duration = System.currentTimeMillis() - startTime;
                int statusCode = exchange.getResponse().getStatusCode() != null
                        ? exchange.getResponse().getStatusCode().value()
                        : 0;
                logger.info("GATEWAY_EXIT | Trace: {} | IP: {} | PATH: {} | STATUS: {} | DURATION: {}ms", finalTraceId, ip, path, statusCode, duration);
            }));
        };
    }
}
