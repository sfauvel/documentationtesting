package org.sfvl;

public class GameOfLife {
    boolean[][] board = new boolean[10][10];

    public void setAlive(int line, int column) {
        board[line][column] = true;
    }

    public void nexGeneration() {
        boolean[][] newboard = new boolean[10][10];
        for (int line = 0; line < board.length; line++) {
            for (int column = 0; column < board[line].length; column++) {
                int neighbours = countNeighbours(line, column);
                final Cell cell = board[line][column] ? Cell.alive() : Cell.dead();

                newboard[line][column] = cell.nextState(neighbours);
            }
        }
        board = newboard;
    }

    private int countNeighbours(int cellLine, int cellColumn) {
        int neighbours = 0;
        for (int line = Math.max(0, cellLine-1); line <= Math.min(cellLine+1, board.length-1); line++) {
            for (int column = Math.max(0, cellColumn-1); column <= Math.min(cellColumn+1, board[line].length-1); column++) {
                if (board[line][column] && !(cellLine == line && cellColumn == column)) {
                    neighbours++;
                }
            }
        }
        return neighbours;
    }

    public boolean getAlive(int line, int column) {
        return board[line][column];
    }


}
