package com.codewithpcodes.oikon.kafka;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class OtpNotificationRequest {
    private String email;
    private String otpCode;
    private String action;
}
