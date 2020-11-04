package org.sfvl.demo;

import org.sfvl.doctesting.DemoDocumentation;

import java.io.IOException;

public class HeroDocumentation extends DemoDocumentation {

    public HeroDocumentation() {
        super("Hero");
    }

    public static void main(String... args) throws IOException {
        final HeroDocumentation generator = new HeroDocumentation();

        generator.generate("org.sfvl");
    }
}
