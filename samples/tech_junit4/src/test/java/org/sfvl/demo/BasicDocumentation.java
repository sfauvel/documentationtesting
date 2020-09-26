package org.sfvl.demo;

import org.sfvl.doctesting.MainDocumentation;

import java.io.IOException;

public class BasicDocumentation extends MainDocumentation {

    public BasicDocumentation() {
        super("Using Approvals");
    }

    public static void main(String... args) throws IOException {
        final BasicDocumentation generator = new BasicDocumentation();

        generator.generate("org.sfvl");
    }
}
