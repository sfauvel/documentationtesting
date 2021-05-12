package com.adaptionsoft.games.uglytrivia;

import org.sfvl.doctesting.utils.ClassFinder;
import org.sfvl.doctesting.demo.DemoDocumentation;
import org.sfvl.doctesting.utils.Config;
import org.sfvl.doctesting.writer.Classes;
import org.sfvl.doctesting.writer.Document;
import org.sfvl.doctesting.writer.Options;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class TriviaDocumentation extends DemoDocumentation {

    public String getContent() {
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

    public String build(String title, List<Class<?>> classesToInclude) {
        return formatter.paragraphSuite(
                new Options(formatter).withCode(),
                formatter.title(1, title),
                getHeader(),
                getContent(),
                new Classes(formatter).includeClasses(
                        docRootPath,
                        classesToInclude
                )
        );
    }

    public static List<Class<?>> getStandardClasses() {
        return new ClassFinder().testClasses(
                TriviaDocumentation.class.getPackage(),
                m -> !m.getDeclaringClass().equals(GameSvgTest.class));
    }

    public static List<Class<?>> getSvgClasses() {
        return Arrays.asList(GameSvgTest.class);
    }

    @Override
    public void produce() throws IOException {
        new Document(this.build("Trivia", getStandardClasses())).saveAs(Config.DOC_PATH.resolve("Documentation.adoc"));

        new Document(this.build("Trivia with animation", getSvgClasses())).saveAs(Config.DOC_PATH.resolve("DocumentationWithAnimation.adoc"));
    }

    public static void main(String... args) throws IOException {
        new TriviaDocumentation().produce();
    }

}
