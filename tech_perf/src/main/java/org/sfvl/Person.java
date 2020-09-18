package org.sfvl;

import java.time.LocalDate;

public class Person {
    private String firstName;
    private String lastName;

    private LocalDate dateOfBirth;

    public Person(String firstName, String lastName, LocalDate dateOfBirth) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.dateOfBirth = dateOfBirth;
    }

    public int age() {
        return LocalDate.now().getYear()-dateOfBirth.getYear();
    }

    @Override
    public String toString() {
        return firstName + " " +lastName;
    }
}
