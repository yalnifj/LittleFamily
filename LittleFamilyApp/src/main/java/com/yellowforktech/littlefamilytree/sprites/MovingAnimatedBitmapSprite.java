package com.yellowforktech.littlefamilytree.sprites;

import android.graphics.Bitmap;
import android.graphics.PointF;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by jfinlay on 5/20/2015.
 */
public class MovingAnimatedBitmapSprite extends AnimatedBitmapSprite {
    protected float slope;
    protected float speed;
    protected boolean wrap;
    protected boolean ignoreBounds = false;
    protected int maxWidth;
    protected int maxHeight;

    protected Map<Integer, PointF> stateSpeeds;
    protected Map<Integer, PointF> stateTargets;

    public MovingAnimatedBitmapSprite(Bitmap bitmap, int maxWidth, int maxHeight) {
        super(bitmap);
        this.maxWidth = maxWidth;
        this.maxHeight = maxHeight;
        stateSpeeds = new HashMap<>();
        stateTargets = new HashMap<>();
    }

    public MovingAnimatedBitmapSprite(Map<Integer, List<Bitmap>> bitmaps, int maxWidth, int maxHeight) {
        super(bitmaps);
        this.maxWidth = maxWidth;
        this.maxHeight = maxHeight;
        stateSpeeds = new HashMap<>();
        stateTargets = new HashMap<>();
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

    public boolean isIgnoreBounds() {
        return ignoreBounds;
    }

    public void setIgnoreBounds(boolean ignoreBounds) {
        this.ignoreBounds = ignoreBounds;
    }

    public void setStateSpeed(int state, PointF speed) {
        stateSpeeds.put(state, speed);
    }

    public void setStateTarget(int state, PointF target) {
        stateTargets.put(state, target);
    }

    @Override
    public void doStep() {
        super.doStep();
        if (this.stateSpeeds.get(state) != null) {
            this.speed = this.stateSpeeds.get(state).x;
            this.slope = this.stateSpeeds.get(state).y;
        }
        this.x = this.x + this.speed;
        this.y = this.y + this.slope;
        if (this.speed > 0) {
            if (stateTargets.get(state) != null) {
                if (this.x > stateTargets.get(state).x) {
                    this.x = stateTargets.get(state).x;
                    this.speed = 0;
                }
            }
            if (this.x > this.maxWidth) {
                if (wrap) {
                    this.x = 0 - (this.width - this.speed);
                } else if (!ignoreBounds) {
                    setRemoveMe(true);
                }
            }
        } else if (this.speed < 0) {
            if (stateTargets.get(state) != null) {
                if (this.x < stateTargets.get(state).x) {
                    this.x = stateTargets.get(state).x;
                    this.speed = 0;
                }
            }
            if (this.x + this.width < 0) {
                if (wrap) {
                    this.x = this.maxWidth + this.speed;
                } else if (!ignoreBounds) {
                    setRemoveMe(true);
                }
            }
        }

        if (this.slope > 0) {
            if (stateTargets.get(state) != null) {
                if (this.y > stateTargets.get(state).y) {
                    this.y = stateTargets.get(state).y;
                    this.slope = 0;
                }
            }
            if (this.y > this.maxHeight) {
                if (wrap) {
                    this.y = 0.0f - (this.height - this.slope);
                } else {
                    setRemoveMe(true);
                }
            }
        } else if (this.slope < 0) {
            if (stateTargets.get(state) != null) {
                if (this.y < stateTargets.get(state).y) {
                    this.y = stateTargets.get(state).y;
                    this.slope = 0;
                }
            }
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
