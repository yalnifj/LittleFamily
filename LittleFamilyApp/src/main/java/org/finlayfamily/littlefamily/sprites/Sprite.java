package org.finlayfamily.littlefamily.sprites;

import android.graphics.Canvas;
import android.graphics.Matrix;

/**
 * Created by jfinlay on 5/8/2015.
 */
public abstract class Sprite {
    protected float x;
    protected float y;
    protected int width;
    protected int height;
    protected int state;
    protected boolean selected;
    protected boolean selectable;
    protected boolean removeMe;
    protected Matrix matrix;
    protected float scale;

    public Sprite() {
        x = 0;
        y = 0;
        width = 0;
        height = 0;
        state = 0;
        selectable = false;
        removeMe = false;
        this.scale = 1;
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

    public float getX() {
        return x;
    }

    public void setX(float x) {
        this.x = x;
    }

    public float getY() {
        return y;
    }

    public void setY(float y) {
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

    public Matrix getMatrix() {
        return matrix;
    }

    public void setMatrix(Matrix matrix) {
        this.matrix = matrix;
    }

    public boolean isSelected() {
        return selected;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }

    public float getScale() {
        return scale;
    }

    public void setScale(float scale) {
        this.scale = scale;
    }

    public abstract void doStep();
    public abstract void doDraw(Canvas canvas);
    public abstract void onSelect(float x, float y);
    public abstract boolean onMove(float oldX, float oldY, float newX, float newY);
    public abstract void onRelease(float x, float y);

    public boolean inSprite(float tx, float ty) {
        if (!selectable) return false;
        if (tx>=x*scale && tx<=(x+width)*scale && ty>=y && ty<=(y+height)*scale) {
            return true;
        }
        return false;
    }

    public abstract void onDestroy();
}
