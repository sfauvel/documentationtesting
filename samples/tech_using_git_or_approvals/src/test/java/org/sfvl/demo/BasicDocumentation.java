package org.sfvl.demo;

import org.sfvl.doctesting.DemoDocumentation;
import org.sfvl.doctesting.Document;

import java.io.IOException;
import java.nio.file.Paths;

public class BasicDocumentation extends DemoDocumentation {
    public BasicDocumentation() {
        super("Using Git and Approvals");
    }

    public static void main(String... args) throws IOException {
        new Document(BasicDocumentation.class).saveAs(Paths.get("Documentation.adoc"));

    }
}
