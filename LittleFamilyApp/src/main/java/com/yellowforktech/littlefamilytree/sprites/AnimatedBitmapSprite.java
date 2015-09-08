package com.yellowforktech.littlefamilytree.sprites;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;

import com.yellowforktech.littlefamilytree.activities.tasks.BitmapSequenceLoader;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by jfinlay on 5/8/2015.
 */
public class AnimatedBitmapSprite extends Sprite implements BitmapSequenceLoader.Listener {
    protected Map<Integer, List<Bitmap>> bitmaps;
    protected Map<Integer, List<Integer>> bitmapIds;
    protected int frame;
    protected int stepsPerFrame;
    protected int steps;
    protected boolean bounce;
    protected boolean backward;
    protected Paint basePaint;
    protected boolean ignoreAlpha;
    protected Resources resources;
    protected boolean selected;
    protected Paint selectedPaint;
    protected int loadingState;

    public AnimatedBitmapSprite() {
        super();
        this.bitmaps = new HashMap<>();
        bitmapIds = new HashMap<>();
        frame = 0;
        stepsPerFrame = 3;
        basePaint = new Paint();
        basePaint.setColor(Color.WHITE);
        basePaint.setStyle(Paint.Style.FILL);
        bounce = false;

        selectedPaint = new Paint();
        selectedPaint.setColor(Color.YELLOW);
        selectedPaint.setStrokeWidth(3);
        selectedPaint.setAlpha(150);
        selectedPaint.setStyle(Paint.Style.STROKE);
    }

    public AnimatedBitmapSprite(Bitmap bitmap) {
        this();
        List<Bitmap> list = new ArrayList<>(1);
        bitmapIds = new HashMap<>();
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

    public Map<Integer, List<Integer>> getBitmapIds() {
        return bitmapIds;
    }

    public void setBitmapIds(Map<Integer, List<Integer>> bitmapIds) {
        this.bitmapIds = bitmapIds;
    }

    public Resources getResources() {
        return resources;
    }

    public void setResources(Resources resources) {
        this.resources = resources;
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

    @Override
    public boolean isSelected() {
        return selected;
    }

    @Override
    public void setSelected(boolean selected) {
        this.selected = selected;
    }

    public void setStepsPerFrame(int stepsPerFrame) {
        this.stepsPerFrame = stepsPerFrame;
    }

    public boolean isIgnoreAlpha() {
        return ignoreAlpha;
    }

    public void setIgnoreAlpha(boolean ignoreAlpha) {
        this.ignoreAlpha = ignoreAlpha;
    }

    public void addBitmap(int state, Bitmap bitmap) {
        List<Bitmap> stateBits = bitmaps.get(state);
        if (stateBits==null) {
            stateBits = new ArrayList<>(1);
            bitmaps.put(state, stateBits);
        }
        stateBits.add(bitmap);
    }

    @Override
    public void doStep() {
        if (bitmaps!=null && bitmaps.get(state)==null && bitmapIds.get(state)!=null && resources!=null) {
            List<Bitmap> loaded = new ArrayList<>(bitmapIds.get(state).size());
            for (Integer rid : bitmapIds.get(state)) {
                loaded.add(BitmapFactory.decodeResource(getResources(), rid));
            }
            bitmaps.put(state, loaded);

        }
        //--preload next state bitmaps
        if (state>0 && bitmaps!=null && bitmaps.get(state+1)==null && bitmapIds.get(state+1)!=null && resources!=null) {
            if (bitmaps.get(state).size()>4) {
                loadingState = state + 1;
                BitmapSequenceLoader loader = new BitmapSequenceLoader(getResources(), this);
                loader.execute(bitmapIds.get(state + 1));
            }
            else {
                List<Bitmap> loaded = new ArrayList<>(bitmapIds.get(state+1).size());
                for (Integer rid : bitmapIds.get(state+1)) {
                    loaded.add(BitmapFactory.decodeResource(getResources(), rid));
                }
                bitmaps.put(state+1, loaded);
            }
        }
        if (bitmaps!=null && bitmaps.get(state)!=null && bitmaps.get(state).size()>1 ) {
            steps++;
            if (steps >= stepsPerFrame) {
                if (backward) frame--;
                else frame++;
                if (frame >= bitmaps.get(state).size() || frame < 0) {
                    if (bounce) {
                        if (backward) frame = 1;
                        else frame = bitmaps.get(state).size()-2;
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
            synchronized (bitmaps) {
                List<Bitmap> frames = bitmaps.get(state);
                if (frames==null) frames = bitmaps.get(state-1);
                if (frames != null) {
                    if (frame >= 0 && frame < frames.size()) {
                        Bitmap bitmap = frames.get(frame);
                        if (bitmap!=null) {
                            Rect rect = new Rect();
                            rect.set((int) (x), (int) (y), (int) ((x + width)), (int) ((y + height)));
                            if (matrix != null) {
                                canvas.save();
                                canvas.setMatrix(matrix);
                            }
                            canvas.drawBitmap(bitmap, null, rect, basePaint);
                            if (matrix != null) {
                                canvas.restore();
                            }
                        }
                    }
                }
            }
        }
    }

    public boolean inSprite(float tx, float ty) {
        if (!selectable) return false;
        if (tx>=x*scale && tx<(x+width)*scale && ty>=y*scale && ty<=(y+height)*scale) {
            if (ignoreAlpha) {
                return true;
            }
            if (bitmaps!=null) {
                List<Bitmap> frames = bitmaps.get(state);
                Bitmap bitmap = frames.get(frame);
                int bx = (int)(tx-x*scale);
                int by = (int)(ty-y*scale);
                if (bx < bitmap.getWidth() && by < bitmap.getHeight()) {
                    int color = bitmap.getPixel(bx, by);
                    int alpha = Color.alpha(color);
                    if (alpha > 50) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    @Override
    public void onSelect(float x, float y) {
        selected = true;
    }

    @Override
    public boolean onMove(float oldX, float oldY, float newX, float newY) {
        return false;
    }

    @Override
    public void onRelease(float x, float y) {
        selected = false;
    }

    @Override
    public void onDestroy() {
        for(List<Bitmap> list : bitmaps.values()) {
            if (list!=null) {
                for (Bitmap bm : list) {
                    bm.recycle();
                }
                list.clear();
            }
        }
        bitmaps.clear();
    }

    public void freeStates() {
        for(Integer key : bitmapIds.keySet()) {
            List<Bitmap> bms = bitmaps.remove(key);
            if (bms!=null) {
                for(Bitmap b : bms) {
                    b.recycle();
                }
                bms.clear();
            }
        }
    }

    @Override
    public void onComplete(ArrayList<Bitmap> bitmaps) {
        this.bitmaps.put(loadingState, bitmaps);
    }
}
