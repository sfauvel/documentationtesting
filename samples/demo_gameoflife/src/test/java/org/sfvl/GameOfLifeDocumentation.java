package org.sfvl;

import org.sfvl.doctesting.demo.DemoDocumentation;
import org.sfvl.doctesting.writer.Document;

import java.io.IOException;
import java.nio.file.Paths;

public class GameOfLifeDocumentation extends DemoDocumentation {

    public GameOfLifeDocumentation() {
        super("Game of life");
    }

    public static void main(String... args) throws IOException {
        new Document(GameOfLifeDocumentation.class).saveAs(Paths.get("Documentation.adoc"));
    }

}
