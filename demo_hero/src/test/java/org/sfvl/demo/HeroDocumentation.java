package org.sfvl.demo;

import org.sfvl.doctesting.MainDocumentation;

import java.io.IOException;

public class HeroDocumentation extends MainDocumentation {

    public static void main(String... args) throws IOException {
        final HeroDocumentation generator = new HeroDocumentation();

        generator.generate("org.sfvl");
    }
}
