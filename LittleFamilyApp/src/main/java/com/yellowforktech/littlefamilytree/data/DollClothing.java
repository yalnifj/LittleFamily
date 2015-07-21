package com.yellowforktech.littlefamilytree.data;

import android.graphics.Bitmap;

/**
 * Created by jfinlay on 4/17/2015.
 */
public class DollClothing {
    private int snapX;
    private int snapY;
    private int x;
    private int y;
    private String filename;
    private boolean placed;
    private Bitmap bitmap;
    private Bitmap outline;

    public DollClothing(int snapX, int snapY, String filename) {
        this.snapX = snapX;
        this.snapY = snapY;
        this.filename = filename;
    }

    public int getSnapX() {
        return snapX;
    }

    public void setSnapX(int snapX) {
        this.snapX = snapX;
    }

    public int getSnapY() {
        return snapY;
    }

    public void setSnapY(int snapY) {
        this.snapY = snapY;
    }

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public boolean isPlaced() {
        return placed;
    }

    public void setPlaced(boolean placed) {
        this.placed = placed;
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

    public Bitmap getBitmap() {
        return bitmap;
    }

    public void setBitmap(Bitmap bitmap) {
        this.bitmap = bitmap;
    }

    public Bitmap getOutline() {
        return outline;
    }

    public void setOutline(Bitmap outline) {
        this.outline = outline;
    }
}
