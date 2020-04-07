package org.sfvl;

import org.sfvl.doctesting.MainDocumentation;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

public class GameOfLifeDocumentation extends MainDocumentation {

    public GameOfLifeDocumentation() {
        super("Game of life");
    }

    public static void main(String... args) throws IOException {
        final GameOfLifeDocumentation generator = new GameOfLifeDocumentation();

        generator.generate("org.sfvl");
    }

}
