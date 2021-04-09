package org.sfvl;

import org.sfvl.doctesting.demo.DemoDocumentation;
import org.sfvl.doctesting.writer.Document;

import java.io.IOException;
import java.nio.file.Paths;

public class GameOfLifeDocumentation extends DemoDocumentation {

    public GameOfLifeDocumentation() {
        super("Game of life");
    }

    @Override
    public void produce() throws IOException {
        new Document(this.build()).saveAs(Paths.get("").resolve("Documentation.adoc"));
    }

    public static void main(String... args) throws IOException {
        new GameOfLifeDocumentation().produce();
    }

}
