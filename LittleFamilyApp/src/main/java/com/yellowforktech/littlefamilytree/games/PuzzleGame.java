package com.yellowforktech.littlefamilytree.games;

import com.yellowforktech.littlefamilytree.data.PuzzlePiece;

import java.util.Random;

/**
 * Created by jfinlay on 5/14/2015.
 */
public class PuzzleGame {
    private int rows = 2;
    private int cols = 2;
    private PuzzlePiece[][] board;

    public PuzzleGame(int rows, int cols) {
        setupLevel(rows, cols);
    }

    public void setupLevel(int rows, int cols) {
        this.rows = rows;
        this.cols = cols;
        board = new PuzzlePiece[rows][cols];

        fillBoard();
        randomizeBoard();
    }

    public void fillBoard() {
        for(int r=0; r<rows; r++) {
            for (int c=0; c<cols; c++) {
                board[r][c] = new PuzzlePiece(r, c);
            }
        }
    }

    public void randomizeBoard() {
        Random rand = new Random();
        for(int r=0; r<rows; r++) {
            for (int c = 0; c < cols; c++) {
                int rr = rand.nextInt(rows);
                int rc = rand.nextInt(cols);
                swap(r, c, rr, rc);
            }
        }
        for(int r=0; r<rows; r++) {
            for (int c = 0; c < cols; c++) {
                if (r==board[r][c].getRow() && c==board[r][c].getCol()) {
                    board[r][c].setInPlace(true);
                }
            }
        }
        if (isCompleted()) randomizeBoard();
    }

    public boolean isCompleted() {
        for(int r=0; r<rows; r++) {
            for (int c = 0; c < cols; c++) {
                if (!board[r][c].isInPlace()) return false;
            }
        }
        return true;
    }

    public PuzzlePiece getPiece(int row, int col) {
        if (row >= rows || col >= cols || row < 0 || col < 0)
            throw new IllegalArgumentException("Row or column out of bounds (" + row + "," + col + ")");
        return board[row][col];
    }

    public PuzzlePiece getHint() {
        PuzzlePiece hint = null;
        for(int r=0; r<rows; r++) {
            for (int c = 0; c < cols; c++) {
                if (!board[r][c].isInPlace()) {
                    hint = board[r][c];
                    break;
                }
            }
        }
        return hint;
    }

    public void swap(int row1, int col1, int row2, int col2) {
        if (row1 >= rows || col1 >= cols || row1<0 || col1<0) throw new IllegalArgumentException("Row1 or column1 out of bounds ("+row1+","+col1+")");
        if (row2 >= rows || col2 >= cols || row2<0 || col2<0) throw new IllegalArgumentException("Row2 or column2 out of bounds ("+row2+","+col2+")");
        PuzzlePiece p1 = board[row1][col1];
        PuzzlePiece p2 = board[row2][col2];
        board[row1][col1] = p2;
        board[row2][col2] = p1;
    }

    public int getCols() {
        return cols;
    }

    public int getRows() {
        return rows;
    }
}
