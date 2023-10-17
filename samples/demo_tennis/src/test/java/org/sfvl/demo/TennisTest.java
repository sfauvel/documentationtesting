package org.sfvl.demo;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.sfvl.doctesting.junitextension.ApprovalsExtension;
import org.sfvl.doctesting.junitextension.SimpleApprovalsExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * We will display a tennis score.
 */
@DisplayName(value = "Scores examples")
public class TennisTest {

    @RegisterExtension
    static ApprovalsExtension doc = new SimpleApprovalsExtension();

    static class TennisRecorder extends Tennis {
        List<String> points = new ArrayList<>();
        List<Score> scores = new ArrayList<>();

        @Override
        public void playerAWinPoint() {
            points.add("A");
            super.playerAWinPoint();
            scores.add(super.getScore());
        }

        @Override
        public void playerBWinPoint() {
            points.add("B");
            super.playerBWinPoint();
            scores.add(super.getScore());
        }
    }

    TennisRecorder tennis = new TennisRecorder();

    /**
     * At the beginning, score is 0 / 0.
     */
    @Test
    public void start_of_the_game() {

        displayScore(tennis);
    }

    /**
     * When player A win one point, score is 15 / 0.
     */
    @Test
    public void player_A_win_one_point() {
        tennis.playerAWinPoint();

        displayScore(tennis);
    }

    /**
     * When player B win one point, score is 0 / 15.
     */
    @Test
    public void player_B_win_one_point() {

        tennis.playerBWinPoint();

        displayScore(tennis);
    }

    /**
     * When player A win two points, score is 30 / 0.
     */
    @Test
    public void player_A_win_two_points() {

        tennis.playerAWinPoint();
        tennis.playerAWinPoint();

        displayScore(tennis);
    }

    /**
     * When player B win two points, score is 0 / 30.
     */
    @Test
    public void player_B_win_two_points() {

        tennis.playerBWinPoint();
        tennis.playerBWinPoint();

        displayScore(tennis);
    }

    /**
     * When player A win 2 points and player B win 3 points, score is 30 / 40.
     */
    @Test
    public void both_players_win_some_points() {

        tennis.playerBWinPoint();
        tennis.playerAWinPoint();
        tennis.playerAWinPoint();
        tennis.playerBWinPoint();
        tennis.playerBWinPoint();

        displayScore(tennis);
    }

    @DisplayName("Score evolution in a game")
    @Test
    public void score_evolution() {

        tennis.playerBWinPoint();
        tennis.playerAWinPoint();
        tennis.playerAWinPoint();
        tennis.playerBWinPoint();
        tennis.playerBWinPoint();
        tennis.playerAWinPoint();
        tennis.playerAWinPoint();
        tennis.playerBWinPoint();
        tennis.playerBWinPoint();
        tennis.playerBWinPoint();

        displayScoreEvolution(tennis);
    }

    private void displayScoreEvolution(TennisRecorder tennis) {
        doc.write("[%autowidth, cols=" + (tennis.points.size() + 1) + "*, stripes=none]",
                "|===",
                "| Player A" + pointsToTable("A", tennis.points),
                "| Player B" + pointsToTable("B", tennis.points),
                "| Score" + scoresToTables(tennis.scores),
                "|===",
                "");
    }



    private void displayScore(TennisRecorder tennis) {
        String textScore = tennis.getScore().toString();
        doc.write("[%autowidth, cols=" + (tennis.points.size() + 2) + "*, stripes=none]",
                "|===",
                "| Player A" + pointsToTable("A", tennis.points) + "\n.2+^.^| *" + textScore + "*",
                "| Player B" + pointsToTable("B", tennis.points) + " |",
                "|===",
                "");

        writeStyle();
    }

    private void writeStyle() {
        doc.write("", "++++", "<style>", "table.tableblock.grid-all {", "    border-collapse: collapse;", "}", "table.tableblock.grid-all, table.tableblock.grid-all td, table.grid-all > * > tr > .tableblock:last-child {", "    border: 1px solid #dddddd;", "}", "</style>", "++++", "");
    }

    private String pointsToTable(String player, List<String> points) {
        return points.stream()
                .map(p -> " | " + (p.equals(player) ? "&#x2714;" : " "))
                .collect(Collectors.joining())
                .trim();
    }

    private static String scoresToTables(List<Score> scores) {
        return scores.stream().map(s -> " | *" + s.toString() + "*").collect(Collectors.joining());
    }

}

