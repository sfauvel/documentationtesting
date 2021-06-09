package org.sfvl.demo;

import org.sfvl.doctesting.demo.DemoDocumentation;
import org.sfvl.doctesting.utils.Config;
import org.sfvl.doctesting.writer.Document;

import java.io.IOException;
import java.nio.file.Paths;

public class TennisDocumentation extends DemoDocumentation {

    public TennisDocumentation() {
        super("Tennis");
    }

    @Override
    public void produce() throws IOException {
        new Document(this.build()).saveAs(Config.DOC_PATH.resolve("index.adoc"));
    }

    public static void main(String... args) throws IOException {
        new TennisDocumentation().produce();
    }
}
