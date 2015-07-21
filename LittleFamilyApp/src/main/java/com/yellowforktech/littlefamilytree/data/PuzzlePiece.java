package com.yellowforktech.littlefamilytree.data;

/**
 * Created by jfinlay on 5/15/2015.
 */
public class PuzzlePiece {
    private int row;
    private int col;
    private int x;
    private int y;
    private boolean selected = false;
    private int toX;
    private int toY;
    private int width;
    private int height;
    private boolean inPlace = false;
    private boolean animating = false;

    public PuzzlePiece(int row, int col) {
        this.col = col;
        this.row = row;
    }

    public int getCol() {
        return col;
    }

    public void setCol(int col) {
        this.col = col;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public boolean isInPlace() {
        return inPlace;
    }

    public void setInPlace(boolean inPlace) {
        this.inPlace = inPlace;
    }

    public int getRow() {
        return row;
    }

    public void setRow(int row) {
        this.row = row;
    }

    public boolean isSelected() {
        return selected;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }

    public int getToX() {
        return toX;
    }

    public void setToX(int toX) {
        this.toX = toX;
    }

    public int getToY() {
        return toY;
    }

    public void setToY(int toY) {
        this.toY = toY;
    }

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public int getX() {
        return x;
    }

    public void setX(int x) {
        this.x = x;
    }

    public int getY() {
        return y;
    }

    public void setY(int y) {
        this.y = y;
    }

    public boolean isAnimating() {
        return animating;
    }

    public void setAnimating(boolean animating) {
        this.animating = animating;
    }
}
