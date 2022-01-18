package org.sfvl.application;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Train {
    private final List<Coach> coaches;

    public Train(Coach... coaches) {
        this.coaches = Arrays.asList(coaches);
    }

    public List<Coach> coaches() {
        return this.coaches;
    }

    public BookingResult book(int seats) {
        final Coach coach = coaches.get(0);
        final double max_capacity = coach.getSeats() * 0.7;
        final int seats_booked = coach.getSeats() - coach.getAvailableSeats();
        if (seats_booked + seats <= max_capacity) {
            coach.addSeats(seats);
            return new BookingResultOk();
        } else {
            return new BookingResultNotEnoughSeats();
        }
    }

    public static  interface BookingResult {
        boolean isOk();
        String reason();
    }

    private class BookingResultOk implements BookingResult {
        @Override
        public boolean isOk() {
            return true;
        }

        @Override
        public String reason() {
            return "";
        }
    }

    private class BookingResultNotEnoughSeats implements BookingResult {
        @Override
        public boolean isOk() {
            return false;
        }

        @Override
        public String reason() {
            return "Maximum booking capacity has been reached";
        }
    }
}
