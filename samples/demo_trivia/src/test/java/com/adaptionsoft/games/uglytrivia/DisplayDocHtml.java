package com.adaptionsoft.games.uglytrivia;

import org.sfvl.doctesting.junitinheritance.DocAsTestBase;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class DisplayDocHtml extends DisplayDoc {

    private DocAsTestBase docAsTest;

    public DisplayDocHtml(DocAsTestBase docAsTest) {
        this.docAsTest = docAsTest;
    }

    public void write(String... texts) {
        docAsTest.write(texts);
    }

    public void displayInLine(Runnable... displayMethods) {
        write(String.format("[.tableInline]\n[%%autowidth, cols=%d, frame=none, grid=none]\n|====\n", displayMethods.length));
        for (Runnable displayMethod : displayMethods) {
            write("\na|");
            displayMethod.run();
        }
        write("\n|====\n");
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

    public String getScore(int gold) {
        return String.format("&#x%d;", 2779 + gold);
    }

    public String dice(int value) {
        return String.format("[.dice]#&#x%d;#\n", 2679 + value);
    }

    @Override
    public void displaySpecificMulti(GameSvgTest.FakeGame aGame) throws IOException {
        displayMethodsByGroup.forEach(methods -> displayGroup(aGame, methods));
    }

    @Override
    public void displayGroup(GameSvgTest.FakeGame aGame, List<Runnable> methods) {
        displayInLine(methods.toArray(new Runnable[0]));
    }

    @Override
    public void text(String text) {
        write("[.tableHeader]#" + text + "#\n");
        write("\n\n");
    }

    @Override
    public void move(final Game aGame, final String player, int from, int playerHighLighted) {

        final int columns = GameSvgTest.BOARD_SIZE;

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
    public void roll(GameSvgTest.FakeGame aGame, int roll) {

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
    public void displayQuestionAskedMulti(GameSvgTest.FakeGame aGame, String currentPlayer, Supplier<Boolean> wrongAnswer) {
        displayQuestionAsked(aGame, currentPlayer, wrongAnswer);
    }

    private void displayQuestionAsked(GameSvgTest.FakeGame aGame, String currentPlayer, Supplier<Boolean> wrongAnswer) {
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
