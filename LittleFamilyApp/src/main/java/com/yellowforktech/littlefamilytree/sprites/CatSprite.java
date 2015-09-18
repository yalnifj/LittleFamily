package com.yellowforktech.littlefamilytree.sprites;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Rect;

import com.yellowforktech.littlefamilytree.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by jfinlay on 9/17/2015.
 */
public class CatSprite extends AnimatedBitmapSprite {
    private float startY;
    private int stateCounter = 0;
    private Context context;
    private int reveal = 0;
    private int revealSteps = 5;
    private boolean animating = false;
    private int blinkCount = 0;
    private int blinkDelay = 12;
    private int blinkSteps = 0;

    public CatSprite(Context context) {
        super(BitmapFactory.decodeResource(context.getResources(), R.drawable.house_familyroom_cat_a1));

        this.context = context;
        this.resources = context.getResources();

        List<Integer> blinking = new ArrayList<>(5);
        blinking.add(R.drawable.house_familyroom_cat_a1);
        blinking.add(R.drawable.house_familyroom_cat_a2);
        blinking.add(R.drawable.house_familyroom_cat_a3);
        blinking.add(R.drawable.house_familyroom_cat_a4);
        blinking.add(R.drawable.house_familyroom_cat_a5);
        bitmapIds.put(1, blinking);
    }

    public boolean isAnimating() {
        return animating;
    }

    public void setAnimating(boolean animating) {
        this.animating = animating;
        reveal = 1;
    }

    @Override
    public void setY(float y) {
        super.setY(y);
        startY = y;
    }

    @Override
    public void doStep() {
        int oldFrame = frame;
        if (state==1) {
            bounce = true;
        } else {
            bounce = false;
        }

        int oldSteps = steps;

        if (state==0 && reveal==revealSteps) {
            reveal = 0;
            state = 1;
            oldFrame = 0;
            frame = 0;
            blinkCount = 0;
        }

        if (state==3 && reveal==revealSteps) {
            reveal = 0;
            state = 0;
            oldFrame = 0;
            frame = 0;
            blinkCount = 0;
            y = startY;
            animating = false;
        }

        if ((state==0 || state==3) && animating) {
            steps++;
            if (steps >= stepsPerFrame) {
                if (backward) frame--;
                else frame++;
                if (frame >= bitmaps.get(0).size() || frame < 0) {
                    if (bounce) {
                        if (backward) frame = 1;
                        else frame = bitmaps.get(0).size()-2;
                        backward = !backward;
                    } else {
                        frame = 0;
                    }
                }
                steps = 0;
            }
        } else {
            super.doStep();
        }

        if (state==0 && oldSteps!=0 && steps==0 && animating) {
            reveal++;
            if (reveal<5) {
                float dy = (float) height / revealSteps;
                y += dy;
            }
        }

        if (state==3 && oldSteps!=0 && steps==0 && animating) {
            reveal++;
            if (reveal>1) {
                float dy = (float) height / revealSteps;
                y -= dy;
            }
        }

        if (state==1 && oldFrame!=0 && frame==0) {
            blinkCount++;
            if (blinkCount>1) {
                state = 3;
            }
        }

        if (state==1 && oldFrame==0 && frame==1) {
            blinkSteps++;
            if (blinkSteps<blinkDelay) {
                frame = 0;
            } else {
                blinkSteps = 0;
            }
        }
    }

    @Override
    public void doDraw(Canvas canvas) {
        if (state==0 || state==3) {
            synchronized (bitmaps) {
                if (animating) {
                    List<Bitmap> frames = bitmaps.get(0);
                    Bitmap bitmap = frames.get(0);
                    if (bitmap != null) {
                        float dy = (float) height / revealSteps;
                        int top = (int) (height - (dy*reveal));
                        if (state==3) top = (int) (height - (dy * (revealSteps-reveal)));
                        if (top < 0) top = 0;

                        Rect src = new Rect();
                        src.set(0, top, bitmap.getWidth(), bitmap.getHeight());

                        Rect rect = new Rect();
                        rect.set((int) (x), (int) (y+top), (int) ((x + width)), (int) ((y+ height)));
                        canvas.drawBitmap(bitmap, src, rect, basePaint);
                    }
                }
            }
        } else {
            super.doDraw(canvas);
        }
    }


}
