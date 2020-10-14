package com.adaptionsoft.games.uglytrivia;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.sfvl.doctesting.ApprovalsBase;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.function.Supplier;
import java.util.stream.Collectors;


@DisplayName("Explanations and examples")
public class GameSvgTest extends ApprovalsBase {

    public static final int SQUARE_SIZE = 50;
    public static final int BOARD_SIZE = 12;
    public static final String TEMPO = "0.5s";


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
     *
     * @throws Exception
     */
    @Test
    @DisplayName("Movements of a player")
    public void player_advances() throws Exception {

        {
            write("== Normal move\n\n");
            for (DisplayDoc displayDoc : Arrays.asList(new DisplayDocSvg(this), new DisplayDocHtml(this))) {

                final FakeGame aGame = startGame("Chet");
                final int currentPlayerNumber = aGame.currentPlayer;
                final int roll = 4;
                displayDoc.addAll(Arrays.asList(
                        () -> {
                            displayDoc.text("Start of the turn");
                            displayDoc.move(aGame, "player" + (String)aGame.players.get(currentPlayerNumber), aGame.places[currentPlayerNumber], currentPlayerNumber);
                        },
                        () -> displayDoc.rollAndMove(aGame, currentPlayerNumber, roll)));
                displayDoc.display(aGame);
            }
        }
        {
            write("== Move beyond the end\n\n");

            write("\n\nIf he reaches the end of the board, he continues by starting from the beginning\n\n");


//            for (DisplayDoc displayDoc : Arrays.asList(new DisplayDocSvg(this), new DisplayDocHtml(this))) {
//
//                final FakeGame aGame = startGame("Chet");
//                final int currentPlayerNumber = aGame.currentPlayer;
//                final int roll = 3;
//                aGame.roll(4);
//                aGame.roll(6);
//                displayDoc.addAll(Arrays.asList(
//                        () -> {
//                            displayDoc.text("Start of the turn");
//                            displayDoc.move(aGame, "player" + (String)aGame.players.get(currentPlayerNumber), aGame.places[currentPlayerNumber], currentPlayerNumber);
//                        },
//                        () -> displayDoc.rollAndMove(aGame, currentPlayerNumber, roll)));
//                displayDoc.display(aGame);
//            }
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

        for (DisplayDoc displayDoc : Arrays.asList(new DisplayDocSvg(this), new DisplayDocHtml(this))) {

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
        for (DisplayDoc displayDoc : Arrays.asList(new DisplayDocSvg(this), new DisplayDocHtml(this))) {

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
            for (DisplayDoc displayDoc : Arrays.asList(new DisplayDocSvg(this), new DisplayDocHtml(this))) {

                final FakeGame aGame = startGame("Chet");

                displayDoc.addAll(displayDoc.displayOneTurnMulti(aGame, 3, () -> true));

                displayDoc.display(aGame);
            }
        }
        {
            write("== Need an odd number to move\n\n");

            for (DisplayDoc displayDoc : Arrays.asList(new DisplayDocSvg(this), new DisplayDocHtml(this))) {

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
            for (DisplayDoc displayDoc : Arrays.asList(new DisplayDocSvg(this), new DisplayDocHtml(this))) {

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

            for (DisplayDoc displayDoc : Arrays.asList(new DisplayDocSvg(this), new DisplayDocHtml(this))) {

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
//            for (DisplayDoc displayDoc : Arrays.asList(new DisplayDocSvg(this)/*, new DisplayDocHtml(this)*/)) {
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

            for (DisplayDoc displayDoc : Arrays.asList(new DisplayDocSvg(this), new DisplayDocHtml(this) {
                public void displaySpecificMulti(FakeGame aGame) throws IOException {
                    displayMethodsByGroup.get(0).get(0).run();
                }
            })) {

                final FakeGame aGame = startGame("Chet", "Pat", "Sue");
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






}
