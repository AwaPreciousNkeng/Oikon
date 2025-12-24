package com.codewithpcodes.oikon.audit;

public enum AuditStatus {
    SUCCESS,
    FAILURE,
    WARNING,
    SECURITY_ALERT,    // For Brute Force / Intrusion attempts
    SUSPICIOUS         // For weird behavior (e.g., login from a new country)
}
