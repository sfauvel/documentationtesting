package org.sfvl.application;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.sfvl.doctesting.utils.NoTitle;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Train tickets can be booked in advance, though we do reserve some
 * capacity in each coach for people who have tickets but didn't
 * reserve a seat.
 *
 * From link:https://cucumber.io/blog/open-source/announcing-cucumber-ruby-4-0-0rc2/[Gherkin Rules and Examples].
 */
@DisplayName("Book train ticket")
public class TrainBookTest extends DemoBaseClass {

    @Test
    @NoTitle
    public void note_demo() {
        super.note_demo();
    }

    /**
     * This allows people who show up just before the train leaves
     * to get a seat.
     */
    @Nested
    @DisplayName("Max 70% of entire train can be booked")
    public class Max_70_percent {

        @Test
        public void train_is_too_full_to_place_a_group() {
            Train train = new Train(new Coach(10, 3), new Coach(10, 6));
            book_for(train, 4);
        }

        @Test
        public void train_has_enough_space_left() {
            Train train = new Train(new Coach(10, 5), new Coach(10, 6));
            book_for(train, 2);
        }

    }

    private void book_for(Train train, int seats_to_book) {

        final String trainBeforeReservation = format(train);
        Train.BookingResult result = train.book(seats_to_book);
        final String trainAfterReservation = format(train);

        final String resultMessage;
        if (result.isOk()) {
            resultMessage = "Reservation is *confirmed*";
        } else {
            resultMessage = String.join("\n",
                    "Reservation is *rejected*",
                    "",
                    "Reason is *" + result.reason() + "*");
        }
        doc.write(".Train before reservation", trainBeforeReservation,
                String.format("I book a reservation for *%d* people", seats_to_book),
                "",
                "====",
                resultMessage,
                "====",
                "",
                ".Train after reservation", trainAfterReservation);
    }

    public String format(Train train) {
        final List<String> coaches_string = train.coaches().stream()
                .map(coach -> format(coach, 2))
                .collect(Collectors.toList());

        String result = String.join("\n",
                ":table-caption:",
                "",
                "[%autowidth, cols=\"1,1\"]",
                "|====",
                coaches_string.stream()
                        .map(line -> "| " + line)
                        .collect(Collectors.joining("\n")),
                "|====\n");

        return result;
    }

    public String format(Coach coach) {
        final int seats = coach.getSeats();
        final int bookedSeats = seats - coach.getAvailableSeats();
        return IntStream.range(0, seats)
                .mapToObj(seat -> (seat < bookedSeats) ? "*" : "0")
                .collect(Collectors.joining());
    }

    public String format(Coach coach, int line_number) {
        final int seats = coach.getSeats();
        final int bookedSeats = seats - coach.getAvailableSeats();
        String[] lines = new String[line_number];
        for (int i = 0; i < lines.length; i++) {
            lines[i] = "";
        }

        for (int seat = 0; seat < seats; seat++) {
            lines[seat % line_number] += (seat < bookedSeats) ? "&#x2612;" : "&#x2610;";
        }

        return Arrays.stream(lines).collect(Collectors.joining(" +\n"));
    }
}