package com.yellowforktech.littlefamilytree.sprites;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Rect;

import java.util.List;
import java.util.Map;

/**
 * Created by jfinlay on 5/12/2015.
 */
public class ClippedAnimatedBitmapSprite extends AnimatedBitmapSprite {
    protected int clipX;
    protected int clipY;
    protected int maxWidth;
    protected int maxHeight;

    public ClippedAnimatedBitmapSprite(Map<Integer, List<Bitmap>> bitmaps, int maxWidth, int maxHeight) {
        super(bitmaps);
        this.maxWidth = maxWidth;
        this.maxHeight = maxHeight;
        this.scale = 1.0f;
    }

    public ClippedAnimatedBitmapSprite(Bitmap bitmap, int maxWidth, int maxHeight) {
        super(bitmap);
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
        if (bitmaps!=null) {
            List<Bitmap> frames = bitmaps.get(state);
            if (frames != null) {
                if (frame >= 0 && frame < frames.size()) {
                    Bitmap bitmap = frames.get(frame);
                    Rect clipRect = new Rect();
                    clipRect.set(clipX, clipY, clipX + (int)(width), clipY + (int)(height));
                    Rect rect = new Rect();
                    rect.set((int) (x), (int) (y), (int) ((x + width) ), (int) ((y + height) ));
                    if (matrix!=null) {
                        if (oldScale!=scale) {
                            buildMatrix();
                            oldScale = scale;
                        }
                        canvas.save();
                        canvas.setMatrix(matrix);
                    }
                    canvas.drawBitmap(bitmap, clipRect, rect, basePaint);
                    if (matrix!=null) {
                        canvas.restore();
                    }
                }
            }
        }
    }

    @Override
    public boolean onMove(float oldX, float oldY, float newX, float newY) {
        clipX -= (newX-oldX);
        clipY -= (newY-oldY);

        if (clipX < 0) clipX = 0;
        if (clipX + (int)(width*scale) > maxWidth*scale) clipX = (int) ((maxWidth - width)*scale);

        if (clipY < 0) clipY = 0;
        if (clipY + height*scale > maxHeight*scale) clipY = (int)((maxHeight - height)*scale);
        return true;
    }
}
