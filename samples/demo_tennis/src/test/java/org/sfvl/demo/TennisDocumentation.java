package org.sfvl.demo;

import org.sfvl.doctesting.MainDocumentation;

import java.io.IOException;

public class TennisDocumentation extends MainDocumentation {

    public TennisDocumentation() {
        super("Tennis");
    }

    public static void main(String... args) throws IOException {
        final TennisDocumentation generator = new TennisDocumentation();

        generator.generate("org.sfvl");
    }
}
