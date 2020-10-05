package com.adaptionsoft.games.uglytrivia;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.sfvl.doctesting.ApprovalsBase;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.IntStream;


@DisplayName("Explanations and examples")
public class GameTest extends ApprovalsBase {

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

    /**
     * In a game turn, the player rolls a dice and advances the number of spaces indicated.
     * @throws Exception
     */
    @Test
    @DisplayName("Movements of a player")
    public void player_advances() throws Exception {

        final FakeGame aGame = startGame("Chet");

        write("== Normal move\n\n");
        displayPosition(aGame, "start", aGame.currentPlayer);

        displayRollDice(aGame, 4);
        write("\n\n");

        displayPosition(aGame, "end", aGame.currentPlayer);

        write("== Move beyond the end\n\n");

        write("\n\nIf he reaches the end of the board, he continues by starting from the beginning\n\n");
        aGame.roll(6);
        displayPosition(aGame, "start", aGame.currentPlayer);
        displayRollDice(aGame, 5);
        displayPosition(aGame, "end", aGame.currentPlayer);

        addStyleSheet();
    }

    /**
     * After moving, the player must answer a question corresponding to the category of the square where he is located.
     * If he answers correctly, he scores a point.
     * @throws Exception
     */
    @Test
    public void player_scores() throws Exception {

        final FakeGame aGame = startGame("Chet");

        final int playerHighlighted = aGame.currentPlayer;
        String currentPlayer = aGame.players.get(playerHighlighted).toString();

        displayPosition(aGame, "start", playerHighlighted);
        displayOneTurn(aGame, currentPlayer, () -> 3, () -> false);
        addStyleSheet();
    }

    /**
     * The first player to score 6 points wins the game.
     * @throws Exception
     */
    @Test
    public void win_the_game() throws Exception {

        final FakeGame aGame = startGame("Chet");

        aGame.wasCorrectlyAnswered();
        aGame.wasCorrectlyAnswered();
        aGame.wasCorrectlyAnswered();
        aGame.wasCorrectlyAnswered();
        aGame.wasCorrectlyAnswered();

        final int playerHighlighted = aGame.currentPlayer;
        String currentPlayer = aGame.players.get(playerHighlighted).toString();

        displayPosition(aGame, "start", playerHighlighted);
        final boolean notAWinner = displayOneTurn(aGame, currentPlayer, () -> 3, () -> false);
        if (!notAWinner) {
            write(currentPlayer + " wins the game !!! +\n");
        }

        addStyleSheet();
    }

    /**
     * When a player gives a wrong answer, he goes to jail.
     * He stays in jail until he rolls an odd number.
     * @throws Exception
     */
    @Test
    public void jail() throws Exception {
        {
            final FakeGame aGame = startGame("Chet");

            write("== Got to jail\n\n");
            final int playerHighlighted = aGame.currentPlayer;
            String currentPlayer = aGame.players.get(playerHighlighted).toString();
            displayPosition(aGame, "start", playerHighlighted);

            final Supplier<Integer> rollSupplier = () -> 3;
            final Supplier<Boolean> wrongAnswer = () -> true;

            displayOneTurn(aGame, currentPlayer, rollSupplier, wrongAnswer);

            write("== Need an odd number to move\n\n");
            displayPosition(aGame, "start", playerHighlighted);

            displayOneTurn(aGame, currentPlayer, () -> 2, () -> true);

            displayOneTurn(aGame, currentPlayer, () -> 3, () -> true);
        }
        {
            final FakeGame aGame = startGame("Chet");

            final int playerHighlighted = aGame.currentPlayer;
            String currentPlayer = aGame.players.get(playerHighlighted).toString();

            write("== Get out of jail\n\n");
            write("When player correctly answer to a question, he goes out of jail.\n\n");

            displayPosition(aGame, "Start", playerHighlighted);
            displayOneTurn(aGame, currentPlayer, () -> 3, () -> true);
            displayOneTurn(aGame, currentPlayer, () -> 5, () -> false);
        }
        addStyleSheet();
    }

