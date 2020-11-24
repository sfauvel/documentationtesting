package org.sfvl.demo;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.sfvl.doctesting.junitextension.ApprovalsExtension;
import org.sfvl.doctesting.DocWriter;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * We will display a tennis score.
 */
@DisplayName(value="Scores examples")
public class TennisTest {

    private final DocWriter docWriter = new DocWriter();
    @RegisterExtension
    ApprovalsExtension extension = new ApprovalsExtension(docWriter);

    private void write(String... texts) {
        docWriter.write(texts);
    }


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


    private void displayScore(TennisRecorder tennis) {
        Score score = tennis.getScore();
        String textScore = score.playerA() + " - " + score.playerB();
        write("[%autowidth, cols=" + (tennis.points.size()+2) + "*, stripes=none]\n|===\n");
        write("| Player A" + pointsToTable("A", tennis.points) + "\n.2+^.^| *" + textScore + "* \n");
        write("| Player B" + pointsToTable("B", tennis.points) + "| \n");
        write("|===\n");

        writeStyle();
    }

    private void writeStyle() {
        write("", 
                "++++",
                "<style>",
                "table.tableblock.grid-all {",
                "    border-collapse: collapse;",
                "}",
                "table.tableblock.grid-all, table.tableblock.grid-all td, table.grid-all > * > tr > .tableblock:last-child {",
                "    border: 1px solid #dddddd;",
                "}",
                "</style>",
                "++++",
                "");
    }

    private String pointsToTable(String player, List<String> points) {
        if (points.isEmpty()) {
            return "";
        } else {
            return points.stream().map(p -> p.equals(player) ? "&#x2714;" : " ").collect(Collectors.joining(" | ", " | ", ""));
        }
    }
}

