package com.codewithpcodes.oikon.domainEvents;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public class EmailVerificationRequestedEvent extends ApplicationEvent {

    private final String username;
    private final String token;

    public EmailVerificationRequestedEvent(Object source, String username, String token) {
        super(source);
        this.token = token;
        this.username = username;
    }
}
