package com.adaptionsoft.games.uglytrivia;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Supplier;

public abstract class DisplayDoc {

    public boolean notAWinner = true;

    List<List<Runnable>> displayMethodsByGroup = new ArrayList<>();

    abstract void text(String text);

    abstract void move(final Game aGame, final String player, int from, int playerHighLighted);

    abstract void roll(GameSvgTest.FakeGame aGame, int roll);

    abstract void displayQuestionAskedMulti(GameSvgTest.FakeGame aGame, String currentPlayer, Supplier<Boolean> wrongAnswer);

    abstract void displayGroup(GameSvgTest.FakeGame aGame, List<Runnable> methods);

    public void display(GameSvgTest.FakeGame aGame) throws IOException {
        displaySpecificMulti(aGame);
    }

    abstract void displaySpecificMulti(GameSvgTest.FakeGame aGame) throws IOException;

    public void add(Runnable displayMethod) {
        this.displayMethodsByGroup.add(Arrays.asList(displayMethod));
    }

    public void addAll(List<Runnable> displayMethods) {
        this.displayMethodsByGroup.add(displayMethods);
    }

    public void rollAndMove(GameSvgTest.FakeGame aGame, int currentPlayerNumber, int roll) {
        int from = aGame.places[currentPlayerNumber];
        roll(aGame, roll);
        move(aGame, "player" + currentPlayerNumber, from, currentPlayerNumber);
    }

    public void question(GameSvgTest.FakeGame aGame, int currentPlayerNumber, String currentPlayer, Supplier<Boolean> wrongAnswer) {
        int from = aGame.places[currentPlayerNumber];
        displayQuestionAskedMulti(aGame, currentPlayer, wrongAnswer);
        move(aGame, "end", from, currentPlayerNumber);
    }

    public List<Runnable> displayOneTurnMulti(GameSvgTest.FakeGame aGame, int roll, Supplier<Boolean> booleanSupplier) {
        int currentPlayerNumber = aGame.currentPlayer;
        String currentPlayer = (String) aGame.players.get(currentPlayerNumber);
        final List<Runnable> oneTurn = Arrays.asList(
                () -> {
                    text("Start of the turn");
                    move(aGame, "playerA", aGame.places[currentPlayerNumber], currentPlayerNumber);
                },
                () -> rollAndMove(aGame, currentPlayerNumber, roll),
                () -> question(aGame, currentPlayerNumber, currentPlayer, booleanSupplier));
        return oneTurn;
    }
}
