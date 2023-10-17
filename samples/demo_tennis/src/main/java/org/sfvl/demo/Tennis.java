package org.sfvl.demo;

public class Tennis {

    int nbPointsA = 0;
    int nbPointsB = 0;

    public Score getScore() {
        return Score.build(nbPointsA, nbPointsB);
    }

    public void playerAWinPoint() {
        nbPointsA++;
    }

    public void playerBWinPoint() {
        nbPointsB++;
    }
}
