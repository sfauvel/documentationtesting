package org.sfvl.demo;

import org.sfvl.doctesting.DemoDocumentation;
import org.sfvl.doctesting.Document;

import java.io.IOException;
import java.nio.file.Paths;

public class TennisDocumentation extends DemoDocumentation {

    public TennisDocumentation() {
        super("Tennis");
    }

    public static void main(String... args) throws IOException {
        new Document(TennisDocumentation.class).saveAs(Paths.get("Documentation.adoc"));
    }
}
