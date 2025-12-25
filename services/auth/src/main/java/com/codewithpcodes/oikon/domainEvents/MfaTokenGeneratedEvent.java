package com.codewithpcodes.oikon.domainEvents;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public class MfaTokenGeneratedEvent extends ApplicationEvent {
    private final String username;
    private final String pin;

    public MfaTokenGeneratedEvent(Object source, String username, String pin) {
        super(source);
        this.username = username;
        this.pin = pin;
    }
}
