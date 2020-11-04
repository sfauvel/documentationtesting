package org.sfvl.demo;

import org.sfvl.doctesting.DemoDocumentation;

import java.io.IOException;

public class BasicDocumentation extends DemoDocumentation {

    public BasicDocumentation() {
        super("JUnit5 extension");
    }

    public static void main(String... args) throws IOException {
        final BasicDocumentation generator = new BasicDocumentation();

        generator.generate("org.sfvl");
    }
}
