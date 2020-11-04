package org.sfvl.application.fizzbuzz;

import org.sfvl.doctesting.DemoDocumentation;

import java.io.IOException;

public class FizzBuzzDocumentation extends DemoDocumentation {

    public FizzBuzzDocumentation() {
        super("FizzBuzz");
    }

    public static void main(String... args) throws IOException {
        final FizzBuzzDocumentation generator = new FizzBuzzDocumentation();

        generator.generate("org.sfvl");
    }

}
