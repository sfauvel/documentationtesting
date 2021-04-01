package org.sfvl.application.fizzbuzz;

import org.sfvl.doctesting.DemoDocumentation;
import org.sfvl.doctesting.Document;

import java.io.IOException;
import java.nio.file.Paths;

public class FizzBuzzDocumentation extends DemoDocumentation {

    public FizzBuzzDocumentation() {
        super("FizzBuzz");
    }

    public static void main(String... args) throws IOException {
        new Document(FizzBuzzDocumentation.class).saveAs(Paths.get("Documentation.adoc"));
    }

}
