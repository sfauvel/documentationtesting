package org.sfvl.application;

public class Coach {

    private final int seats;
    private int availableSeats;

    public Coach(int seats, int availableSeats) {
        this.seats = seats;
        this.availableSeats = availableSeats;
    }

    public int getSeats() {
        return seats;
    }

    public int getAvailableSeats() {
        return availableSeats;
    }

    public void addSeats(int seats) {
        availableSeats -= seats;
    }
}
