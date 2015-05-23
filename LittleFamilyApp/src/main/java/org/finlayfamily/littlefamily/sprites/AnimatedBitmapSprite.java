package org.finlayfamily.littlefamily.sprites;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by jfinlay on 5/8/2015.
 */
public class AnimatedBitmapSprite extends Sprite {
    protected Map<Integer, List<Bitmap>> bitmaps;
    protected int frame;
    protected int stepsPerFrame;
    protected int steps;
    protected boolean bounce;
    protected boolean backward;
    protected Paint basePaint;

    public AnimatedBitmapSprite() {
        super();
        this.bitmaps = new HashMap<>();
        frame = 0;
        stepsPerFrame = 3;
        basePaint = new Paint();
        basePaint.setColor(Color.WHITE);
        basePaint.setStyle(Paint.Style.FILL);
        bounce = false;
    }

    public AnimatedBitmapSprite(Bitmap bitmap) {
        this();
        List<Bitmap> list = new ArrayList<>(1);
        list.add(bitmap);
        bitmaps.put(0, list);
        this.setWidth(bitmap.getWidth());
        this.setHeight(bitmap.getHeight());
    }

    public AnimatedBitmapSprite(Map<Integer, List<Bitmap>> bitmaps) {
        super();
        bounce = false;
        this.bitmaps = bitmaps;
        frame = 0;
        stepsPerFrame = 3;
        basePaint = new Paint();
    }

    public Map<Integer, List<Bitmap>> getBitmaps() {
        return bitmaps;
    }

    public void setBitmaps(Map<Integer, List<Bitmap>> bitmaps) {
        this.bitmaps = bitmaps;
    }

    public boolean isBounce() {
        return bounce;
    }

    public void setBounce(boolean bounce) {
        this.bounce = bounce;
    }

    public int getFrame() {
        return frame;
    }

    public void setFrame(int frame) {
        this.frame = frame;
    }

    public int getStepsPerFrame() {
        return stepsPerFrame;
    }

    public void setStepsPerFrame(int stepsPerFrame) {
        this.stepsPerFrame = stepsPerFrame;
    }

    @Override
    public void doStep() {
        if (bitmaps!=null && bitmaps.get(state)!=null && bitmaps.get(state).size()>1 ) {
            steps++;
            if (steps >= stepsPerFrame) {
                if (backward) frame--;
                else frame++;
                if (frame >= bitmaps.get(state).size() || frame < 0) {
                    if (bounce) {
                        if (backward) frame -= 2;
                        else frame += 2;
                        backward = !backward;
                    } else {
                        frame = 0;
                    }
                }
                steps = 0;
            }
        }
    }

    @Override
    public void doDraw(Canvas canvas) {
        if (bitmaps!=null) {
            List<Bitmap> frames = bitmaps.get(state);
            if (frames != null) {
                if (frame >= 0 && frame < frames.size()) {
                    Bitmap bitmap = frames.get(frame);
                    Rect rect = new Rect();
                    rect.set((int)x, (int)y, (int)x + width, (int)y + height);
                    if (matrix!=null) {
                        canvas.save();
                        canvas.setMatrix(matrix);
                    }
                    canvas.drawBitmap(bitmap, null, rect, basePaint);
                    if (matrix!=null) {
                        canvas.restore();
                    }
                }
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
}
