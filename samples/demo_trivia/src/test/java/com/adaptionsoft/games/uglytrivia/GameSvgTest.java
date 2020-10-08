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
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
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



    interface DisplayDoc {
        void displayMulti(FakeGame aGame, Runnable... displayMethods) throws IOException;
        void showAndHideTextMulti(String text);
        void movePlayer(final Game aGame, final String player, int from, int playerHighLighted);
        void displayRollDiceMulti(FakeGame aGame, int roll);
        void displayQuestionAskedMulti(FakeGame aGame, String currentPlayer, Supplier<Boolean> wrongAnswer);

        }

    class DisplayDocSvg implements DisplayDoc {
        public void displayMulti(FakeGame aGame, Runnable... displayMethods) throws IOException {
            displayBoard(aGame, displayMethods);
        }
        public void showAndHideTextMulti(String text) {
            showAndHideTextSvg(text);
        }
        public void movePlayer(final Game aGame, final String player, int from, int playerHighLighted) {
            write("\n\n");
            movePlayerSvg("playerA", from, aGame.places[playerHighLighted]);
        }

        @Override
        public void displayRollDiceMulti(FakeGame aGame, int roll) {
            displayRollDiceSvgXXX(aGame, roll);
        }

        @Override
        public void displayQuestionAskedMulti(FakeGame aGame, String currentPlayer, Supplier<Boolean> wrongAnswer) {
            displayQuestionAskedSvg(aGame, currentPlayer, wrongAnswer);
        }
    }

    class DisplayDocHtml implements DisplayDoc {
        public void displayMulti(FakeGame aGame, Runnable... displayMethods) throws IOException {
            displayInLine(displayMethods);
        }
        public void showAndHideTextMulti(String text) {
            write("[.tableHeader]#" + text + "#\n");
            write("\n\n");
        }
        public void movePlayer(final Game aGame, final String player, int from, int playerHighLighted) {
            displayPosition(aGame, "start", playerHighLighted);
        }

        @Override
        public void displayRollDiceMulti(FakeGame aGame, int roll) {
            displayRollDice(aGame, roll);
        }

        @Override
        public void displayQuestionAskedMulti(FakeGame aGame, String currentPlayer, Supplier<Boolean> wrongAnswer) {
            displayQuestionAsked(aGame, currentPlayer, wrongAnswer);
        }
    }

    /**
     * In a game turn, the player rolls a dice and advances the number of spaces indicated.
     *
     * @throws Exception
     */
    @Test
    @DisplayName("Multi display")
    public void multi_display(TestInfo testInfo) throws Exception {
        {
            write("== Normal move\n\n");
            for (DisplayDoc displayDoc: Arrays.asList(new DisplayDocSvg(), new DisplayDocHtml())) {

                final FakeGame aGame = startGame("Chet");
                final int currentPlayerNumber = aGame.currentPlayer;
                final int roll = 4;
                displayDoc.displayMulti(aGame,
                        () -> {
                            displayDoc.showAndHideTextMulti("Start of the turn");
                            displayDoc.movePlayer(aGame, "playerA", 0, currentPlayerNumber);

                        },
                        () -> {
                            int from = aGame.places[currentPlayerNumber];
                            displayDoc.displayRollDiceMulti(aGame, roll);
                            displayDoc.movePlayer(aGame, "playerA", from, currentPlayerNumber);
                        });
            }
        }

        addStyleSheet();
    }

    @Test
    @DisplayName("Player score")
    public void multi_player_scores(TestInfo testInfo) throws Exception {
        {
            write("== Normal move\n\n");
            for (DisplayDoc displayDoc: Arrays.asList(new DisplayDocSvg(), new DisplayDocHtml())) {

                final FakeGame aGame = startGame("Chet");
                final int currentPlayerNumber = aGame.currentPlayer;
                final String currentPlayer = (String) aGame.players.get(currentPlayerNumber);

                displayDoc.displayMulti(aGame,
                        () -> {
                            displayDoc.showAndHideTextMulti("Start of the turn");
                            displayDoc.movePlayer(aGame, "playerA", 0, currentPlayerNumber);

                        },
                        () -> {
                            int from = aGame.places[currentPlayerNumber];
                            System.out.println("from(1) " + from);
                            displayDoc.displayRollDiceMulti(aGame, 3);
                            displayDoc.movePlayer(aGame, "playerA", from, currentPlayerNumber);
                        },
                        () -> {
                            int from = aGame.places[currentPlayerNumber];
                            System.out.println("from(2) " + from);
                            displayDoc.displayQuestionAskedMulti(aGame, currentPlayer, () -> false);
                            //write("\n\n");
                            displayDoc.movePlayer(aGame, "end", from, currentPlayerNumber);
                        }
                );


            }
        }

        addStyleSheet();
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
                displayBoard(aGame,
                        () -> displayRollDiceSvg(aGame, roll));
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

                displayBoard(aGame,
                        () -> displayRollDiceSvg(aGame, roll));
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
            displayBoard(aGame,
                    () -> displayRollDiceSvg(aGame, roll),
                    () -> {
                        displayQuestionAskedSvg(aGame, (String) aGame.players.get(currentPlayerNumber), wrongAnswer);
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

    /**
     * The first player to score 6 points wins the game.
     *
     * @throws Exception
     */
    @Test
    public void win_the_game(TestInfo testInfo) throws Exception {
        final DocumentationNamer documentationNamer = new DocumentationNamer(getDocPath(), testInfo);

        {
            final FakeGame aGame = startGame("Chet");

            aGame.wasCorrectlyAnswered();
            aGame.wasCorrectlyAnswered();
            aGame.wasCorrectlyAnswered();
            aGame.wasCorrectlyAnswered();
            aGame.wasCorrectlyAnswered();

            final int currentPlayerNumber = aGame.currentPlayer;
            final String currentPlayer = (String) aGame.players.get(currentPlayerNumber);
            final int roll = 3;
            final Supplier<Boolean> wrongAnswer = () -> false;

            aGame.ask = false;
            displayBoard(aGame,
                    () -> displayRollDiceSvg(aGame, roll),
                    () -> {
                        displayQuestionAskedSvg(aGame, (String) aGame.players.get(currentPlayerNumber), wrongAnswer);
                    },
                    () -> {
                        if (!notAWinner) {
                            showAndHideTextSvg(currentPlayer + " wins the game !!!");
                        }
                    }
            );

        }
        {
            final FakeGame aGame = startGame("Chet");

            aGame.wasCorrectlyAnswered();
            aGame.wasCorrectlyAnswered();
            aGame.wasCorrectlyAnswered();
            aGame.wasCorrectlyAnswered();
            aGame.wasCorrectlyAnswered();

            final int playerHighlighted = aGame.currentPlayer;
            String currentPlayer = aGame.players.get(playerHighlighted).toString();

            displayOneTurn(aGame, currentPlayer, () -> 3, () -> false);
            if (!notAWinner) {
                write(currentPlayer + " wins the game !!! +\n");
            }
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

    private void displayBoard(FakeGame aGame, Runnable... displayMethods) throws IOException {

        final int SVG_WIDTH = 800;
        final int SVG_HEIGHT = 200;

        final int firstAnimateCounter = animationCounter;

            fileWriterWrite("++++\n\n");

            fileWriterWrite("<svg version=\"1.1\" " +
                    "xmlns=\"http://www.w3.org/2000/svg\" " +
                    "xmlns:xlink=\"http://www.w3.org/1999/xlink\" " +
                    "width=\"" + SVG_WIDTH + "\" " +
                    "height=\"" + SVG_HEIGHT + "\" " +
                    ">\n");


            fileWriterWrite("<rect x=\"0\" y=\"0\" width=\"" + SVG_WIDTH + "\" height=\"200\" fill=\"white\" stroke=\"black\" stroke-width=\"1\" />\n");
            fileWriterWrite("");

            for (int caseNumber = 0; caseNumber < BOARD_SIZE; caseNumber++) {
                final Position position = new Position(caseNumber);
                fileWriterWrite(displayCase(position.getX(), position.getY(), aGame.category(caseNumber).toLowerCase()));
            }

            final Position position = new Position(aGame.places[aGame.currentPlayer]);
            fileWriterWrite("<circle opacity=\"1\" id=\"b" + boardCounter + "_playerA\" cx=\"" + ((SQUARE_SIZE / 2) + position.getX()) + "\" cy=\"" + ((SQUARE_SIZE / 2) + position.getY()) + "\" r=\"15\" fill=\"grey\" stroke=\"black\" stroke-width=\"1\">\n");
            fileWriterWrite("<set begin=\"b" + boardCounter + "_animEnd.end\" attributeName=\"cx\" to=\"" + ((SQUARE_SIZE / 2) + position.getX()) + "\" repeatCount=\"1\" fill=\"freeze\"/>\n");
            fileWriterWrite("<set begin=\"b" + boardCounter + "_animEnd.end\" attributeName=\"cy\" to=\"" + ((SQUARE_SIZE / 2) + position.getY()) + "\" repeatCount=\"1\" fill=\"freeze\"/>\n");
            fileWriterWrite("</circle>\n");

            displayTextSvg("Game start !", "startGame");
            IntStream.rangeClosed(1, 6).forEach(dice -> displayTextSvg(dice, "dice" + dice));

            for (Runnable displayMethod : displayMethods) {
                displayMethod.run();
            }

            fileWriterWrite("<text id=\"b" + boardCounter + "_text1\" x=\"50%\" y=\"50%\" dominant-baseline=\"middle\" text-anchor=\"middle\" font-family=\"Verdana\" font-size=\"25\" opacity=\"1\">" +
                    "<set begin=\"b" + boardCounter + "_anim" + firstAnimateCounter + ".begin\" attributeName=\"opacity\" to=\"0\" repeatCount=\"1\" fill=\"freeze\"/>" +
                    "<set begin=\"b" + boardCounter + "_anim" + animationCounter + ".end + 1s\" attributeName=\"opacity\" to=\"1\" repeatCount=\"1\" fill=\"freeze\"/>" +
                    "Click to start" +
                    "</text>\n");


            fileWriterWrite("<rect x=\"0\" y=\"0\" width=\"" + SVG_WIDTH + "\" height=\"" + SVG_HEIGHT + "\" opacity=\"0.1\">\n");
            fileWriterWrite("  <animate id=\"b" + boardCounter + "_anim" + firstAnimateCounter + "\" begin=\"click\" attributeName=\"x\" from=\"0\" to=\"0\" dur=\"0.01s\" repeatCount=\"1\" fill=\"freeze\"/>\n");
            fileWriterWrite("  <set begin=\"b" + boardCounter + "_anim" + firstAnimateCounter + ".begin\" attributeName=\"width\" to=\"50\" repeatCount=\"1\" fill=\"freeze\"/>\n");
            fileWriterWrite("  <set begin=\"b" + boardCounter + "_anim" + firstAnimateCounter + ".begin\" attributeName=\"height\" to=\"50\" repeatCount=\"1\" fill=\"freeze\"/>\n");

            fileWriterWrite("  <animate id=\"b" + boardCounter + "_animEnd\" begin=\"b" + boardCounter + "_anim" + animationCounter + ".end + 1s\" attributeName=\"x\" from=\"0\" to=\"0\" dur=\"0.01s\" repeatCount=\"1\" fill=\"freeze\"/>\n");
            fileWriterWrite("  <set begin=\"b" + boardCounter + "_anim" + animationCounter + ".end + 1s\" attributeName=\"width\" to=\"" + SVG_WIDTH + "\" repeatCount=\"1\" fill=\"freeze\"/>\n");
            fileWriterWrite("  <set begin=\"b" + boardCounter + "_anim" + animationCounter + ".end + 1s\" attributeName=\"height\" to=\"" + SVG_HEIGHT + "\" repeatCount=\"1\" fill=\"freeze\"/>\n");

            fileWriterWrite("</rect>\n");

//            final int currentPlayer = aGame.currentPlayer;
//
//            Supplier<Boolean> wrongAnswer = () -> false;
//            displayQuestionAsked(fileWriter, aGame, (String) aGame.players.get(currentPlayer), () -> false);


            displayStyleSvg();
            fileWriterWrite("</svg>\n\n");
            fileWriterWrite("++++\n\n");

//        write(String.format("image:%s[width=%d,height=200,opts=interactive]\n\n", filename, SVG_WIDTH));
    }

    private void displayStyleSvg() {
        fileWriterWrite("<style>\n");
        fileWriterWrite("text {\n");
        fileWriterWrite("font-size: 30px;\n");
        fileWriterWrite("font-weight: bold;\n");
        fileWriterWrite("fill: black;\n");
//            fileWriterWrite("stroke: white;\n");
//            fileWriterWrite("stroke-width: 1px;\n");
        fileWriterWrite("</style>\n");
    }

    private void fileWriterWrite(String text) {
        write(text);
    }


    private void showAndHideTextSvg(String text) {
        fileWriterWrite("<text x=\"50%\" y=\"50%\" dominant-baseline=\"middle\" text-anchor=\"middle\" font-family=\"Verdana\" font-size=\"25\" opacity=\"0\">" +
                text);

        showAndHideTextSvg();

        fileWriterWrite("</text>\n");

    }


    private void showAndHideTextSvg() {
        int lastAnimation;
        int currentAnimation;

        lastAnimation = animationCounter;
        animationCounter++;
        currentAnimation = animationCounter;
        fileWriterWrite("<animate id=\"b" + boardCounter + "_anim" + currentAnimation + "\"" +
                " begin=\"b" + boardCounter + "_anim" + lastAnimation + ".end\"" +
                " attributeName=\"opacity\" from=\"0\" to=\"1\" dur=\"" + "0.2s" + "\" repeatCount=\"1\" fill=\"freeze\"/>\n");

        lastAnimation = animationCounter;
        animationCounter++;
        currentAnimation = animationCounter;
        fileWriterWrite("<animate id=\"b" + boardCounter + "_anim" + currentAnimation + "\"" +
                " begin=\"b" + boardCounter + "_anim" + lastAnimation + ".end + " + "1s" + "\"" +
                " attributeName=\"opacity\" from=\"1\" to=\"0\" dur=\"" + "0.2s" + "\" repeatCount=\"1\" fill=\"freeze\"/>\n");
    }

    private void movePlayerSvg(final String player, int from, int to) {
        if (to < from) {
            to += BOARD_SIZE;
        }
        for (int i = from + 1; i <= to; i++) {
            movePlayerSvg(player, new Position(i % BOARD_SIZE));
        }
    }

    private void movePlayerSvg(final String player, Position position) {

        int x = (SQUARE_SIZE / 2) + position.getX();
        int y = (SQUARE_SIZE / 2) + position.getY();
        ;

        int lastAnimation;
        int currentAnimation;

        lastAnimation = animationCounter;
        animationCounter++;
        currentAnimation = animationCounter;
        fileWriterWrite("<animate id=\"b" + boardCounter + "_anim" + currentAnimation + "\"" +
                " xlink:href=\"#b" + boardCounter + "_" + player + "\"" +
                " begin=\"b" + boardCounter + "_anim" + lastAnimation + ".end\"" +
                " attributeName=\"cx\" to=\"" + x + "\"" +
                " dur=\"" + TEMPO + "\" repeatCount=\"1\" fill=\"freeze\"/>\n");
        fileWriterWrite("<animate " +
                " xlink:href=\"#b" + boardCounter + "_" + player + "\"" +
                " begin=\"b" + boardCounter + "_anim" + lastAnimation + ".end\"" +
                " attributeName=\"cy\" to=\"" + y + "\"" +
                " dur=\"" + TEMPO + "\" repeatCount=\"1\" fill=\"freeze\"/>\n");
    }

    private void displayTextSvg(int text, String id) {
        displayTextSvg(Integer.toString(text), id);
    }

    private void displayTextSvg(String text, String id) {
        fileWriterWrite("<text id=\"b" + boardCounter + "_" +
                id +
                "\" x=\"50%\" y=\"50%\" dominant-baseline=\"middle\" text-anchor=\"middle\" font-family=\"Verdana\" font-size=\"25\" opacity=\"0\">" +
                text +
                "</text>\n");
    }

    private String displayCase(int x, int y, final String category) {
        final HashMap categoryMap = new HashMap() {
            {
                put("pop", "#9e9eff"/*"blue"*/);
                put("sports", "#f4f407"/*"yellow"*/);
                put("science", "#239d23"/*"green"*/);
                put("rock", "#f23939"/*"red"*/);
            }

        };
        return String.format("<rect x=\"%d\" y=\"%d\" width=\"50\" height=\"50\" fill=\"" + categoryMap.get(category) + "\" stroke=\"black\" stroke-width=\"1\" />\n", x, y);
    }

    private void addStyleSheet() throws IOException {
        write(Files.lines(Paths.get("src", "test", "resources", "style.css"))
                .collect(Collectors.joining("\n")));
    }

    private void displayOneTurn(FakeGame aGame, String currentPlayer, Supplier<Integer> rollSupplier, Supplier<Boolean> wrongAnswer) {
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

    private void displayOneTurnSvg(FakeGame aGame, String currentPlayer, Supplier<Integer> rollSupplier, Supplier<Boolean> wrongAnswer) throws IOException {
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

    private void displayQuestionAskedSvg(FakeGame aGame, String currentPlayer, Supplier<Boolean> wrongAnswer) {
        if (aGame.ask) {
            showAndHideTextSvg("Question " + aGame.currentCategory() + "...");

            if (wrongAnswer.get()) {
                showAndHideTextSvg(String.format("%s incorrectly answered to %s question", currentPlayer, aGame.currentCategory()));
                notAWinner = aGame.wrongAnswer();
            } else {
                showAndHideTextSvg(String.format("%s correctly answered to %s question", currentPlayer, aGame.currentCategory()));
                notAWinner = aGame.wasCorrectlyAnswered();
            }
        } else {
            showAndHideTextSvg(String.format("No question for %s", currentPlayer));

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

    private void displayRollDiceSvg(FakeGame aGame, int roll) {

        final int currentPlayerNumber = aGame.currentPlayer;
        final boolean wasInPenaltyBox = aGame.inPenaltyBox[currentPlayerNumber];
        final String currentPlayer = (String) aGame.players.get(currentPlayerNumber);
        final int from = aGame.places[currentPlayerNumber];

        showAndHideTextSvg(currentPlayer + " rolled a " + roll);

        aGame.roll(roll);
        if (wasInPenaltyBox) {
            showAndHideTextSvg(aGame.isGettingOutOfPenaltyBox ? " and is getting out of penality box" : " and is not getting out of the penalty box");
        }
        movePlayerSvg("playerA", from, aGame.places[currentPlayerNumber]);
    }

    private void displayRollDiceSvgXXX(FakeGame aGame, int roll) {

        final int currentPlayerNumber = aGame.currentPlayer;
        final boolean wasInPenaltyBox = aGame.inPenaltyBox[currentPlayerNumber];
        final String currentPlayer = (String) aGame.players.get(currentPlayerNumber);
        final int from = aGame.places[currentPlayerNumber];

        showAndHideTextSvg(currentPlayer + " rolled a " + roll);

        aGame.roll(roll);
        if (wasInPenaltyBox) {
            showAndHideTextSvg(aGame.isGettingOutOfPenaltyBox ? " and is getting out of penality box" : " and is not getting out of the penalty box");
        }

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
