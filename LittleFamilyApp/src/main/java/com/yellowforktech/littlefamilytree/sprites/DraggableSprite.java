package com.yellowforktech.littlefamilytree.sprites;

import android.graphics.Bitmap;
import android.util.DisplayMetrics;

/**
 * Created by jfinlay on 8/26/2015.
 */
public class DraggableSprite extends TouchEventGameSprite {
    protected float sx;
    protected float sy;
    protected int maxWidth;
    protected int maxHeight;
	protected boolean moving;
    protected Sprite target;
	protected int sw;
	protected int sh;
	protected int thresholdX = 8;
	protected int thresholdY = 8;

    public DraggableSprite(Bitmap bitmap, int maxWidth, int maxHeight, String eventTopic, DisplayMetrics dm) {
        super(bitmap, eventTopic, dm);
        selectable = true;
        this.maxHeight = maxHeight;
        this.maxWidth = maxWidth;
    }

	public void setMaxWidth(int maxWidth)
	{
		this.maxWidth = maxWidth;
	}

	public int getMaxWidth()
	{
		return maxWidth;
	}

	public void setMaxHeight(int maxHeight)
	{
		this.maxHeight = maxHeight;
	}

	public int getMaxHeight()
	{
		return maxHeight;
	}

	public int getThresholdX() {
		return thresholdX;
	}

	public void setThresholdX(int thresholdX) {
		this.thresholdX = thresholdX;
	}

	public int getThresholdY() {
		return thresholdY;
	}

	public void setThresholdY(int thresholdY) {
		this.thresholdY = thresholdY;
	}

	public Sprite getTarget() {
        return target;
    }

    public void setTarget(Sprite target) {
        this.target = target;
    }

	public void setTargetX(int x) {
		sx = x;
	}

	public void setTargetY(int y) {
		sy = y;
	}

	public void setTargetWidth(int w) {
		float r = (float) height/width;
		this.sw = w;
		this.sh = (int) (w * r);
	}

	public void setTargetHeight(int h) {
		float r = (float) height/width;
		this.sh = h;
		this.sw = (int) (h / r);
	}

	public void setTargetWidthHeight(int w, int h) {
		this.sw = w;
		this.sh = h;
	}

	public int getTargetWidth() {
		return sw;
	}

	public int getTargetHeight() {
		return sh;
	}

	public boolean isMoving() {
		return moving;
	}

	public void setMoving(boolean moving) {
		this.moving = moving;
	}

	@Override
	public void doStep()
	{
		super.doStep();
		if (moving) {
			if (x > sx - 3 && x < sx+3 && y>sy-3 && y<sy+3) {
				this.x = sx;
				this.y = sy;
				moving=false;
				this.width = sw;
				this.height = sh;
			} else {
				float dx = (sx - x)/6f;
				if (dx < 0 && dx > -2) dx = -2;
				if (dx > 0 && dx < 2) dx = 2;
				float dy = (sy - y)/6f;
				if (dy < 0 && dy > -2) dy = -2;
				if (dy > 0 && dy < 2) dy = 2;
				x=x+dx;
				y=y+dy;
				float dw = (sw - width)/6f;
				float dh = (sh - height)/6f;
				if (Math.abs(dw) < 1) {
					this.width=sw;
				} else {
					this.width += dw;
				}
				if (Math.abs(dh) < 1) {
					this. height = sh;
				} else {
					this.height += dh;
				}
			}
			
		}
	}

    @Override
    public boolean onMove(float oldX, float oldY, float newX, float newY) {
        float xdiff = newX-oldX;
		float ydiff = newY-oldY;
        if (!moved && (Math.abs(xdiff) < thresholdX && Math.abs(ydiff) < thresholdY)) {
            return false;
        }

        x += xdiff;
        if (x<0) x = 0;
        if (x + width > maxWidth) x = maxWidth - width;

        y += ydiff;
        if (y<0) y = 0;
        if (y +height > maxHeight) y = maxHeight - height;

        moved = true;
        return true;
    }

    @Override
    public void onSelect(float x, float y) {
        sx = this.x;
        sy = this.y;
		sw = this.width;
		sh = this.height;
        super.onSelect(x, y);
		moved = false;
    }

    @Override
    public void onRelease(float x, float y) {
        if (moved) {
			moving = true;
        } else {
			super.onRelease(x, y);
		}
		moved = false;
    }
}
