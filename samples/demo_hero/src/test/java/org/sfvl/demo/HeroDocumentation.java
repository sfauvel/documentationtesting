package org.sfvl.demo;

import org.sfvl.doctesting.demo.DemoDocumentation;
import org.sfvl.doctesting.writer.Document;

import java.io.IOException;
import java.nio.file.Paths;

public class HeroDocumentation extends DemoDocumentation {

    public HeroDocumentation() {
        super("Hero");
    }

    public static void main(String... args) throws IOException {
        new Document(HeroDocumentation.class).saveAs(Paths.get("Documentation.adoc"));
    }
}
