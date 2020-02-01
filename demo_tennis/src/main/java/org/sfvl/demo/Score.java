package org.sfvl.demo;

public class Score {
    private final String scoreA;
    private final String scoreB;


    public Score(int nbPointsA, int nbPointsB) {
        scoreA = convertPtsToScore(nbPointsA);
        scoreB = convertPtsToScore(nbPointsB);
    }

    private static String convertPtsToScore(int nbPoints) {
        switch (nbPoints) {
            case 0 : return "0" ;
            case 1 : return "15" ;
            case 2 : return "30" ;
            case 3 : return "40" ;
            default : throw  new RuntimeException();
        }
    }

    public Score() {
        this(0,0);
    }

    public String playerA() {
        return scoreA;
    }

    public String playerB() {
        return scoreB;
    }
}
