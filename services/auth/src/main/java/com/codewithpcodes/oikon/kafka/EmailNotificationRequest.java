package com.codewithpcodes.oikon.kafka;


public record EmailNotificationRequest(
         String to,
         String token,
         EmailNotificationType type,
         String subject,
         String traceId
) {

}
