package org.sfvl;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;

public class Person {
    public static Clock CLOCK = Clock.fixed(Instant.parse("2022-01-22T10:15:30.00Z"), ZoneId.of("UTC"));

    private String firstName;
    private String lastName;

    private LocalDate dateOfBirth;

    public Person(String firstName, String lastName, LocalDate dateOfBirth) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.dateOfBirth = dateOfBirth;
    }

    public int age() {
        return LocalDate.now(CLOCK).getYear()-dateOfBirth.getYear();
    }

    @Override
    public String toString() {
        return firstName + " " +lastName;
    }
}
