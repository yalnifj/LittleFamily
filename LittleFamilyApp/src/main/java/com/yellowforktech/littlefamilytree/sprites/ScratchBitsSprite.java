package com.yellowforktech.littlefamilytree.sprites;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;

/**
 * Created by jfinlay on 5/8/2015.
 */
public class ScratchBitsSprite extends Sprite {
    protected Paint paint;
	protected int steps;
	
    public ScratchBitsSprite() {
        super();
		paint = new Paint();
		paint.setColor(Color.DKGRAY);
		paint.setStyle(Paint.Style.FILL_AND_STROKE);
		paint.setStrokeWidth(2);
		steps=0;
    }

    public void doStep() {
		setY(getY() + 8);
		steps++;
		if (steps > 20) {
			this.setRemoveMe(true);
		}
	}
	
    public void doDraw(Canvas canvas){
		canvas.drawLine(x+width/2, y, x, y+height, paint);
		canvas.drawLine(x, y+height, x+width, y+height, paint);
		canvas.drawLine(x+width/2, y, x+width, y+height, paint);
	}
	
    public void onSelect(float x, float y) { }
    public boolean onMove(float oldX, float oldY, float newX, float newY) { return false; }
    public void onRelease(float x, float y) { }

    public void onDestroy() {}
}