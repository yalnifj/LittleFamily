package com.yellowforktech.littlefamilytree.sprites;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Rect;

/**
 * Created by kids on 7/7/15.
 */
public class StarSprite extends Sprite {
    private Bitmap starBitmap;
    private boolean fadeOut;
    private int maxWidth;
    private int maxHeight;

    public StarSprite(Bitmap starBitmap, boolean growing, boolean fadeOut) {
        this.starBitmap = starBitmap;
        this.fadeOut = fadeOut;
        maxWidth = starBitmap.getWidth();
        maxHeight = starBitmap.getHeight();
        if (growing) {
            this.state = 0;
            this.setWidth(1);
            this.setHeight(1);
        }
        else {
            this.state = 1;
            this.setWidth(starBitmap.getWidth());
            this.setHeight(starBitmap.getHeight());
        }
    }

    @Override
    public void doStep() {
        if (state==0) {
            width+=2;
            x-=1;
            height+=2;
            y-=1;
            if (width>=maxWidth) state=1;
        } else {
            width-=2;
            x+=1;
            height-=2;
            y+=1;
            if (width<=0 || height<=0) {
                width = 0;
                height = 0;
                state=0;
                if (fadeOut) {
                    this.setRemoveMe(true);
                }
            }
        }
    }

    @Override
    public void doDraw(Canvas canvas) {
        if (starBitmap!=null) {
            Rect rect = new Rect();
            rect.set((int) (x), (int) (y), (int) ((x + width)), (int) ((y + height)));
            if (matrix != null) {
                canvas.save();
                canvas.setMatrix(matrix);
            }
            canvas.drawBitmap(starBitmap, null, rect, null);
            if (matrix != null) {
                canvas.restore();
            }
        }
    }

    @Override
    public void onSelect(float x, float y) {

    }

    @Override
    public boolean onMove(float oldX, float oldY, float newX, float newY) {
        return false;
    }

    @Override
    public void onRelease(float x, float y) {

    }

    @Override
    public void onDestroy() {
        starBitmap = null;
    }
}
