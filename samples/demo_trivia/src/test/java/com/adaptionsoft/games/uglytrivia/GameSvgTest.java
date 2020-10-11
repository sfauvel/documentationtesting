package com.adaptionsoft.games.uglytrivia;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.sfvl.doctesting.ApprovalsBase;
import org.sfvl.doctesting.DocAsTestBase;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
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

    public static class DisplayDocSvg extends DisplayDoc {
        private DocAsTestBase docAsTest;
        private static int boardCounter = 0; // Need to be static to ensure unicity. It may needed to add class name.

        public DisplayDocSvg(DocAsTestBase docAsTest) {
            this.docAsTest = docAsTest;
        }

        @Override
        public void displaySpecificMulti(FakeGame aGame) throws IOException {

//            final List<Runnable> collect = displayMethodsByGroup.stream()
//                    .map(methods -> new Runnable() {
//                        @Override
//                        public void run() {
//                            methods.forEach(m -> m.run());
//                        }
//                    })
//                    .collect(Collectors.toList());
//
//            displayGroup(aGame, collect);

            displayBoard(aGame, displayMethodsByGroup.stream()
                    .map(methods -> new Runnable() {
                        @Override
                        public void run() {
                            displayGroup(aGame, methods);
                        }
                    })
                    .collect(Collectors.toList()).toArray(new Runnable[0]));


//            displayBoard(aGame, displayMethodsByGroup.stream()
//                    .flatMap(Collection::stream)
//                    .collect(Collectors.toList()).toArray(new Runnable[0]));


        }

        private void displayBoard(FakeGame aGame, Runnable... displayMethods) throws IOException {

            final int SVG_WIDTH = 800;
            final int SVG_HEIGHT = 200;

            final int firstAnimateCounter = animationCounter;
            boardCounter++;

            svgWrite("++++\n\n");

            svgWrite("<svg version=\"1.1\" " +
                    "xmlns=\"http://www.w3.org/2000/svg\" " +
                    "xmlns:xlink=\"http://www.w3.org/1999/xlink\" " +
                    "width=\"" + SVG_WIDTH + "\" " +
                    "height=\"" + SVG_HEIGHT + "\" " +
                    ">\n");


            svgWrite("<rect x=\"0\" y=\"0\" width=\"" + SVG_WIDTH + "\" height=\"200\" fill=\"white\" stroke=\"black\" stroke-width=\"1\" />\n");
            svgWrite("");

            for (int caseNumber = 0; caseNumber < BOARD_SIZE; caseNumber++) {
                final Position position = new Position(caseNumber);
                svgWrite(displayCase(position.getX(), position.getY(), aGame.category(caseNumber).toLowerCase()));
            }

            svgWritePlayer(aGame);

            displayTextSvg("Game start !", "startGame");
            IntStream.rangeClosed(1, 6).forEach(dice -> displayTextSvg(dice, "dice" + dice));

            for (Runnable displayMethod : displayMethods) {
                displayMethod.run();
            }

            svgWrite("<text id=\"b" + boardCounter + "_text1\" x=\"50%\" y=\"50%\" dominant-baseline=\"middle\" text-anchor=\"middle\" font-family=\"Verdana\" font-size=\"25\" opacity=\"1\">" +
                    "<set begin=\"b" + boardCounter + "_anim" + firstAnimateCounter + ".begin\" attributeName=\"opacity\" to=\"0\" repeatCount=\"1\" fill=\"freeze\"/>" +
                    "<set begin=\"b" + boardCounter + "_anim" + animationCounter + ".end + 1s\" attributeName=\"opacity\" to=\"1\" repeatCount=\"1\" fill=\"freeze\"/>" +
                    "Click to start" +
                    "</text>\n");


            svgWrite("<rect x=\"0\" y=\"0\" width=\"" + SVG_WIDTH + "\" height=\"" + SVG_HEIGHT + "\" opacity=\"0.1\">\n");
            svgWrite("  <animate id=\"b" + boardCounter + "_anim" + firstAnimateCounter + "\" begin=\"click\" attributeName=\"x\" from=\"0\" to=\"0\" dur=\"0.01s\" repeatCount=\"1\" fill=\"freeze\"/>\n");
            svgWrite("  <set begin=\"b" + boardCounter + "_anim" + firstAnimateCounter + ".begin\" attributeName=\"width\" to=\"50\" repeatCount=\"1\" fill=\"freeze\"/>\n");
            svgWrite("  <set begin=\"b" + boardCounter + "_anim" + firstAnimateCounter + ".begin\" attributeName=\"height\" to=\"50\" repeatCount=\"1\" fill=\"freeze\"/>\n");

            svgWrite("  <animate id=\"b" + boardCounter + "_animEnd\" begin=\"b" + boardCounter + "_anim" + animationCounter + ".end + 1s\" attributeName=\"x\" from=\"0\" to=\"0\" dur=\"0.01s\" repeatCount=\"1\" fill=\"freeze\"/>\n");
            svgWrite("  <set begin=\"b" + boardCounter + "_anim" + animationCounter + ".end + 1s\" attributeName=\"width\" to=\"" + SVG_WIDTH + "\" repeatCount=\"1\" fill=\"freeze\"/>\n");
            svgWrite("  <set begin=\"b" + boardCounter + "_anim" + animationCounter + ".end + 1s\" attributeName=\"height\" to=\"" + SVG_HEIGHT + "\" repeatCount=\"1\" fill=\"freeze\"/>\n");

            svgWrite("</rect>\n");

            displayStyleSvg();
            svgWrite("</svg>\n\n");
            svgWrite("++++\n\n");

//        write(String.format("image:%s[width=%d,height=200,opts=interactive]\n\n", filename, SVG_WIDTH));
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


        private void displayStyleSvg() {
            svgWrite("<style>\n");
            svgWrite("text {\n");
            svgWrite("font-size: 30px;\n");
            svgWrite("font-weight: bold;\n");
            svgWrite("fill: black;\n");
//            fileWriterWrite("stroke: white;\n");
//            fileWriterWrite("stroke-width: 1px;\n");
            svgWrite("</style>\n");
        }


        private void svgWritePlayer(FakeGame aGame) {
            Function<Integer, String> point = p -> String.format("<text id=\"b%d_playerA_%d\" x=\"25\" y=\"25\" dominant-baseline=\"middle\" text-anchor=\"middle\" font-family=\"Verdana\" font-size=\"25\" opacity=\"%d\">%d</text>\n",
                    boardCounter,
                    p,
                    aGame.purses[aGame.currentPlayer] == p ? 1 : 0,
                    p);

            final Position position = new Position(aGame.places[aGame.currentPlayer]);
            svgWrite("<svg id=\"b" + boardCounter + "_playerA\" x=\"" + position.getX() + "\" y=\"" + position.getY() + "\"  ><g>\n");
            svgWrite("<circle opacity=\"1\" cx=\"" + (SQUARE_SIZE / 2) + "\" cy=\"" + (SQUARE_SIZE / 2) + "\" r=\"15\" fill=\"grey\" stroke=\"black\" stroke-width=\"1\">\n");
            svgWrite("</circle>\n");
            IntStream.rangeClosed(0, 6).forEach(p -> svgWrite(point.apply(p)));

            svgWrite(String.format("<rect id=\"b%d_playerA_jail\" x=\"8\" y=\"8\" width=\"34\" height=\"34\" fill=none stroke=\"black\" stroke-width=\"5\" opacity=\"%d\"/>\n",
                    boardCounter, aGame.inPenaltyBox[aGame.currentPlayer] ? 1 : 0));

            svgWrite("</g>");
            svgWrite("<set begin=\"b" + boardCounter + "_animEnd.end\" attributeName=\"x\" to=\"" + position.getX() + "\" repeatCount=\"1\" fill=\"freeze\"/>\n");
            svgWrite("<set begin=\"b" + boardCounter + "_animEnd.end\" attributeName=\"y\" to=\"" + position.getY() + "\" repeatCount=\"1\" fill=\"freeze\"/>\n");
            svgWrite("</svg>\n");

//        fileWriterWrite("<set xlink:href=\"#b" + boardCounter + "_playerA_0\"  attributeName=\"opacity\" to=\"1\" repeatCount=\"1\" fill=\"freeze\"/>\n");

            // Restore value at the end
            displayPoints(aGame, "b" + boardCounter + "_animEnd.end");
            displayPoints(aGame, "b" + boardCounter + "_anim" + animationCounter + ".end");
            displayPenalityBox(aGame, "b" + boardCounter + "_animEnd.end");
        }


        private void svgWrite(String text) {
            docAsTest.write(text);
        }


        private void showAndHideTextSvg(String text) {
            svgWrite("<text x=\"50%\" y=\"50%\" dominant-baseline=\"middle\" text-anchor=\"middle\" font-family=\"Verdana\" font-size=\"25\" opacity=\"0\">" +
                    text);

            showAndHideTextSvg();

            svgWrite("</text>\n");

        }

        private void showAndHideTextSvg() {
            int lastAnimation;
            int currentAnimation;

            lastAnimation = animationCounter;
            animationCounter++;
            currentAnimation = animationCounter;
            svgWrite("<animate id=\"b" + boardCounter + "_anim" + currentAnimation + "\"" +
                    " begin=\"b" + boardCounter + "_anim" + lastAnimation + ".end\"" +
                    " attributeName=\"opacity\" from=\"0\" to=\"1\" dur=\"" + "0.2s" + "\" repeatCount=\"1\" fill=\"freeze\"/>\n");

            lastAnimation = animationCounter;
            animationCounter++;
            currentAnimation = animationCounter;
            svgWrite("<animate id=\"b" + boardCounter + "_anim" + currentAnimation + "\"" +
                    " begin=\"b" + boardCounter + "_anim" + lastAnimation + ".end + " + "1s" + "\"" +
                    " attributeName=\"opacity\" from=\"1\" to=\"0\" dur=\"" + "0.2s" + "\" repeatCount=\"1\" fill=\"freeze\"/>\n");
        }

        private void displayTextSvg(int text, String id) {
            displayTextSvg(Integer.toString(text), id);
        }

        private void displayTextSvg(String text, String id) {
            svgWrite("<text id=\"b" + boardCounter + "_" +
                    id +
                    "\" x=\"50%\" y=\"50%\" dominant-baseline=\"middle\" text-anchor=\"middle\" font-family=\"Verdana\" font-size=\"25\" opacity=\"0\">" +
                    text +
                    "</text>\n");
        }
        private void displayQuestionAskedSvg(FakeGame aGame, String currentPlayer, Supplier<Boolean> wrongAnswer) {
            System.out.println("displayQuestionAskedSvg");
            if (aGame.ask) {
                showAndHideTextSvg("Question " + aGame.currentCategory() + "...");

                if (wrongAnswer.get()) {
                    showAndHideTextSvg(String.format("%s incorrectly answered to %s question", currentPlayer, aGame.currentCategory()));

                    notAWinner = aGame.wrongAnswer();
                    System.out.println(String.format("wrongAnswer :%s", Boolean.toString(notAWinner)));
                } else {
                    showAndHideTextSvg(String.format("%s correctly answered to %s question", currentPlayer, aGame.currentCategory()));
                    notAWinner = aGame.wasCorrectlyAnswered();
                    System.out.println(String.format("wasCorrectlyAnswered :%s", Boolean.toString(notAWinner)));
                }
            } else {
                showAndHideTextSvg(String.format("No question for %s", currentPlayer));
            }
            displayPoints(aGame, "b" + boardCounter + "_anim" + animationCounter + ".end");
            displayPenalityBox(aGame, "b" + boardCounter + "_anim" + animationCounter + ".end");

            if (!notAWinner) {
                showAndHideTextSvg(currentPlayer + " wins the game !!!");
            }

        }

        private void displayPenalityBox(FakeGame aGame, String idToBegin) {
            svgWrite(String.format("<set xlink:href=\"#b%d_playerA_jail\" begin=\"" + idToBegin + "\" attributeName=\"opacity\" to=\"%d\" repeatCount=\"1\" fill=\"freeze\"/>\n",
                    boardCounter,
                    aGame.inPenaltyBox[aGame.currentPlayer] ? 1 : 0));
        }

        private void displayPoints(FakeGame aGame, String idToBegin) {
            for (int i = 0; i < aGame.players.size(); i++) {
                final int playerToDisplay = i;
                IntStream.rangeClosed(0, 6).forEach(p -> {
                    svgWrite(String.format("<set xlink:href=\"#b%d_playerA_%d\" begin=\"" + idToBegin + "\" attributeName=\"opacity\" to=\"%d\" repeatCount=\"1\" fill=\"freeze\"/>\n",
                            boardCounter,
                            p,
                            aGame.purses[playerToDisplay] == p ? 1 : 0));
                });

            }
        }
        @Override
        void displayGroup(FakeGame aGame, List<Runnable> methods) {
            methods.stream().forEach(m -> m.run());
        }

        @Override
        public void text(String text) {
            showAndHideTextSvg(text);
        }

        @Override
        public void move(final Game aGame, final String player, int from, int playerHighLighted) {
            svgWrite("\n\n");
            // showPoints(playerHighLighted);

            int to = aGame.places[playerHighLighted];
            if (to < from) {
                to += BOARD_SIZE;
            }
            for (int i = from + 1; i <= to; i++) {
                movePlayerSvg("playerA", new Position(i % BOARD_SIZE));
            }
        }

        private void movePlayerSvg(final String player, Position position) {

            int x = position.getX();
            int y = position.getY();

            int lastAnimation;
            int currentAnimation;

            lastAnimation = animationCounter;
            animationCounter++;
            currentAnimation = animationCounter;
            svgWrite("<animate id=\"b" + boardCounter + "_anim" + currentAnimation + "\"" +
                    " xlink:href=\"#b" + boardCounter + "_" + player + "\"" +
                    " begin=\"b" + boardCounter + "_anim" + lastAnimation + ".end\"" +
                    " attributeName=\"x\" to=\"" + x + "\"" +
                    " dur=\"" + TEMPO + "\" repeatCount=\"1\" fill=\"freeze\"/>\n");
            svgWrite("<animate " +
                    " xlink:href=\"#b" + boardCounter + "_" + player + "\"" +
                    " begin=\"b" + boardCounter + "_anim" + lastAnimation + ".end\"" +
                    " attributeName=\"y\" to=\"" + y + "\"" +
                    " dur=\"" + TEMPO + "\" repeatCount=\"1\" fill=\"freeze\"/>\n");
        }

        public void showPoints(int player) {
            svgWrite("<set begin=\"b" + boardCounter + "_anim" + animationCounter + ".begin\" attributeName=\"opacity\" to=\"1\" repeatCount=\"1\" fill=\"freeze\"/>\n");

        }

        @Override
        public void roll(FakeGame aGame, int roll) {

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

        @Override
        public void displayQuestionAskedMulti(FakeGame aGame, String currentPlayer, Supplier<Boolean> wrongAnswer) {
            displayQuestionAskedSvg(aGame, currentPlayer, wrongAnswer);
        }

    }

    class DisplayDocHtml extends DisplayDoc {
        @Override
        public void displaySpecificMulti(FakeGame aGame) throws IOException {
            displayMethodsByGroup.forEach(methods -> displayGroup(aGame, methods));
        }

        @Override
        public void displayGroup(FakeGame aGame, List<Runnable> methods) {
            displayInLine(methods.toArray(new Runnable[0]));
        }

        @Override
        public void text(String text) {
            write("[.tableHeader]#" + text + "#\n");
            write("\n\n");
        }

        @Override
        public void move(final Game aGame, final String player, int from, int playerHighLighted) {

            final int columns = BOARD_SIZE;

            write(String.format("[.boardTitle]\nBoard at the %s of the turn\n\n++++\n\n<table class=\"triviaBoard\">\n", "start"));

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

        @Override
        public void roll(FakeGame aGame, int roll) {

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

        @Override
        public void displayQuestionAskedMulti(FakeGame aGame, String currentPlayer, Supplier<Boolean> wrongAnswer) {
            displayQuestionAsked(aGame, currentPlayer, wrongAnswer);
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
            if (!notAWinner) {
                write("*" + currentPlayer + " wins the game !!!* +\n");
            }
        }
    }

    /**
     * In a game turn, the player rolls a dice and advances the number of spaces indicated.
     *
     * @throws Exception
     */
    @Test
    @DisplayName("Movements of a player")
    public void player_advances() throws Exception {

        {
            write("== Normal move\n\n");
            for (DisplayDoc displayDoc : Arrays.asList(new DisplayDocSvg(this), new DisplayDocHtml())) {

                final FakeGame aGame = startGame("Chet");
                final int currentPlayerNumber = aGame.currentPlayer;
                final int roll = 4;
                displayDoc.addAll(Arrays.asList(
                        () -> {
                            displayDoc.text("Start of the turn");
                            displayDoc.move(aGame, "playerA", aGame.places[currentPlayerNumber], currentPlayerNumber);
                        },
                        () -> displayDoc.rollAndMove(aGame, currentPlayerNumber, roll)));
                displayDoc.display(aGame);
            }
        }
        {
            write("== Move beyond the end\n\n");

            write("\n\nIf he reaches the end of the board, he continues by starting from the beginning\n\n");


            for (DisplayDoc displayDoc : Arrays.asList(new DisplayDocSvg(this), new DisplayDocHtml())) {

                final FakeGame aGame = startGame("Chet");
                final int currentPlayerNumber = aGame.currentPlayer;
                final int roll = 3;
                aGame.roll(4);
                aGame.roll(6);
                displayDoc.addAll(Arrays.asList(
                        () -> {
                            displayDoc.text("Start of the turn");
                            displayDoc.move(aGame, "playerA", aGame.places[currentPlayerNumber], currentPlayerNumber);
                        },
                        () -> displayDoc.rollAndMove(aGame, currentPlayerNumber, roll)));
                displayDoc.display(aGame);
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
    public void player_scores() throws Exception {

        for (DisplayDoc displayDoc : Arrays.asList(new DisplayDocSvg(this), new DisplayDocHtml())) {

            final FakeGame aGame = startGame("Chet");

            displayDoc.addAll(displayDoc.displayOneTurnMulti(aGame, 3, () -> false));

            displayDoc.display(aGame);
        }

        addStyleSheet();
    }

    /**
     * The first player to score 6 points wins the game.
     *
     * @throws Exception
     */
    @Test
    public void win_the_game() throws Exception {
        for (DisplayDoc displayDoc : Arrays.asList(new DisplayDocSvg(this), new DisplayDocHtml())) {

            final FakeGame aGame = startGame("Chet");
            aGame.wasCorrectlyAnswered();
            aGame.wasCorrectlyAnswered();
            aGame.wasCorrectlyAnswered();
            aGame.wasCorrectlyAnswered();
            aGame.wasCorrectlyAnswered();

            displayDoc.addAll(displayDoc.displayOneTurnMulti(aGame, 3, () -> false));

            displayDoc.display(aGame);
        }

        addStyleSheet();
    }

    /**
     * When a player gives a wrong answer, he goes to jail.
     * He stays in jail until he rolls an odd number.
     *
     * @throws Exception
     */
    @Test
    public void jail() throws Exception {
        {
            write("== Got to jail\n\n");
            for (DisplayDoc displayDoc : Arrays.asList(new DisplayDocSvg(this), new DisplayDocHtml())) {

                final FakeGame aGame = startGame("Chet");

                displayDoc.addAll(displayDoc.displayOneTurnMulti(aGame, 3, () -> true));

                displayDoc.display(aGame);
            }
        }
        {
            write("== Need an odd number to move\n\n");

            for (DisplayDoc displayDoc : Arrays.asList(new DisplayDocSvg(this), new DisplayDocHtml())) {

                final FakeGame aGame = startGame("Chet");
                aGame.roll(1);
                aGame.wrongAnswer();

                displayDoc.addAll(displayDoc.displayOneTurnMulti(aGame, 2, () -> true));

                displayDoc.addAll(displayDoc.displayOneTurnMulti(aGame, 3, () -> true));
                displayDoc.display(aGame);
            }
        }

        {
            write("== Get out of jail\n\n");
            write("When player correctly answer to a question, he goes out of jail.\n\n");
            for (DisplayDoc displayDoc : Arrays.asList(new DisplayDocSvg(this), new DisplayDocHtml())) {

                final FakeGame aGame = startGame("Chet");
                aGame.wrongAnswer();

                final int playerHighlighted = aGame.currentPlayer;
                String currentPlayer = aGame.players.get(playerHighlighted).toString();

                displayDoc.addAll(displayDoc.displayOneTurnMulti(aGame, 3, () -> false));
                displayDoc.display(aGame);
            }
        }

        addStyleSheet();
    }

    /**
     * When rolled is even, player stay in penality box.
     *
     * @throws Exception
     */
    @Test
    public void player_stay_in_penality_box() throws Exception {
        {
            write("== Need an odd number to move\n\n");

            for (DisplayDoc displayDoc : Arrays.asList(new DisplayDocSvg(this), new DisplayDocHtml())) {

                final FakeGame aGame = startGame("Chet");
                aGame.roll(3);
                aGame.wrongAnswer();

                displayDoc.addAll(displayDoc.displayOneTurnMulti(aGame, 2, () -> true));

                displayDoc.display(aGame);
            }
        }

        addStyleSheet();

    }

    /**
     * This is a full game from the beginning to the end when a player wins the game.
     *
     * @throws Exception
     */
    @Test
    @DisplayName("A full game")
    public void play_until_someone_wins() throws Exception {

//        {
//            write("&nbsp; +\n");
//
//            for (DisplayDoc displayDoc : Arrays.asList(new DisplayDocSvg(this)/*, new DisplayDocHtml()*/)) {
//
//                final FakeGame aGame = startGame("Chet");
//                Random rand = new Random(12345);
//
//                final Supplier<Integer> rollSupplier = () -> rand.nextInt(5) + 1;
//                final Supplier<Boolean> wrongAnswer = () -> rand.nextInt(9) == 7;
//                displayDoc.addAll(Arrays.asList(() -> {
//                    notAWinner = true;
//                    do {
//                        final List<Runnable> runnables = displayDoc.displayOneTurnMulti(aGame, rollSupplier.get(), wrongAnswer);
////                        displayDoc.displayGroup(aGame, runnables);
//                        runnables.forEach(r -> r.run());
//                    } while (notAWinner);
//                }));
//                System.out.println("displayDoc.display(aGame);");
//                displayDoc.display(aGame);
//            }
//        }
        {
            write("&nbsp; +\n");

            for (DisplayDoc displayDoc : Arrays.asList(new DisplayDocSvg(this), new DisplayDocHtml() {
                public void displaySpecificMulti(FakeGame aGame) throws IOException {
                    displayMethodsByGroup.get(0).get(0).run();
                }
            })) {

                final FakeGame aGame = startGame("Chet");
                Random rand = new Random(12345);

                final Supplier<Integer> rollSupplier = () -> rand.nextInt(5) + 1;
                final Supplier<Boolean> wrongAnswer = () -> rand.nextInt(9) == 7;
                displayDoc.addAll(Arrays.asList(() -> {
                    displayDoc.notAWinner = true;
                    int tour = 1;
                    do {
                        final List<Runnable> runnables = displayDoc.displayOneTurnMulti(aGame, rollSupplier.get(), wrongAnswer);
                        write("*Turn " + tour++ + "*\n\n");
                        displayDoc.displayGroup(aGame, runnables);
                        write("\n\n___\n\n");
                    } while (displayDoc.notAWinner);
                }));
                System.out.println("displayDoc.display(aGame);");
                displayDoc.display(aGame);
            }
        }
//        {
//            write("&nbsp; +\n");
//
//            final FakeGame aGame = startGame("Chet", "Pat", "Sue");
//            displayPlayers(aGame);
//
//            Random rand = new Random(12345);
//
//            notAWinner = true;
//            int tour = 1;
//            String currentPlayer;
//            do {
//
//                currentPlayer = aGame.players.get(aGame.currentPlayer).toString();
//                write("*Turn " + tour++ + "*\n\n");
//
//                final Supplier<Integer> rollSupplier = () -> rand.nextInt(5) + 1;
//                final Supplier<Boolean> wrongAnswer = () -> rand.nextInt(9) == 7;
//
//                displayOneTurn(aGame, currentPlayer, rollSupplier, wrongAnswer);
//                write("\n\n___\n\n");
//                if (!notAWinner) {
//                    write(currentPlayer + " wins the game !!! +\n");
//                }
//            } while (notAWinner);
//        }

        addStyleSheet();
    }


    private void addStyleSheet() throws IOException {
        write(Files.lines(Paths.get("src", "test", "resources", "style.css"))
                .collect(Collectors.joining("\n")));
    }





    private String formatPlayers(List<String> listPlayerAt, String separator) {
        if (listPlayerAt.isEmpty()) {
            return "&nbsp;";
        }

        return listPlayerAt.stream().collect(Collectors.joining(separator));
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
