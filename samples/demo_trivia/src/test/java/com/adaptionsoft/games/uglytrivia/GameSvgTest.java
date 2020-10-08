package com.adaptionsoft.games.uglytrivia;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.sfvl.doctesting.ApprovalsBase;
import org.sfvl.doctesting.DocumentationNamer;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.IntStream;


@DisplayName("Explanations and examples")
public class GameSvgTest extends ApprovalsBase {


    private static final int SQUARE_SIZE = 50;
    private static final int BOARD_SIZE = 12;
    private static final String TEMPO = "0.5s";
    private static int animationCounter = 0; // Need to be static to ensure unicity. It may needed to add class name.
    private static int boardCounter = 0; // Need to be static to ensure unicity. It may needed to add class name.
    private boolean notAWinner = true;

    class FakeGame extends Game {

        boolean ask = false;

        @Override
        protected void askQuestion() {
            ask = true;
            super.askQuestion();
        }
    }

    private FakeGame startGame(String... players) {
        final FakeGame aGame = new FakeGame();
        for (String player : players) {
            aGame.add(player);
        }
        return aGame;
    }

    public void displayInLine(Runnable... displayMethods) {
        write(String.format("[.tableInline]\n[%%autowidth, cols=%d, frame=none, grid=none]\n|====\n", displayMethods.length));
        for (Runnable displayMethod : displayMethods) {
            write("\na|");
            displayMethod.run();
        }
        write("\n|====\n");
    }


    /**
     * In a game turn, the player rolls a dice and advances the number of spaces indicated.
     *
     * @throws Exception
     */
    @Test
    @DisplayName("Movements of a player")
    public void player_advances(TestInfo testInfo) throws Exception {
        final DocumentationNamer documentationNamer = new DocumentationNamer(getDocPath(), testInfo);
        {
            write("== Normal move\n\n");
            {
                final FakeGame aGame = startGame("Chet");
                final int currentPlayerNumber = aGame.currentPlayer;
                final int roll = 4;
                displayBoard(documentationNamer, aGame,
                        fileWriter -> displayRollDice(aGame, fileWriter, roll));
            }
            {
                final FakeGame aGame = startGame("Chet");
                final int currentPlayerNumber = aGame.currentPlayer;
                final int roll = 4;

                displayInLine(
                        () -> {
                            write("[.tableHeader]#Start of the turn#\n");
                            write("\n\n");
                            displayPosition(aGame, "start", currentPlayerNumber);
                        },
                        () -> {
                            displayRollDice(aGame, roll);
                            write("\n\n");
                            displayPosition(aGame, "after move", currentPlayerNumber);
                        }
                );
            }
        }
        {
            write("== Move beyond the end\n\n");
            {
                write("\n\nIf he reaches the end of the board, he continues by starting from the beginning\n\n");
                final FakeGame aGame = startGame("Chet");
                final int currentPlayerNumber = aGame.currentPlayer;
                final int roll = 3;
                aGame.roll(4);
                aGame.roll(6);

                displayBoard(documentationNamer, aGame,
                        fileWriter -> displayRollDice(aGame, fileWriter, roll));
            }

            {
                write("\n\nIf he reaches the end of the board, he continues by starting from the beginning\n\n");

                final FakeGame aGame = startGame("Chet");
                final int currentPlayerNumber = aGame.currentPlayer;
                final int roll = 3;
                aGame.roll(4);
                aGame.roll(6);

                displayInLine(
                        () -> {
                            write("[.tableHeader]#Start of the turn#\n");
                            write("\n\n");
                            displayPosition(aGame, "start", currentPlayerNumber);
                        },
                        () -> {
                            displayRollDice(aGame, roll);
                            write("\n\n");
                            displayPosition(aGame, "after move", currentPlayerNumber);
                        }
                );
            }
        }

        addStyleSheet();
    }

    /**
     * After moving, the player must answer a question corresponding to the category of the square where he is located.
     * If he answers correctly, he scores a point.
     *
     * @throws Exception
     */
    @Test
    @DisplayName("Player score")
    public void player_scores(TestInfo testInfo) throws Exception {
        final DocumentationNamer documentationNamer = new DocumentationNamer(getDocPath(), testInfo);

        {
            final FakeGame aGame = startGame("Chet");
            final int currentPlayerNumber = aGame.currentPlayer;
            final int roll = 3;
            final Supplier<Boolean> wrongAnswer = () -> false;

            aGame.ask = false;
            displayBoard(documentationNamer, aGame,
                    fileWriter -> displayRollDice(aGame, fileWriter, roll),
                    fileWriter -> {
                        displayQuestionAsked(fileWriter, aGame, (String) aGame.players.get(currentPlayerNumber), wrongAnswer);
                    });


        }
        {
            final FakeGame aGame = startGame("Chet");

            final int currentPlayerNumber = aGame.currentPlayer;
            String currentPlayer = aGame.players.get(currentPlayerNumber).toString();

            displayOneTurn(aGame, currentPlayer, () -> 3, () -> false);
        }
        addStyleSheet();
    }

