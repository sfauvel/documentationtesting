package org.sfvl.demo;

import org.junit.jupiter.api.Test;
import org.sfvl.doctesting.ApprovalsBase;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * We will display a tennis score.
 */
public class TennisTest extends ApprovalsBase {

    static class TennisRecorder extends Tennis {
        List<String> points = new ArrayList<>();
        @Override
        public void playerAWinPoint() {
            points.add("A");
            super.playerAWinPoint();
        }

        @Override
        public void playerBWinPoint() {
            points.add("B");
            super.playerBWinPoint();
        }
    }

    TennisRecorder tennis = new TennisRecorder();

    /**
     * At the begining, score is 0 / 0.
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


    private void displayScore(TennisRecorder tennis) {
        Score score = tennis.getScore();
        write("[%autowidth]\n|===\n");
        write("| A" + pointsToTable("A", tennis.points) + " | " + score.playerA() + " \n");
        write("| B" + pointsToTable("B", tennis.points) + " | " + score.playerB() + " \n");
        write("|===\n");
    }

    private String pointsToTable(String player, List<String> points) {
        if (points.isEmpty()) {
            return "";
        } else {
            return points.stream().map(p -> p.equals(player) ? "&#x2714;" : " ").collect(Collectors.joining(" | ", " | ", ""));
        }
    }
}

