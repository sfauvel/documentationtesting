package com.adaptionsoft.games.uglytrivia;

import org.sfvl.doctesting.ClassFinder;
import org.sfvl.doctesting.DemoDocumentation;
import org.sfvl.doctesting.Document;
import org.sfvl.doctesting.DocumentationBuilder;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class TriviaDocumentation extends DemoDocumentation {

    public TriviaDocumentation(String documentationTitle) {
        super(documentationTitle);
    }

    @Override
    protected String getDocumentOptions() {
        return formatter.paragraphSuite(
                ":sectnums:\n" + super.getDocumentOptions(),
                "= " + documentationTitle);
    }

    @Override
    protected String getContent() {

        final Game aGame = new Game();
        final String line = IntStream.range(0, 12)
                .mapToObj(i -> aGame.category(i))
                .distinct()
                .map(category -> String.format("* [%s category]#%s#", category.toLowerCase(), category))
                .collect(Collectors.joining("\n"));

        return super.getContent() +
                "== Legend\n\n" +
                "=== Categories\n\nThe questions asked to the players are chosen from the following categories: \n\n" + line + "\n\n" +
                "=== Board\n\n" +
                "On the board, the current player is in bold: *Chet*, Pat +\n" +
                "A player who has a penalty has his name enclosed in square brackets: [Chet] +\n" +
                "The number of points is displayed behind the player's name: Chet " + GameTest.getScore(4) + " +\n" +
                "\n";
    }

    public static void main(String... args) throws IOException {
        {
            final DocumentationBuilder documentation = new TriviaDocumentation("Trivia")
                    .withClassesToInclude(new ClassFinder().testClasses(
                            TriviaDocumentation.class.getPackage(),
                            m -> !m.getDeclaringClass().equals(GameSvgTest.class))
                    );
            new Document(documentation.build()).saveAs(Paths.get("Documentation.adoc"));
        }
        {
            final DocumentationBuilder documentation = new TriviaDocumentation("Trivia with animation")
                    .withClassesToInclude(GameSvgTest.class);
            new Document(documentation.build()).saveAs(Paths.get("DocumentationWithAnimation.adoc"));
        }

    }
}