    static class SvgWriter extends FileWriter {
        public SvgWriter(File file) throws IOException {
            super(file);
        }

        @Override
        public void write(String str) {
            try {
                super.write(str);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    static class Position {

        private final int y;
        private final int x;

        public Position(int number) {
            switch (number) {
                case 0:
                    x = 0;
                    y = 0;
                    break;
                case 1:
                    x = 1;
                    y = 0;
                    break;
                case 2:
                    x = 2;
                    y = 0;
                    break;
                case 3:
                    x = 3;
                    y = 0;
                    break;
                case 4:
                    x = 3;
                    y = 1;
                    break;
                case 5:
                    x = 3;
                    y = 2;
                    break;
                case 6:
                    x = 3;
                    y = 3;
                    break;
                case 7:
                    x = 2;
                    y = 3;
                    break;
                case 8:
                    x = 1;
                    y = 3;
                    break;
                case 9:
                    x = 0;
                    y = 3;
                    break;
                case 10:
                    x = 0;
                    y = 2;
                    break;
                case 11:
                    x = 0;
                    y = 1;
                    break;
                default:
                    x = 0;
                    y = 0;
                    new RuntimeException("Invalid position");
            }
        }

        public int getX() {
            return x * SQUARE_SIZE;
        }

        public int getY() {
            return y * SQUARE_SIZE;
        }

    }

    private void displayBoard(DocumentationNamer documentationNamer, FakeGame aGame, Consumer<SvgWriter>... displayMethods) throws IOException {

        final String filename = "board" + (boardCounter++) + ".svg";
        final int SVG_WIDTH = 800;
        final int SVG_HEIGHT = 200;

        final Path filePath = Paths.get(documentationNamer.getSourceFilePath(), filename);

        final int firstAnimateCounter = animationCounter;

        try (SvgWriter fileWriter = new SvgWriter(filePath.toFile())) {
            fileWriter.write("<!DOCTYPE svg PUBLIC \"-//W3C//DTD SVG 1.1//EN\" \"http://www.w3.org/Graphics/SVG/1.1/DTD/svg11.dtd\">\n");

            fileWriter.write("<svg version=\"1.1\" " +
                    "xmlns=\"http://www.w3.org/2000/svg\" " +
                    "xmlns:xlink=\"http://www.w3.org/1999/xlink\" " +
                    "width=\"" + SVG_WIDTH + "\" " +
                    "height=\"" + SVG_HEIGHT + "\" " +
                    ">\n");


            fileWriter.write("<rect x=\"0\" y=\"0\" width=\"" + SVG_WIDTH + "\" height=\"200\" fill=\"white\" stroke=\"black\" stroke-width=\"1\" />\n");
            fileWriter.write("");

            for (int caseNumber = 0; caseNumber < BOARD_SIZE; caseNumber++) {
                final Position position = new Position(caseNumber);
                fileWriter.write(displayCase(position.getX(), position.getY(), aGame.category(caseNumber).toLowerCase()));
            }

            final Position position = new Position(aGame.places[aGame.currentPlayer]);
            fileWriter.write("<circle opacity=\"1\" id=\"playerA\" cx=\"" + ((SQUARE_SIZE / 2) + position.getX()) + "\" cy=\"" + ((SQUARE_SIZE / 2) + position.getY()) + "\" r=\"15\" fill=\"grey\" stroke=\"black\" stroke-width=\"1\">\n");
            fileWriter.write("<set begin=\"animEnd.end\" attributeName=\"cx\" to=\"" + ((SQUARE_SIZE / 2) + position.getX()) + "\" repeatCount=\"1\" fill=\"freeze\"/>\n");
            fileWriter.write("<set begin=\"animEnd.end\" attributeName=\"cy\" to=\"" + ((SQUARE_SIZE / 2) + position.getY()) + "\" repeatCount=\"1\" fill=\"freeze\"/>\n");
            fileWriter.write("</circle>\n");

            displayText(fileWriter, "Game start !", "startGame");
            IntStream.rangeClosed(1, 6).forEach(dice -> displayText(fileWriter, dice, "dice" + dice));

            for (Consumer<SvgWriter> displayMethod : displayMethods) {
                displayMethod.accept(fileWriter);
            }

            fileWriter.write("<text id=\"text1\" x=\"50%\" y=\"50%\" dominant-baseline=\"middle\" text-anchor=\"middle\" font-family=\"Verdana\" font-size=\"25\" opacity=\"1\">" +
                    "<set begin=\"anim" + firstAnimateCounter + ".begin\" attributeName=\"opacity\" to=\"0\" repeatCount=\"1\" fill=\"freeze\"/>" +
                    "<set begin=\"anim" + animationCounter + ".end + 1s\" attributeName=\"opacity\" to=\"1\" repeatCount=\"1\" fill=\"freeze\"/>" +
                    "Click to start" +
                    "</text>\n");


            fileWriter.write("<rect x=\"0\" y=\"0\" width=\"" + SVG_WIDTH + "\" height=\"" + SVG_HEIGHT + "\" opacity=\"0.2\">\n");
            fileWriter.write("  <animate id=\"anim" + firstAnimateCounter + "\" begin=\"click\" attributeName=\"x\" from=\"0\" to=\"0\" dur=\"0.01s\" repeatCount=\"1\" fill=\"freeze\"/>\n");
            fileWriter.write("  <set begin=\"anim" + firstAnimateCounter + ".begin\" attributeName=\"width\" to=\"50\" repeatCount=\"1\" fill=\"freeze\"/>\n");
            fileWriter.write("  <set begin=\"anim" + firstAnimateCounter + ".begin\" attributeName=\"height\" to=\"50\" repeatCount=\"1\" fill=\"freeze\"/>\n");

            fileWriter.write("  <animate id=\"animEnd\" begin=\"anim" + animationCounter + ".end + 1s\" attributeName=\"x\" from=\"0\" to=\"0\" dur=\"0.01s\" repeatCount=\"1\" fill=\"freeze\"/>\n");
            fileWriter.write("  <set begin=\"anim" + animationCounter + ".end + 1s\" attributeName=\"width\" to=\"" + SVG_WIDTH + "\" repeatCount=\"1\" fill=\"freeze\"/>\n");
            fileWriter.write("  <set begin=\"anim" + animationCounter + ".end + 1s\" attributeName=\"height\" to=\"" + SVG_HEIGHT + "\" repeatCount=\"1\" fill=\"freeze\"/>\n");

            fileWriter.write("</rect>\n");

//            final int currentPlayer = aGame.currentPlayer;
//
//            Supplier<Boolean> wrongAnswer = () -> false;
//            displayQuestionAsked(fileWriter, aGame, (String) aGame.players.get(currentPlayer), () -> false);


            fileWriter.write("<style>\n");
            fileWriter.write("text {\n");
            fileWriter.write("font-size: 30px;\n");
            fileWriter.write("font-weight: bold;\n");
            fileWriter.write("fill: black;\n");
//            fileWriter.write("stroke: white;\n");
//            fileWriter.write("stroke-width: 1px;\n");
            fileWriter.write("</style>\n");
            fileWriter.write("</svg>\n\n");
        }

        write(String.format("image:%s[width=%d,height=200,opts=interactive]\n\n", filename, SVG_WIDTH));
//        write(String.format("image:%s[width=200,height=200,opts=inline]\n\n", filename));
    }


    private void showAndHideText(SvgWriter fileWriter, String text) {
        fileWriter.write("<text x=\"50%\" y=\"50%\" dominant-baseline=\"middle\" text-anchor=\"middle\" font-family=\"Verdana\" font-size=\"25\" opacity=\"0\">" +
                text);

        showAndHideText(fileWriter);

        fileWriter.write("</text>\n");

    }


    private void showAndHideText(SvgWriter fileWriter) {
        int lastAnimation;
        int currentAnimation;

        lastAnimation = animationCounter;
        animationCounter++;
        currentAnimation = animationCounter;
        fileWriter.write("<animate id=\"anim" + currentAnimation + "\"" +
                " begin=\"anim" + lastAnimation + ".end\"" +
                " attributeName=\"opacity\" from=\"0\" to=\"1\" dur=\"" + "0.2s" + "\" repeatCount=\"1\" fill=\"freeze\"/>\n");

        lastAnimation = animationCounter;
        animationCounter++;
        currentAnimation = animationCounter;
        fileWriter.write("<animate id=\"anim" + currentAnimation + "\"" +
                " begin=\"anim" + lastAnimation + ".end + " + "1s" + "\"" +
                " attributeName=\"opacity\" from=\"1\" to=\"0\" dur=\"" + "0.2s" + "\" repeatCount=\"1\" fill=\"freeze\"/>\n");
    }

    private void movePlayer(SvgWriter fileWriter, final String player, int from, int to) {
        if (to < from) {
            to += BOARD_SIZE;
        }
        for (int i = from + 1; i <= to; i++) {
            movePlayer(fileWriter, player, new Position(i % BOARD_SIZE));
        }
    }

    private void movePlayer(SvgWriter fileWriter, final String player, Position position) {

        int x = (SQUARE_SIZE / 2) + position.getX();
        int y = (SQUARE_SIZE / 2) + position.getY();
        ;

        int lastAnimation;
        int currentAnimation;

        lastAnimation = animationCounter;
        animationCounter++;
        currentAnimation = animationCounter;
        fileWriter.write("<animate id=\"anim" + currentAnimation + "\"" +
                " xlink:href=\"#" + player + "\"" +
                " begin=\"anim" + lastAnimation + ".end\"" +
                " attributeName=\"cx\" to=\"" + x + "\"" +
                " dur=\"" + TEMPO + "\" repeatCount=\"1\" fill=\"freeze\"/>\n");
        fileWriter.write("<animate " +
                " xlink:href=\"#" + player + "\"" +
                " begin=\"anim" + lastAnimation + ".end\"" +
                " attributeName=\"cy\" to=\"" + y + "\"" +
                " dur=\"" + TEMPO + "\" repeatCount=\"1\" fill=\"freeze\"/>\n");
    }

    private void displayText(SvgWriter fileWriter, int text, String id) {
        displayText(fileWriter, Integer.toString(text), id);
    }

    private void displayText(SvgWriter fileWriter, String text, String id) {

//        <svg width="400" height="200" viewBox="-200 0 400 200">
//  <text id="text1" x="50%" y="50%" dominant-baseline="middle" text-anchor="middle" font-family="Verdana" font-size="25" opacity="1"><set begin="anim15.begin" attributeName="opacity" to="0" repeatCount="1" fill="freeze"/><set begin="anim20.end + 1s" attributeName="opacity" to="1" repeatCount="1" fill="freeze"/>Click to start</text>
//</svg>

        fileWriter.write("<text id=\"" +
                id +
                "\" x=\"50%\" y=\"50%\" dominant-baseline=\"middle\" text-anchor=\"middle\" font-family=\"Verdana\" font-size=\"25\" opacity=\"0\">" +
                text +
                "</text>\n");
    }

    private String displayCase(int x, int y, final String category) {
        final HashMap categoryMap = new HashMap() {
            {
                put("pop", "#239d23"/*"green"*/);
                put("sports", "#9e9eff"/*"blue"*/);
                put("science", "#f4f407"/*"yellow"*/);
                put("rock", "#f23939"/*"red"*/);
            }

        };
        return String.format("<rect x=\"%d\" y=\"%d\" width=\"50\" height=\"50\" fill=\"" + categoryMap.get(category) + "\" stroke=\"black\" stroke-width=\"1\" />\n", x, y);
    }

    private void addStyleSheet() throws IOException {
        write(Files.lines(Paths.get("src", "test", "resources", "style.css"))
                .collect(Collectors.joining("\n")));
    }

    private void displayOneTurn(FakeGame aGame, String currentPlayer, Supplier<Integer> rollSupplier, Supplier<Boolean> wrongAnswer) throws IOException {
        final int roll = rollSupplier.get();

        final int currentPlayerNumber = aGame.currentPlayer;

        aGame.ask = false;
        displayInLine(
                () -> {
                    write("[.tableHeader]#Start of the turn#\n");
                    write("\n\n");
                    displayPosition(aGame, "start", currentPlayerNumber);
                },
                () -> {
                    displayRollDice(aGame, roll);
                    write("\n\n");
                    displayPosition(aGame, "after move", currentPlayerNumber);
                },
                () -> {
                    displayQuestionAsked(aGame, currentPlayer, wrongAnswer);
                    write("\n\n");
                    displayPosition(aGame, "end", currentPlayerNumber);
                }
        );
    }

    private void displayQuestionAsked(FakeGame aGame, String currentPlayer, Supplier<Boolean> wrongAnswer) {
        if (aGame.ask) {
            if (wrongAnswer.get()) {
                write(String.format("[wrongAnswer]#&#x2718;#\n%s incorrectly answered to %s question +\n", currentPlayer, aGame.currentCategory()));
                notAWinner = aGame.wrongAnswer();
            } else {
                write(String.format("[rightAnswer]#&#x2714;#\n%s correctly answered to %s question +\n", currentPlayer, aGame.currentCategory()));
                notAWinner = aGame.wasCorrectlyAnswered();
            }
        } else {
            write(String.format("No question for %s +\n", currentPlayer));
        }
    }

    private void displayQuestionAsked(SvgWriter fileWriter, FakeGame aGame, String currentPlayer, Supplier<Boolean> wrongAnswer) {
        if (aGame.ask) {
            showAndHideText(fileWriter, "Question " + aGame.currentCategory() + "...");

            if (wrongAnswer.get()) {
                showAndHideText(fileWriter, String.format("%s incorrectly answered to %s question", currentPlayer, aGame.currentCategory()));
                notAWinner = aGame.wrongAnswer();
            } else {
                showAndHideText(fileWriter, String.format("%s correctly answered to %s question", currentPlayer, aGame.currentCategory()));
                notAWinner = aGame.wasCorrectlyAnswered();
            }
        } else {
            showAndHideText(fileWriter, String.format("No question for %s", currentPlayer));

        }
    }

    private void displayRollDice(FakeGame aGame, int roll) {

        final int currentPlayerNumber = aGame.currentPlayer;
        final boolean wasInPenaltyBox = aGame.inPenaltyBox[currentPlayerNumber];
        final String currentPlayer = (String) aGame.players.get(currentPlayerNumber);

        write(currentPlayer + " rolled a " + dice(roll));
        aGame.roll(roll);
        if (wasInPenaltyBox) {
            write(aGame.isGettingOutOfPenaltyBox ? " and is getting out of penality box" : " and is not getting out of the penalty box");
        }
        write(" +\n");
    }

    private void displayRollDice(FakeGame aGame, SvgWriter fileWriter, int roll) {

        final int currentPlayerNumber = aGame.currentPlayer;
        final boolean wasInPenaltyBox = aGame.inPenaltyBox[currentPlayerNumber];
        final String currentPlayer = (String) aGame.players.get(currentPlayerNumber);
        final int from = aGame.places[currentPlayerNumber];

        showAndHideText(fileWriter, currentPlayer + " rolled a " + roll);

        aGame.roll(roll);
        if (wasInPenaltyBox) {
            showAndHideText(fileWriter, aGame.isGettingOutOfPenaltyBox ? " and is getting out of penality box" : " and is not getting out of the penalty box");
        }
        movePlayer(fileWriter, "playerA", from, aGame.places[currentPlayerNumber]);
    }


    private void displayCategories(Game aGame) {
        final String line = IntStream.range(0, BOARD_SIZE)
                .mapToObj(i -> aGame.category(i))
                .distinct()
                .map(category -> String.format("* [%s]#%s#", category.toLowerCase(), category))
                .collect(Collectors.joining("\n"));

        write("Categories :\n\n" + line + "\n\n");
    }

    private void displayPosition(Game aGame, String startOrEnd, int playerHighLighted) {

        final int columns = BOARD_SIZE;

        write(String.format("[.boardTitle]\nBoard at the %s of the turn\n\n++++\n\n<table class=\"triviaBoard\">\n", startOrEnd));

        Function<Integer, String> tdPosition = position -> String.format("<td class=\"%s\">%s</td>",
                aGame.category(position).toLowerCase(),
                formatPlayers(getListPlayerAt(aGame, position, playerHighLighted), ""));

        int position = 0;
        write("<tr>\n");
        write(tdPosition.apply(0));
        write(tdPosition.apply(1));
        write(tdPosition.apply(2));
        write(tdPosition.apply(3));
        write("</tr>\n");

        write("<tr>\n");
        write(tdPosition.apply(11));
        write("<td>&nbsp;</td>");
        write("<td>&nbsp;</td>");
        write(tdPosition.apply(4));
        write("</tr>\n");

        write("<tr>\n");
        write(tdPosition.apply(10));
        write("<td>&nbsp;</td>");
        write("<td>&nbsp;</td>");
        write(tdPosition.apply(5));
        write("</tr>\n");

        write("<tr>\n");
        write(tdPosition.apply(9));
        write(tdPosition.apply(8));
        write(tdPosition.apply(7));
        write(tdPosition.apply(6));
        write("</tr>\n");

//        write("<tr>\n"+IntStream.range(0, columns)
//                .mapToObj(String.format("<td class=\"%s\">%s</td>",
//                        aGame.category(position).toLowerCase(),
//                        getListPlayerAt(aGame, position, playerHighLighted).stream().collect(Collectors.joining("")))
//                + "\n</tr>\n"));
        write("</table>\n\n++++\n\n");
    }

    private String formatPlayers(List<String> listPlayerAt, String separator) {
        if (listPlayerAt.isEmpty()) {
            return "&nbsp;";
        }

        return listPlayerAt.stream().collect(Collectors.joining(separator));
    }

    private void displayPositionXX(Game aGame, String startOrEnd, int playerHighLighted) throws IOException {
//        write("[%autowidth]\n|====\n");
        final int columns = BOARD_SIZE;
//        write("Board :\n[cols=\"BOARD_SIZE*a\"]\n|====\n");
//        write(IntStream.range(0, columns)
//                .mapToObj(position -> String.format("[.%s.%s]\n%s", aGame.category(position).toLowerCase(), "boardHeader", position))
//                .collect(Collectors.joining("\n| ", "| ", ""))
//                + "\n"
//        );
//
//        write(IntStream.range(0, columns)
//                .mapToObj(position -> String.format("[.%s]\n%s",
//                        aGame.category(position).toLowerCase(),
//                        getListPlayerAt(aGame, position).stream().collect(Collectors.joining(" +\n")))+"&nbsp;\n")
//                .collect(Collectors.joining(" | ", "| ", ""))
//                + "\n"
//        );
//        write("|====\n");

        write(String.format("[.boardTitle]\nBoard at the %s of the turn\n\n++++\n\n<table class=\"triviaBoard\">\n", startOrEnd));
        write("<tr>\n" + IntStream.range(0, columns)
                .mapToObj(position -> String.format("<td class=\"%s %s\">%s</td>", aGame.category(position).toLowerCase(), "boardHeader", position))
                .collect(Collectors.joining("\n"))
                + "\n</tr>\n"
        );

        write("<tr>\n" + IntStream.range(0, columns)
                .mapToObj(position -> String.format("<td class=\"%s\">%s</td>",
                        aGame.category(position).toLowerCase(),
                        formatPlayers(getListPlayerAt(aGame, position, playerHighLighted), "")))
                .collect(Collectors.joining("\n"))
                + "\n</tr>\n"
        );
        write("</table>\n\n++++\n\n");
    }

    private void displayPlayers(Game aGame) throws IOException {
        write("Players : ");
        write(formatPlayers(aGame.players, ", ")
                + "\n\n"
        );
    }

    private void displayScores(Game aGame) throws IOException {
        write("Score :\n[%autowidth]\n|====\n");
        write("| Player " + IntStream.range(0, aGame.howManyPlayers())
                .mapToObj(Integer::toString)
                .collect(Collectors.joining(" | ", "| ", ""))
                + "\n"
        );
        write("| Name " + aGame.players.stream()
                .collect(Collectors.joining(" | ", "| ", ""))
                + "\n"
        );
        write("| Gold " + Arrays.stream(aGame.purses)
                .mapToObj(Integer::toString)
                .collect(Collectors.joining(" | ", "| ", ""))
                + "\n"
        );
        write("|====\n");

    }

    private List<String> getListPlayerAt(Game aGame, int position, int playerHighLighted) {
        final List<String> playerList = new ArrayList<String>();
        for (int player = 0; player < aGame.howManyPlayers(); player++) {
            if (aGame.places[player] == position) {
                String cssClass = (playerHighLighted == player) ? "currentPlayer" : "";
                String playerName = (String) aGame.players.get(player);
                if (aGame.inPenaltyBox[player]) {
                    playerName = "[" + playerName + "]";
                }

                final int gold = aGame.purses[player];
                String textString = "";
                if (gold > 0) {
                    textString = getScore(gold);
                }

                playerList.add(String.format("<p class=\"%s\">%s %s</p>", cssClass, playerName, textString));
            }

        }
        return playerList;
    }

    public static String getScore(int gold) {
        return String.format("&#x%d;", 2779 + gold);
    }

    public static String dice(int value) {
        return String.format("[.dice]#&#x%d;#\n", 2679 + value);
    }

}
