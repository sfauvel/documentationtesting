package org.sfvl.demo;

import org.sfvl.doctesting.demo.DemoDocumentation;
import org.sfvl.doctesting.writer.Document;

import java.io.IOException;
import java.nio.file.Paths;

public class BasicDocumentation extends DemoDocumentation {
    public BasicDocumentation() {
        super("Using Git and Approvals");
    }

    @Override
    public void produce() throws IOException {
        new Document(this.build()).saveAs(Paths.get("").resolve("Documentation.adoc"));
    }

    public static void main(String... args) throws IOException {
        new BasicDocumentation().produce();
    }
}
