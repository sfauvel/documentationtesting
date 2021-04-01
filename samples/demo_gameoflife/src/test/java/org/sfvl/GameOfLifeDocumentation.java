package org.sfvl;

import org.sfvl.doctesting.DemoDocumentation;
import org.sfvl.doctesting.Document;

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
