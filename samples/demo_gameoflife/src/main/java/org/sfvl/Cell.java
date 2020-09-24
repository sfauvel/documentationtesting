package org.sfvl;

public abstract class Cell {

    boolean state = true;
    public static Cell alive() {
        return new CellAlive();
    }

    public static Cell dead() {
        return new CellDead();
    }

    abstract public boolean nextState(int neighbours);

    public abstract boolean isAlive();

    static class CellAlive extends Cell {
        public boolean nextState(int neighbours) {
            return 1 < neighbours && neighbours < 4;
        }

        @Override
        public boolean isAlive() {
            return true;
        }
    }

    static class CellDead extends Cell {
        public boolean nextState(int neighbours) {
            return neighbours == 3;
        }

        @Override
        public boolean isAlive() {
            return false;
        }
    }
}
