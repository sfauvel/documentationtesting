package org.sfvl.demo;

public abstract class Score {
    public static Score build(int nbPointsA, int nbPointsB) {
        if (nbPointsA <= 3 && nbPointsB <= 3) {
            return new StandardScore(nbPointsA, nbPointsB);
        } else if (nbPointsA == nbPointsB) {
            return new ScoreDeuce();
        } else if (Math.abs(nbPointsA-nbPointsB)>=2) {
            return new ScoreGame(nbPointsA, nbPointsB);
        } else {
            return new ScoreAdvantage(nbPointsA, nbPointsB);
        }
    }

    private static class StandardScore extends Score {

        private final int nbPointsA;
        private final int nbPointsB;
        public StandardScore(int nbPointsA, int nbPointsB) {
            this.nbPointsA = nbPointsA;
            this.nbPointsB = nbPointsB;
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

        public String playerA() {
            return convertPtsToScore(nbPointsA);
        }

        public String playerB() {
            return convertPtsToScore(nbPointsB);
        }

        @Override
        public String toString() {
            return playerA() + " - " + playerB();
        }
    }

    private static class ScoreDeuce extends Score {
        @Override
        public String toString() {
            return "Deuce";
        }
    }
    private static class ScoreAdvantage extends Score {
        final String player;
        public ScoreAdvantage(int nbPointsA, int nbPointsB) {
            player = nbPointsA > nbPointsB ? "A" : "B";
        }

        @Override
        public String toString() {
            return "Adv. " + player;
        }
    }

    private static class ScoreGame extends Score {
        final String player;
        public ScoreGame(int nbPointsA, int nbPointsB) {
            player = nbPointsA > nbPointsB ? "A" : "B";
        }

        @Override
        public String toString() {
            return "Game " + player;
        }
    }
}
