package org.finlayfamily.littlefamily.sprites;

import android.graphics.Bitmap;

import java.util.List;
import java.util.Map;

/**
 * Created by jfinlay on 5/20/2015.
 */
public class BouncingAnimatedBitmapSprite extends AnimatedBitmapSprite {
    protected float slope;
    protected float speed;
    protected int maxWidth;
    protected int maxHeight;
    protected int minX = 0;
    protected int minY = 0;

    public BouncingAnimatedBitmapSprite(Bitmap bitmap, int maxWidth, int maxHeight) {
        super(bitmap);
        this.maxWidth = maxWidth;
        this.maxHeight = maxHeight;
    }

    public BouncingAnimatedBitmapSprite(Map<Integer, List<Bitmap>> bitmaps, int maxWidth, int maxHeight) {
        super(bitmaps);
        this.maxWidth = maxWidth;
        this.maxHeight = maxHeight;
    }

    public float getSlope() {
        return slope;
    }

    public void setSlope(float slope) {
        this.slope = slope;
    }

    public float getSpeed() {
        return speed;
    }

    public void setSpeed(float speed) {
        this.speed = speed;
    }

    public int getMaxHeight() {
        return maxHeight;
    }

    public void setMaxHeight(int maxHeight) {
        this.maxHeight = maxHeight;
    }

    public int getMaxWidth() {
        return maxWidth;
    }

    public void setMaxWidth(int maxWidth) {
        this.maxWidth = maxWidth;
    }

    public int getMinX() {
        return minX;
    }

    public void setMinX(int minX) {
        this.minX = minX;
    }

    public int getMinY() {
        return minY;
    }

    public void setMinY(int minY) {
        this.minY = minY;
    }

    @Override
    public void doStep() {
        super.doStep();
        this.x = this.x + this.speed;
        this.y = this.y + this.slope * this.speed;
        if (this.x + this.width > this.maxWidth) {
            this.speed = -1 * this.speed;
            this.slope = -1 * this.slope;
        }
        if (this.x < minX) {
            this.speed = -1 * this.speed;
            this.slope = -1 * this.slope;
        }
        if (this.y + this.height > this.maxHeight) {
            this.slope = -1 * this.slope;
        }
        if (this.y < 0) {
            this.slope = -1 * this.slope;
        }
    }
}
