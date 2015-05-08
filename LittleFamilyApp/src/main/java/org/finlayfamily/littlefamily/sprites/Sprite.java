package org.finlayfamily.littlefamily.sprites;

import android.graphics.Canvas;

/**
 * Created by jfinlay on 5/8/2015.
 */
public abstract class Sprite {
    protected int x;
    protected int y;
    protected int width;
    protected int height;
    protected int state;

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public int getState() {
        return state;
    }

    public void setState(int state) {
        this.state = state;
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

    public abstract void doStep();
    public abstract void doDraw(Canvas canvas);
    public abstract void onSelect(float x, float y);
    public abstract void onMove(float oldX, float oldY, float newX, float newY);
    public abstract void onRelease(float x, float y);

    public boolean inSprite(float tx, float ty) {
        if (tx>=x && tx<=x+width && ty>=y && ty<=ty+height) {
            return true;
        }
        return false;
    }
}
