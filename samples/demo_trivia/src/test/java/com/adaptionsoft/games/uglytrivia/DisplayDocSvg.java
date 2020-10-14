package com.adaptionsoft.games.uglytrivia;

import com.adaptionsoft.games.svg.*;
import org.sfvl.doctesting.DocAsTestBase;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class DisplayDocSvg extends DisplayDoc {
    private DocAsTestBase docAsTest;
    private static int animationCounter = 0; // Need to be static to ensure unicity. It may needed to add class name.
    private static String boardCounter = "0"; // Need to be static to ensure unicity. It may needed to add class name.
    private static List<String> playerColors = Arrays.asList("blue", "yellow", "red", "green");

    public DisplayDocSvg(DocAsTestBase docAsTest) {
        this.docAsTest = docAsTest;
    }

    @Override
    public void displaySpecificMulti(GameSvgTest.FakeGame aGame) throws IOException {

        displayBoard(aGame, displayMethodsByGroup.stream()
                .map(methods -> new Runnable() {
                    @Override
                    public void run() {
                        displayGroup(aGame, methods);
                    }
                })
                .collect(Collectors.toList()).toArray(new Runnable[0]));
    }

    private void displayBoard(GameSvgTest.FakeGame aGame, Runnable... displayMethods) throws IOException {

        final int SVG_WIDTH = 800;
        final int SVG_HEIGHT = 200;

        final int firstAnimateCounter = animationCounter;
        // TODO We should give name of test as id to not be impacted by the order or when not all tests are launch.
        boardCounter = Integer.toString(Integer.valueOf(boardCounter).intValue() + 1);

        write("++++\n\n");
        write("<svg version=\"1.1\" " +
                "xmlns=\"http://www.w3.org/2000/svg\" " +
                "xmlns:xlink=\"http://www.w3.org/1999/xlink\" " +
                "width=\"" + SVG_WIDTH + "\" " +
                "height=\"" + SVG_HEIGHT + "\" " +
                ">\n");

        write(new SvgRect()
                .setRect(0, 0, SVG_WIDTH, 200)
                .setFill("white")
                .setStroke("black")
                .setStrokeWidth("1").toSvg());

        for (int caseNumber = 0; caseNumber < GameSvgTest.BOARD_SIZE; caseNumber++) {
            final Position position = new Position(caseNumber);
            write(displayCase(position.getX(), position.getY(), aGame.category(caseNumber).toLowerCase()));
        }

        for (Object player : aGame.players) {

            svgWritePlayer(aGame, player.toString());
        }

        displayTextSvg("Game start !", Arrays.asList(), "startGame");
        IntStream.rangeClosed(1, 6).forEach(dice -> displayTextSvg(dice, "dice" + dice));

        for (Runnable displayMethod : displayMethods) {
            displayMethod.run();
        }

        write(new SvgText(boardCounter)
                .setId("b" + boardCounter + "_text1")
                .setX("50%").setY("50%")
                .setDominantBaseline("middle").setTextAnchor("middle")
                .setFontFamily("Verdana").setFontSize("25")
                .setOpacity("1")
                .addAll(Arrays.asList(
                        new SvgSet(boardCounter)
                                .setBeginRelativeTo(firstAnimateCounter, "begin")
                                .setAttributeName("opacity").setTo(0),
                        new SvgSet(boardCounter)
                                .setBeginRelativeTo(animationCounter, "end + 1s")
                                .setAttributeName("opacity").setTo(1)
                ))
                .setContent("Click to start")
                .toSvg());

        final SvgRect svgRect = new SvgRect()
                .setRect(0, 0, SVG_WIDTH, SVG_HEIGHT)
                .setOpacity("0.1")
                .addAll(Arrays.asList(
                        new SvgAnimate(boardCounter)
                                .setAnimIdFromIndex(firstAnimateCounter)
                                .setBegin("click")
                                .setAttributeName("x").setFrom("0").setTo("0")
                                .setDuration("0.01s"),
                        new SvgSet(boardCounter)
                                .setBeginRelativeTo(firstAnimateCounter, "begin")
                                .setAttributeName("width").setTo("50"),
                        new SvgSet(boardCounter)
                                .setBeginRelativeTo(firstAnimateCounter, "begin")
                                .setAttributeName("height").setTo("50"),
                        new SvgAnimate(boardCounter)
                                .setId("b" + boardCounter + "_animEnd")
                                .setBeginRelativeTo(animationCounter, "end + 1s")
                                .setAttributeName("x").setFrom("0").setTo("0")
                                .setDuration("0.01s"),
                        new SvgSet(boardCounter)
                                .setBeginRelativeTo(animationCounter, "end + 1s")
                                .setAttributeName("width").setTo(SVG_WIDTH),
                        new SvgSet(boardCounter)
                                .setBeginRelativeTo(animationCounter, "end + 1s")
                                .setAttributeName("height").setTo(SVG_HEIGHT)));

        write(svgRect.toSvg());
        displayStyleSvg();
        write("</svg>\n\n");
        write("++++\n\n");

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
        return new SvgRect()
                .setRect(x, y, 50, 50)
                .setFill(categoryMap.get(category).toString())
                .setStroke("black").setStrokeWidth("1")
                .toSvg();
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


    private void svgWritePlayer(GameSvgTest.FakeGame aGame, String playerName) {

        final int playerIndex = indexFromPlayerName(aGame, playerName).orElse(0);
        final int delta = - 5 + playerIndex * 4;

        Function<Integer, String> point = p -> new SvgText(boardCounter)
                .setId(String.format("b%s_player%s_%d", boardCounter, playerName, p))
                .setX(Integer.toString(25 + delta)).setY(Integer.toString(25 + delta))
                .setDominantBaseline("middle").setTextAnchor("middle")
                .setFontFamily("Verdana").setFontSize("25")
                .setOpacity(aGame.purses[aGame.currentPlayer] == p ? "1" : "0")
                .setContent(Integer.toString(p))
                .toSvg();

        final Position position = new Position(aGame.places[aGame.currentPlayer]);

        String color = playerColors.get(playerIndex);


        write("<svg id=\"b" + boardCounter + "_player"+playerName+"\" x=\"" + position.getX() + "\" y=\"" + position.getY() + "\"  >");
        write("<g>\n");
        write("<circle opacity=\"1\" cx=\"" + ((GameSvgTest.SQUARE_SIZE / 2) + delta) + "\" cy=\"" + ((GameSvgTest.SQUARE_SIZE / 2) + delta)  + "\" r=\"15\" fill=\""+color+"\" stroke=\"black\" stroke-width=\"1\">\n");
        write("</circle>\n");
        IntStream.rangeClosed(0, 6).forEach(p -> write(point.apply(p)));

        write(new SvgRect()
                .setId(String.format("b%s_player"+playerName+"_jail", boardCounter))
                .setRect(8 + delta, 8 + delta, 34, 34)
                .setFill("none")
                .setStroke(color).setStrokeWidth("4").setStrokeDasharray("8,3")
                .setOpacity(aGame.inPenaltyBox[aGame.currentPlayer] ? "1" : "0")
                .toSvg());

        write("</g>");
        write(new SvgSet(boardCounter)
                .setBegin("b" + boardCounter + "_animEnd.end")
                .setAttributeName("x").setTo(position.getX())
                .toSvg());
        write(new SvgSet(boardCounter)
                .setBegin("b" + boardCounter + "_animEnd.end")
                .setAttributeName("y").setTo(position.getY())
                .toSvg());

        write("</svg>\n");


        // Restore value at the end
        displayPoints(aGame, "b" + boardCounter + "_animEnd.end");
        displayPoints(aGame, "b" + boardCounter + "_anim" + animationCounter + ".end");
        displayPenalityBox(aGame, "b" + boardCounter + "_animEnd.end");
    }

    private Optional<Integer> indexFromPlayerName(GameSvgTest.FakeGame aGame, String playerName) {
        for (int playerIndex = 0; playerIndex < aGame.players.size(); playerIndex++) {
            if (playerName.equals((String) aGame.players.get(playerIndex))) {
                return Optional.of(playerIndex);
            }
        }
        return Optional.empty();
    }


    private void write(String... text) {
        docAsTest.write(text);
    }


    private void showAndHideTextSvg(String text) {

        int lastAnimationIndex = animationCounter;

        final SvgAnimateElement showText = new SvgAnimate(boardCounter)
                .setAnimIdFromIndex(++animationCounter)
                .setBeginRelativeTo(lastAnimationIndex, "end")
                .setAttributeName("opacity")
                .setFrom("0")
                .setTo("1")
                .setDuration("0.2s");

        final SvgAnimateElement hideText = new SvgAnimate(boardCounter)
                .setAnimIdFromIndex(++animationCounter)
                .setBegin(showText.getId().get() + ".end + 1s")
                .setAttributeName("opacity")
                .setFrom("1")
                .setTo("0")
                .setDuration("0.2s");

        displayTextSvg(text, Arrays.asList(showText, hideText));

    }

    private void displayTextSvg(int text, String id) {
        displayTextSvg(Integer.toString(text), Arrays.asList(), id);
    }

    private void displayTextSvg(String text, List<SvgElement> elements) {
        write(buildSvgText(text, elements).toSvg());
    }
    private void displayTextSvg(String text, List<SvgElement> elements, String id) {
        write(buildSvgText(text, elements).setId("b" + boardCounter + "_" + id).toSvg());
    }

    private SvgText buildSvgText(String text, List<SvgElement> elements) {
        return new SvgText(boardCounter)
                .setX("50%").setY("50%")
                .setDominantBaseline("middle").setTextAnchor("middle")
                .setFontFamily("Verdana").setFontSize("25")
                .setOpacity("0")
                .setContent(text)
                .addAll(elements);
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
        for (int i = 0; i < aGame.players.size(); i++) {
            final int playerToDisplay = i;
            write(String.format("<set xlink:href=\"#b%s_player%s_jail\" begin=\"" + idToBegin + "\" attributeName=\"opacity\" to=\"%d\" repeatCount=\"1\" fill=\"freeze\"/>\n",
                    boardCounter,
                    (String) aGame.players.get(playerToDisplay),
                    aGame.inPenaltyBox[playerToDisplay] ? 1 : 0));
        }
    }

    private void displayPoints(final GameSvgTest.FakeGame aGame, String idToBegin) {
        for (int i = 0; i < aGame.players.size(); i++) {
            final int playerToDisplay = i;
            IntStream.rangeClosed(0, 6).forEach(p -> {
                write(String.format("<set xlink:href=\"#b%s_player%s_%d\" begin=\"" + idToBegin + "\" attributeName=\"opacity\" to=\"%d\" repeatCount=\"1\" fill=\"freeze\"/>\n",
                        boardCounter,
                        (String) aGame.players.get(playerToDisplay),
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
            movePlayerSvg("player"+(String)aGame.players.get(playerHighLighted), new Position(i % GameSvgTest.BOARD_SIZE));
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

        write(new SvgAnimate(boardCounter)
                .setAnimIdFromIndex(++animationCounter)
                .setXLinkHref("b" + boardCounter + "_" + player)
                .setBeginRelativeTo(lastAnimation, "end")
                .setAttributeName("x")
                .setTo(Integer.toString(x))
                .setDuration(GameSvgTest.TEMPO)
                .toSvg());

        write(new SvgAnimate(boardCounter)
                .setXLinkHref("b" + boardCounter + "_" + player)
                .setBeginRelativeTo(lastAnimation, "end")
                .setAttributeName("y")
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