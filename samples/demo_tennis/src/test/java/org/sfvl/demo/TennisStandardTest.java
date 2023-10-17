package org.sfvl.demo;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Classic test to check Tennis.
 */
public class TennisStandardTest {

    @Test
    public void xx(){
        final Tennis game = new Tennis();
        game.playerAWinPoint();
        game.playerBWinPoint();
        game.playerBWinPoint();

        final Score score = game.getScore();
        assertEquals("15-30", score.toString());
    }

    @Test
    public void score_evolution() {
        final Tennis tennis = new Tennis();
        assertEquals("0 - 0", tennis.getScore().toString());
        tennis.playerBWinPoint();
        assertEquals("0 - 15", tennis.getScore().toString());
        tennis.playerAWinPoint();
        assertEquals("15 - 15", tennis.getScore().toString());
        tennis.playerAWinPoint();
        assertEquals("30 - 15", tennis.getScore().toString());
        tennis.playerBWinPoint();
        assertEquals("30 - 30", tennis.getScore().toString());
        tennis.playerBWinPoint();
        assertEquals("30 - 40", tennis.getScore().toString());
        tennis.playerAWinPoint();
        assertEquals("40 - 40", tennis.getScore().toString());
        tennis.playerAWinPoint();
        assertEquals("Adv. A", tennis.getScore().toString());
        tennis.playerBWinPoint();
        assertEquals("Deuce", tennis.getScore().toString());
        tennis.playerBWinPoint();
        assertEquals("Adv. B", tennis.getScore().toString());
        tennis.playerBWinPoint();
        assertEquals("Game B", tennis.getScore().toString());

    }
}
