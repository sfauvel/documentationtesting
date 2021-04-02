package org.sfvl.demo;

import org.sfvl.doctesting.demo.DemoDocumentation;
import org.sfvl.doctesting.writer.Document;

import java.io.IOException;
import java.nio.file.Paths;

public class BasicDocumentation extends DemoDocumentation {
    public BasicDocumentation() {
        super("Using Git");
    }

    public static void main(String... args) throws IOException {
        new Document(BasicDocumentation.class).saveAs(Paths.get("Documentation.adoc"));

    }
}
