package org.finlayfamily.littlefamily.games;

import org.finlayfamily.littlefamily.data.MatchPerson;

import java.util.Random;

/**
 * Created by jfinlay on 5/14/2015.
 */
public class PuzzleGame {
    private int rows = 2;
    private int cols = 2;
    private int[] board;

    public PuzzleGame(int rows, int cols) {
        setupLevel(rows, cols);
    }

    public void setupLevel(int rows, int cols) {
        this.rows = rows;
        this.cols = cols;
        board = new int[rows * cols];

        fillBoard();
        randomizeBoard();
    }

    public void fillBoard() {
        for(int i=0; i<board.length; i++) {
            board[i] = i;
        }
    }

    public void randomizeBoard() {
        Random rand = new Random();
        for(int i=0; i<board.length; i++) {
            int r1 = rand.nextInt(board.length);
            int r2 = rand.nextInt(board.length);
            int p1 = board[r1];
            int p2 = board[r2];
            board[r2] = p1;
            board[r1] = p2;
        }
    }

    public boolean isCompleted() {
        for(int i=0; i<board.length; i++) {
            if (board[i]!=i) return false;
        }
        return true;
    }

    public int getSection(int row, int col) {
        if (row >= rows || col >= cols || row<0 || col<0) throw new IllegalArgumentException("Row or column out of bounds ("+row+","+col+")");
        return board[row*cols + col];
    }

    public int[] getHint() {
        int[] hint = new int[2];
        for(int i=0; i<board.length; i++) {
            if (board[i]!=i) {
                hint[0] = i;
                hint[1] = board[i];
                break;
            }
        }
        return hint;
    }

    public void swap(int row1, int col1, int row2, int col2) {
        if (row1 >= rows || col1 >= cols || row1<0 || col1<0) throw new IllegalArgumentException("Row1 or column1 out of bounds ("+row1+","+col1+")");
        if (row2 >= rows || col2 >= cols || row2<0 || col2<0) throw new IllegalArgumentException("Row2 or column2 out of bounds ("+row2+","+col2+")");
        int p1 = board[row1*cols + col1];
        int p2 = board[row2*cols + col2];
        board[row1*cols + col1] = p2;
        board[row2*cols + col2] = p1;
    }

    public int getCols() {
        return cols;
    }

    public int getRows() {
        return rows;
    }
}