    /**
     * When rolled is even, player stay in penality box.
     * @throws Exception
     */
    @Test
    public void player_stay_in_penality_box() throws Exception {

        final FakeGame aGame = startGame("Chet");

        aGame.roll(3);
        aGame.wrongAnswer();

        String currentPlayer = aGame.players.get(aGame.currentPlayer).toString();

        displayPosition(aGame, "start", aGame.currentPlayer);

        final Supplier<Integer> rollSupplier = () -> 2;
        final Supplier<Boolean> wrongAnswer = () -> false;

        boolean notAWinner = displayOneTurn(aGame, currentPlayer, rollSupplier, wrongAnswer);

        addStyleSheet();
    }

    /**
     * This is a full game from the beginning to the end when a player wins the game.
     * @throws Exception
     */
    @Test
    @DisplayName("A full game")
    public void play_until_someone_wins() throws Exception {
        write("&nbsp; +\n");
        final FakeGame aGame = startGame("Chet", "Pat", "Sue");
        displayPlayers(aGame);

        Random rand = new Random(12345);

        boolean notAWinner = true;
        int tour = 1;
        String currentPlayer;
        do {

            currentPlayer = aGame.players.get(aGame.currentPlayer).toString();
            write("*Turn " + tour++ + "*\n\n");
            displayPosition(aGame, "start", aGame.currentPlayer);

            final Supplier<Integer> rollSupplier = () -> rand.nextInt(5) + 1;
            final Supplier<Boolean> wrongAnswer = () -> rand.nextInt(9) == 7;

            notAWinner = displayOneTurn(aGame, currentPlayer, rollSupplier, wrongAnswer);
            write("\n\n___\n\n");
            if (!notAWinner) {
                write(currentPlayer + " wins the game !!! +\n");
            }
        } while (notAWinner);


        addStyleSheet();
    }

    private void addStyleSheet() throws IOException {
        write(Files.lines(Paths.get("src", "test", "resources", "style.css"))
                .collect(Collectors.joining("\n")));
    }


    private boolean displayOneTurn(FakeGame aGame, String currentPlayer, Supplier<Integer> rollSupplier, Supplier<Boolean> wrongAnswer) throws IOException {
        boolean notAWinner = true;
        final int roll = rollSupplier.get();

        final int currentPlayerNumber = aGame.currentPlayer;

        aGame.ask = false;
        displayRollDice(aGame, roll);

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

        displayPosition(aGame, "end", currentPlayerNumber);
        //displayPlayers(aGame);
        return notAWinner;
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


    private void displayCategories(Game aGame) {
        final String line = IntStream.range(0, 12)
                .mapToObj(i -> aGame.category(i))
                .distinct()
                .map(category -> String.format("* [%s]#%s#", category.toLowerCase(), category))
                .collect(Collectors.joining("\n"));

        write("Categories :\n\n"+line+"\n\n");
    }

    private void displayPosition(Game aGame, String startOrEnd, int playerHighLighted) throws IOException {
//        write("[%autowidth]\n|====\n");
        final int columns = 12;
//        write("Board :\n[cols=\"12*a\"]\n|====\n");
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
        write("<tr>\n"+IntStream.range(0, columns)
                .mapToObj(position -> String.format("<td class=\"%s %s\">%s</td>", aGame.category(position).toLowerCase(), "boardHeader", position))
                .collect(Collectors.joining("\n"))
                + "\n</tr>\n"
        );

        write("<tr>\n"+IntStream.range(0, columns)
                .mapToObj(position -> String.format("<td class=\"%s\">%s</td>",
                        aGame.category(position).toLowerCase(),
                        getListPlayerAt(aGame, position, playerHighLighted).stream().collect(Collectors.joining(""))))
                .collect(Collectors.joining("\n"))
                + "\n</tr>\n"
        );
        write("</table>\n\n++++\n\n");
    }
    private void displayPlayers(Game aGame) throws IOException {
        write("Players : \n\n");

        write(aGame.players.stream()
                .collect(Collectors.joining(", "))
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
        for (int player = 0; player < aGame.howManyPlayers() ; player++) {
            if (aGame.places[player]== position) {
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
