package com.yellowforktech.littlefamilytree.sprites;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Rect;

/**
 * Created by jfinlay on 5/12/2015.
 */
public class ClippedRepeatedBackgroundSprite extends Sprite {
    protected int clipX;
    protected int clipY;
    protected int maxWidth;
    protected int maxHeight;
    protected Bitmap background;

    public ClippedRepeatedBackgroundSprite(Bitmap bitmap, int maxWidth, int maxHeight) {
        super();
        this.background = bitmap;
        this.maxWidth = maxWidth;
        this.maxHeight = maxHeight;
        this.scale = 1.0f;
    }

    public int getClipX() {
        return clipX;
    }

    public void setClipX(int clipX) {
        this.clipX = clipX;
    }

    public int getClipY() {
        return clipY;
    }

    public void setClipY(int clipY) {
        this.clipY = clipY;
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

    @Override
    public void doDraw(Canvas canvas) {
        if (background!=null) {
            Rect clipRect = new Rect();
            clipRect.set(0, clipY, background.getWidth(), clipY + (int)(height));
            Rect rect = new Rect();
            rect.set((int) (x), (int) (y), (int) ((x + width) ), (int) ((y + height) ));
            if (matrix!=null) {
                canvas.save();
                canvas.setMatrix(matrix);
            }
            canvas.drawBitmap(background, clipRect, rect, null);
            if (matrix!=null) {
                canvas.restore();
            }
        }
    }

    @Override
    public boolean onMove(float oldX, float oldY, float newX, float newY) {
        clipX -= (newX-oldX);
        clipY -= (newY-oldY);

        if (clipX < 0) clipX = 0;
        else if (clipX + (int)(width*scale) > maxWidth*scale) clipX = (int) ((maxWidth - width)*scale);

        if (clipY < 0) clipY = 0;
        else if (clipY + height*scale > maxHeight*scale) clipY = (int)((maxHeight - height)*scale);
        return true;
    }

    @Override
    public void doStep() {

    }

    @Override
    public void onSelect(float x, float y) {

    }

    @Override
    public void onRelease(float x, float y) {

    }

    @Override
    public void onDestroy() {
        if (background!=null && !background.isRecycled()) {
            background.recycle();
        }
        background = null;
    }
}
