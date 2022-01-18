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
        assertEquals("15", score.playerA());
        assertEquals("30", score.playerB());
    }
}
