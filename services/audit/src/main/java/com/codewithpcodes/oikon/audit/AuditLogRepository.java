package com.codewithpcodes.oikon.audit;

import org.springframework.data.mongodb.repository.ReactiveMongoRepository;

public interface AuditLogRepository extends ReactiveMongoRepository<AuditLog, String> {

}
