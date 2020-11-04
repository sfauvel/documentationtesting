package org.sfvl;

import org.sfvl.doctesting.DemoDocumentation;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

public class GameOfLifeDocumentation extends DemoDocumentation {

    public GameOfLifeDocumentation() {
        super("Game of life");
    }

    public static void main(String... args) throws IOException {
        final GameOfLifeDocumentation generator = new GameOfLifeDocumentation();

        generator.generate("org.sfvl");
    }

}
