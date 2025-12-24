package com.codewithpcodes.oikon.kafka;


public record OtpNotificationRequest(
         String email,
         String otpCode,
         String action
) {

}
