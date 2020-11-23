package com.adaptionsoft.games.uglytrivia;

import org.sfvl.doctesting.DemoDocumentation;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class TriviaDocumentation extends DemoDocumentation {

    public TriviaDocumentation() {
        this("Trivia");
    }

    public TriviaDocumentation(String documentationTitle) {
        super(documentationTitle);
    }

    @Override
    protected String getDocumentOptions() {
        return joinParagraph(
                ":sectnums:\n" + super.getDocumentOptions(),
                "= " + DOCUMENTATION_TITLE);
    }

    @Override
    protected String getHeader() {

        final Game aGame = new Game();
        final String line = IntStream.range(0, 12)
                .mapToObj(i -> aGame.category(i))
                .distinct()
                .map(category -> String.format("* [%s category]#%s#", category.toLowerCase(), category))
                .collect(Collectors.joining("\n"));

        return super.getHeader() +
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
            final TriviaDocumentation documentation = new TriviaDocumentation() {
                @Override
                protected Set<Method> getAnnotatedMethod(Class<? extends Annotation> annotation, String packageToScan) {
                    final Set<Method> annotatedMethod = super.getAnnotatedMethod(annotation, packageToScan);
                    return annotatedMethod.stream()
                            .filter(m -> !m.getDeclaringClass().equals(GameSvgTest.class))
                            .collect(Collectors.toSet());
                }
            };
            documentation.generate("com.adaptionsoft.games.uglytrivia");
        }
        {
            final TriviaDocumentation documentation = new TriviaDocumentation("Trivia with animation") {
                @Override
                protected Set<Method> getAnnotatedMethod(Class<? extends Annotation> annotation, String packageToScan) {
                    final Set<Method> annotatedMethod = super.getAnnotatedMethod(annotation, packageToScan);
                    return annotatedMethod.stream()
                            .filter(m -> m.getDeclaringClass().equals(GameSvgTest.class))
                            .collect(Collectors.toSet());
                }
            };
            documentation.generate("com.adaptionsoft.games.uglytrivia", "DocumentationWithAnimation");
        }

    }
}
