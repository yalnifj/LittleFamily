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
    protected boolean selectable;
    protected boolean removeMe;

    public Sprite() {
        x = 0;
        y = 0;
        width = 0;
        height = 0;
        state = 0;
        selectable = false;
        removeMe = false;
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

    public int getHeight() {
        return height;
    }

    public boolean isSelectable() {
        return selectable;
    }

    public void setSelectable(boolean selectable) {
        this.selectable = selectable;
    }

    public boolean isRemoveMe() {
        return removeMe;
    }

    public void setRemoveMe(boolean removeMe) {
        this.removeMe = removeMe;
    }

    public abstract void doStep();
    public abstract void doDraw(Canvas canvas);
    public abstract void onSelect(float x, float y);
    public abstract void onMove(float oldX, float oldY, float newX, float newY);
    public abstract void onRelease(float x, float y);

    public boolean inSprite(float tx, float ty) {
        if (!selectable) return false;
        if (tx>=x && tx<=x+width && ty>=y && ty<=ty+height) {
            return true;
        }
        return false;
    }
}
