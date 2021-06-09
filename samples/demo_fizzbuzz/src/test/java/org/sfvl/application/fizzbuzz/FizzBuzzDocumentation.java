package org.sfvl.application.fizzbuzz;

import org.sfvl.doctesting.demo.DemoDocumentation;
import org.sfvl.doctesting.utils.Config;
import org.sfvl.doctesting.writer.Document;

import java.io.IOException;
import java.nio.file.Paths;

public class FizzBuzzDocumentation extends DemoDocumentation {

    public FizzBuzzDocumentation() {
        super("FizzBuzz");
    }

    @Override
    public void produce() throws IOException {
        new Document(this.build()).saveAs(Config.DOC_PATH.resolve("index.adoc"));
    }

    public static void main(String... args) throws IOException {
        new FizzBuzzDocumentation().produce();
    }

}
