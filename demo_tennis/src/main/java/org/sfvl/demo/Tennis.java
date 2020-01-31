package org.sfvl.demo;

public class Tennis {

    int nbPointsA = 0;
    int nbPointsB = 0;

    public Score getScore() {
        return new Score(nbPointsA, nbPointsB);
    }

    public void joueurAMarque() {
        nbPointsA++;
    }

    public void joueurBMarque() {
        nbPointsB++;
    }
}
