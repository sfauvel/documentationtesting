package org.sfvl.application.fizzbuzz;

import org.junit.jupiter.api.Test;
import org.sfvl.doctesting.junitinheritance.ApprovalsBase;
import org.sfvl.application.FizzBuzz;

import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * link:https://en.wikipedia.org/wiki/Fizz_buzz[Fizz buzz] is a group word game for children to teach them about division.
 *
 * Write a program that prints the numbers from 1 to 100. But for multiples of three print “Fizz” instead of the number
 * and for the multiples of five print “Buzz”. For numbers which are multiples of both three and five print “FizzBuzz “.
 *
 * http://codingdojo.org/kata/FizzBuzz/
 */
public class FizzBuzzTest extends ApprovalsBase {

    @Test
    public void sample_output() {
        write(IntStream.rangeClosed(1, 20)
                .mapToObj(FizzBuzz::get)
                .collect(Collectors.joining(" +\n")));
        write("... etc up to 100");
    }

    /**
     * When number is not divisible by five or three, returns the number.
     */
    @Test
    public void return_given_number() {
        callFizzBuzzWith(1);
        callFizzBuzzWith(2);
        callFizzBuzzWith(4);
        callFizzBuzzWith(22);
    }

    /**
     * When number is divisible by three, returns Fizz.
     */
    @Test
    public void return_fizz_when_divisible_by_three() {
        callFizzBuzzWith(3);
        callFizzBuzzWith(9);
    }

    /**
     * When number is divisible by five, returns Buzz.
     */
    @Test
    public void return_buzz_when_divisible_by_five() {
        callFizzBuzzWith(5);
        callFizzBuzzWith(25);
    }

    /**
     * When number is divisible by three and five, returns FizzBuzz.
     */
    @Test
    public void return_fizzbuzz_when_divisible_by_three_and_five() {
        callFizzBuzzWith(15);
        callFizzBuzzWith(60);
    }


    private void callFizzBuzzWith(int value) {
        write(String.format("FizzBuzz(%d) = %s +\n ", value, FizzBuzz.get(value)));
    }


}