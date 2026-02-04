package com.codewithpcodes.oikon.domainEvents;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public class UserUpdateEvent extends ApplicationEvent {

    private final String username;
    private final String
}
