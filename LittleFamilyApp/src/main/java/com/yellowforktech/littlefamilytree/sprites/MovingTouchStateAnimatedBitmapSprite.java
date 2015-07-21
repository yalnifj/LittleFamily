package com.yellowforktech.littlefamilytree.sprites;

import android.content.Context;
import android.graphics.Bitmap;

import java.util.List;
import java.util.Map;

/**
 * Created by kids on 7/8/15.
 */
public class MovingTouchStateAnimatedBitmapSprite extends TouchStateAnimatedBitmapSprite {
    protected float slope;
    protected float speed;
    protected boolean wrap;
    protected int maxWidth;
    protected int maxHeight;

    public MovingTouchStateAnimatedBitmapSprite(Context context) {
        super(context);
    }

    public MovingTouchStateAnimatedBitmapSprite(Bitmap bitmap, Context context) {
        super(bitmap, context);
    }

    public MovingTouchStateAnimatedBitmapSprite(Map<Integer, List<Bitmap>> bitmaps, Context context) {
        super(bitmaps, context);
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

    public boolean isWrap() {
        return wrap;
    }

    public void setWrap(boolean wrap) {
        this.wrap = wrap;
    }

    @Override
    public void doStep() {
        super.doStep();

        this.x = this.x + this.speed;
        this.y = this.y + this.slope;
        if (this.speed > 0) {
            if (this.x > this.maxWidth) {
                if (wrap) {
                    this.x = 0 - (this.width - this.speed);
                } else {
                    setRemoveMe(true);
                }
            }
        } else if (this.speed < 0) {
            if (this.x + this.width < 0) {
                if (wrap) {
                    this.x = this.maxWidth + this.speed;
                } else {
                    setRemoveMe(true);
                }
            }
        }

        if (this.slope > 0) {
            if (this.y > this.maxHeight) {
                if (wrap) {
                    this.y = 0.0f - (this.height - this.slope);
                } else {
                    setRemoveMe(true);
                }
            }
        } else if (this.slope < 0) {
            if (this.y + this.height < 0) {
                if (wrap) {
                    this.y = this.maxHeight + this.slope;
                } else {
                    setRemoveMe(true);
                }
            }
        }
    }
}
