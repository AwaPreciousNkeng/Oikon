package com.codewithpcodes.oikon.domainEvents;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

import java.time.LocalDate;
import java.util.UUID;

@Getter
public class UserCreationEvent extends ApplicationEvent {

    private final UUID userId;
    private final String email;
    private final String firstName;
    private final String lastName;
    private final String phoneNumber;
    private final LocalDate dateOfBirth;
    private final String street;
    private final String city;
    private final String state;
    private final String country;
    private final int zipCode;
    public UserCreationEvent(Object source, UUID userId, String email, String firstName, String lastName, String phoneNumber, String street, String city, String state, String country, LocalDate dateOfBirth ,int zipCode) {
        super(source);
        this.userId = userId;
        this.email = email;
        this.firstName = firstName;
        this.lastName = lastName;
        this.phoneNumber = phoneNumber;
        this.dateOfBirth = dateOfBirth;
        this.zipCode = zipCode;
        this.street = street;
        this.city = city;
        this.state = state;
        this.country = country;
    }
}
