package com.adaptionsoft.games.uglytrivia;

import org.sfvl.doctesting.DocAsTestBase;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class DisplayDocSvg extends DisplayDoc {
    private DocAsTestBase docAsTest;
    private static int animationCounter = 0; // Need to be static to ensure unicity. It may needed to add class name.
    private static String boardCounter = "0"; // Need to be static to ensure unicity. It may needed to add class name.

    public DisplayDocSvg(DocAsTestBase docAsTest) {
        this.docAsTest = docAsTest;
    }

    @Override
    public void displaySpecificMulti(GameSvgTest.FakeGame aGame) throws IOException {

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

    private void displayBoard(GameSvgTest.FakeGame aGame, Runnable... displayMethods) throws IOException {

        final int SVG_WIDTH = 800;
        final int SVG_HEIGHT = 200;

        final int firstAnimateCounter = animationCounter;
        boardCounter = Integer.toString(Integer.valueOf(boardCounter).intValue()+1);

        write("++++\n\n");
        write("<svg version=\"1.1\" " +
                "xmlns=\"http://www.w3.org/2000/svg\" " +
                "xmlns:xlink=\"http://www.w3.org/1999/xlink\" " +
                "width=\"" + SVG_WIDTH + "\" " +
                "height=\"" + SVG_HEIGHT + "\" " +
                ">\n");


        write("<rect x=\"0\" y=\"0\" width=\"" + SVG_WIDTH + "\" height=\"200\" fill=\"white\" stroke=\"black\" stroke-width=\"1\" />\n");
        write("");

        for (int caseNumber = 0; caseNumber < GameSvgTest.BOARD_SIZE; caseNumber++) {
            final Position position = new Position(caseNumber);
            write(displayCase(position.getX(), position.getY(), aGame.category(caseNumber).toLowerCase()));
        }

        svgWritePlayer(aGame);

        displayTextSvg("Game start !", "startGame");
        IntStream.rangeClosed(1, 6).forEach(dice -> displayTextSvg(dice, "dice" + dice));

        for (Runnable displayMethod : displayMethods) {
            displayMethod.run();
        }

        write("<text id=\"b" + boardCounter + "_text1\" x=\"50%\" y=\"50%\" dominant-baseline=\"middle\" text-anchor=\"middle\" font-family=\"Verdana\" font-size=\"25\" opacity=\"1\">" +
                "<set begin=\"b" + boardCounter + "_anim" + firstAnimateCounter + ".begin\" attributeName=\"opacity\" to=\"0\" repeatCount=\"1\" fill=\"freeze\"/>" +
                "<set begin=\"b" + boardCounter + "_anim" + animationCounter + ".end + 1s\" attributeName=\"opacity\" to=\"1\" repeatCount=\"1\" fill=\"freeze\"/>" +
                "Click to start" +
                "</text>\n");


        write("<rect x=\"0\" y=\"0\" width=\"" + SVG_WIDTH + "\" height=\"" + SVG_HEIGHT + "\" opacity=\"0.1\">\n");
        write("  " + new SvgAnimation(boardCounter)
                .setAnimId(firstAnimateCounter)
                .setBegin("click")
                .setAttribute("x").setFrom("0").setTo("0")
                .setDuration("0.01s").toSvg());
        write("  <set begin=\"b" + boardCounter + "_anim" + firstAnimateCounter + ".begin\" attributeName=\"width\" to=\"50\" repeatCount=\"1\" fill=\"freeze\"/>\n");
        write("  <set begin=\"b" + boardCounter + "_anim" + firstAnimateCounter + ".begin\" attributeName=\"height\" to=\"50\" repeatCount=\"1\" fill=\"freeze\"/>\n");

        write("  " + new SvgAnimation(boardCounter)
                .setId("b" + boardCounter + "_animEnd")
                .setBeginRelativeTo(animationCounter, "end + 1s")
                .setAttribute("x").setFrom("0").setTo("0")
                .setDuration("0.01s").toSvg());
        write("  <set begin=\"b" + boardCounter + "_anim" + animationCounter + ".end + 1s\" attributeName=\"width\" to=\"" + SVG_WIDTH + "\" repeatCount=\"1\" fill=\"freeze\"/>\n");
        write("  <set begin=\"b" + boardCounter + "_anim" + animationCounter + ".end + 1s\" attributeName=\"height\" to=\"" + SVG_HEIGHT + "\" repeatCount=\"1\" fill=\"freeze\"/>\n");

        write("</rect>\n");

        displayStyleSvg();
        write("</svg>\n\n");
        write("++++\n\n");

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
        write("<style>\n");
        write("text {\n");
        write("font-size: 30px;\n");
        write("font-weight: bold;\n");
        write("fill: black;\n");
//            fileWriterWrite("stroke: white;\n");
//            fileWriterWrite("stroke-width: 1px;\n");
        write("</style>\n");
    }


    private void svgWritePlayer(GameSvgTest.FakeGame aGame) {
        Function<Integer, String> point = p -> String.format("<text id=\"b%s_playerA_%d\" x=\"25\" y=\"25\" dominant-baseline=\"middle\" text-anchor=\"middle\" font-family=\"Verdana\" font-size=\"25\" opacity=\"%d\">%d</text>\n",
                boardCounter,
                p,
                aGame.purses[aGame.currentPlayer] == p ? 1 : 0,
                p);

        final Position position = new Position(aGame.places[aGame.currentPlayer]);
        write("<svg id=\"b" + boardCounter + "_playerA\" x=\"" + position.getX() + "\" y=\"" + position.getY() + "\"  ><g>\n");
        write("<circle opacity=\"1\" cx=\"" + (GameSvgTest.SQUARE_SIZE / 2) + "\" cy=\"" + (GameSvgTest.SQUARE_SIZE / 2) + "\" r=\"15\" fill=\"grey\" stroke=\"black\" stroke-width=\"1\">\n");
        write("</circle>\n");
        IntStream.rangeClosed(0, 6).forEach(p -> write(point.apply(p)));

        write(String.format("<rect id=\"b%s_playerA_jail\" x=\"8\" y=\"8\" width=\"34\" height=\"34\" fill=none stroke=\"black\" stroke-width=\"5\" opacity=\"%d\"/>\n",
                boardCounter, aGame.inPenaltyBox[aGame.currentPlayer] ? 1 : 0));

        write("</g>");
        write("<set begin=\"b" + boardCounter + "_animEnd.end\" attributeName=\"x\" to=\"" + position.getX() + "\" repeatCount=\"1\" fill=\"freeze\"/>\n");
        write("<set begin=\"b" + boardCounter + "_animEnd.end\" attributeName=\"y\" to=\"" + position.getY() + "\" repeatCount=\"1\" fill=\"freeze\"/>\n");
        write("</svg>\n");

//        fileWriterWrite("<set xlink:href=\"#b" + boardCounter + "_playerA_0\"  attributeName=\"opacity\" to=\"1\" repeatCount=\"1\" fill=\"freeze\"/>\n");

        // Restore value at the end
        displayPoints(aGame, "b" + boardCounter + "_animEnd.end");
        displayPoints(aGame, "b" + boardCounter + "_anim" + animationCounter + ".end");
        displayPenalityBox(aGame, "b" + boardCounter + "_animEnd.end");
    }


    private void write(String... text) {
        docAsTest.write(text);
    }


    private void showAndHideTextSvg(String text) {
        write("<text x=\"50%\" y=\"50%\" dominant-baseline=\"middle\" text-anchor=\"middle\" font-family=\"Verdana\" font-size=\"25\" opacity=\"0\">" +
                text);

        showAndHideTextSvg();

        write("</text>\n");

    }

    private void showAndHideTextSvg() {

        int lastAnimationIndex = animationCounter;

        final SvgAnimation showText = new SvgAnimation(boardCounter)
                .setAnimId(++animationCounter)
                .setBeginRelativeTo(lastAnimationIndex, "end")
                .setAttribute("opacity")
                .setFrom("0")
                .setTo("1")
                .setDuration("0.2s");

        final SvgAnimation hideText = new SvgAnimation(boardCounter)
                .setAnimId(++animationCounter)
                .setBegin(showText.getId().get() + ".end + 1s")
                .setAttribute("opacity")
                .setFrom("1")
                .setTo("0")
                .setDuration("0.2s");

        write(showText.toSvg());
        write(hideText.toSvg());

    }


    private void displayTextSvg(int text, String id) {
        displayTextSvg(Integer.toString(text), id);
    }

    private void displayTextSvg(String text, String id) {
        write("<text id=\"b" + boardCounter + "_" +
                id +
                "\" x=\"50%\" y=\"50%\" dominant-baseline=\"middle\" text-anchor=\"middle\" font-family=\"Verdana\" font-size=\"25\" opacity=\"0\">" +
                text +
                "</text>\n");
    }

    private void displayQuestionAskedSvg(GameSvgTest.FakeGame aGame, String currentPlayer, Supplier<Boolean> wrongAnswer) {
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

    private void displayPenalityBox(GameSvgTest.FakeGame aGame, String idToBegin) {
        write(String.format("<set xlink:href=\"#b%s_playerA_jail\" begin=\"" + idToBegin + "\" attributeName=\"opacity\" to=\"%d\" repeatCount=\"1\" fill=\"freeze\"/>\n",
                boardCounter,
                aGame.inPenaltyBox[aGame.currentPlayer] ? 1 : 0));
    }

    private void displayPoints(GameSvgTest.FakeGame aGame, String idToBegin) {
        for (int i = 0; i < aGame.players.size(); i++) {
            final int playerToDisplay = i;
            IntStream.rangeClosed(0, 6).forEach(p -> {
                write(String.format("<set xlink:href=\"#b%s_playerA_%d\" begin=\"" + idToBegin + "\" attributeName=\"opacity\" to=\"%d\" repeatCount=\"1\" fill=\"freeze\"/>\n",
                        boardCounter,
                        p,
                        aGame.purses[playerToDisplay] == p ? 1 : 0));
            });

        }
    }

    @Override
    void displayGroup(GameSvgTest.FakeGame aGame, List<Runnable> methods) {
        methods.stream().forEach(m -> m.run());
    }

    @Override
    public void text(String text) {
        showAndHideTextSvg(text);
    }

    @Override
    public void move(final Game aGame, final String player, int from, int playerHighLighted) {
        write("\n\n");
        // showPoints(playerHighLighted);

        int to = aGame.places[playerHighLighted];
        if (to < from) {
            to += GameSvgTest.BOARD_SIZE;
        }
        for (int i = from + 1; i <= to; i++) {
            movePlayerSvg("playerA", new Position(i % GameSvgTest.BOARD_SIZE));
        }
    }

    private void movePlayerSvg(final String player, Position position) {

        int x = position.getX();
        int y = position.getY();

        int lastAnimation;
        int currentAnimation;

        lastAnimation = animationCounter;
//        animationCounter++;
//        currentAnimation = animationCounter;

        write(new SvgAnimation(boardCounter)
                .setAnimId(++animationCounter)
                .setXlink("b" + boardCounter + "_" + player)
                .setBeginRelativeTo(lastAnimation, "end")
                .setAttribute("x")
                .setTo(Integer.toString(x))
                .setDuration(GameSvgTest.TEMPO)
                .toSvg());

        write(new SvgAnimation(boardCounter)
                .setXlink("b" + boardCounter + "_" + player)
                .setBeginRelativeTo(lastAnimation, "end")
                .setAttribute("y")
                .setTo(Integer.toString(y))
                .setDuration(GameSvgTest.TEMPO)
                .toSvg());


    }

    public void showPoints(int player) {
        write("<set begin=\"b" + boardCounter + "_anim" + animationCounter + ".begin\" attributeName=\"opacity\" to=\"1\" repeatCount=\"1\" fill=\"freeze\"/>\n");

    }

    @Override
    public void roll(GameSvgTest.FakeGame aGame, int roll) {

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
    public void displayQuestionAskedMulti(GameSvgTest.FakeGame aGame, String currentPlayer, Supplier<Boolean> wrongAnswer) {
        displayQuestionAskedSvg(aGame, currentPlayer, wrongAnswer);
    }

}

class Position {

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
        return x * GameSvgTest.SQUARE_SIZE;
    }

    public int getY() {
        return y * GameSvgTest.SQUARE_SIZE;
    }

}