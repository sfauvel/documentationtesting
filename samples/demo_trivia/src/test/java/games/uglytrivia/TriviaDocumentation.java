package com.adaptionsoft.games.uglytrivia;

import org.junit.jupiter.api.TestInfo;
import org.sfvl.doctesting.MainDocumentation;

import java.io.IOException;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class TriviaDocumentation  extends MainDocumentation {

    public TriviaDocumentation() {
        super("Trivia");
    }

    @Override
    protected String getHeader() {

        final Game aGame = new Game();
        final String line = IntStream.range(0, 12)
                .mapToObj(i -> aGame.category(i))
                .distinct()
                .map(category -> String.format("* [%s]#%s#", category.toLowerCase(), category))
                .collect(Collectors.joining("\n"));

        return super.getHeader() +
                "== Legend\n\n" +
                "=== Categories\n\nGame categories: \n\n"+line+"\n\n" +
                "=== Board\n\n" +
                "On the board, the current player is in bold (*Chet*, Pat) +\n" +
                "A player who has a penalty has his name enclosed in square brackets ([Chet]) +\n" +
                "The number of points is displayed behind the player's name (Chet "+ GameTest.getScore(4)+") +\n" +
                "\n";
    }

    public static void main (String... args) throws IOException {
        final TriviaDocumentation documentation = new TriviaDocumentation();
        documentation.generate("com.adaptionsoft.games.uglytrivia", "Trivia");

    }
}
