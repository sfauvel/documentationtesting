package org.sfvl.application.fizzbuzz;

import org.sfvl.doctesting.MainDocumentation;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

public class FizzBuzzDocumentation extends MainDocumentation {

    public FizzBuzzDocumentation() {
        super("FizzBuzz");
    }

    public static void main(String... args) throws IOException {
        final FizzBuzzDocumentation generator = new FizzBuzzDocumentation();

        generator.generate("org.sfvl");
    }

}
