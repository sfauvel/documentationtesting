package org.demo;

import org.sfvl.doctesting.demo.DemoDocumentation;
import org.sfvl.doctesting.utils.Config;
import org.sfvl.doctesting.writer.Document;

import java.io.IOException;

public class KotlinDocumentation extends DemoDocumentation {

    public KotlinDocumentation() {
        super("Kotlin");
    }

    @Override
    public void produce() throws IOException {
        new Document(this.build()).saveAs(Config.DOC_PATH.resolve("index.adoc"));
    }

    public static void main(String... args) throws IOException {
        new KotlinDocumentation().produce();
    }
}
