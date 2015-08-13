package com.yellowforktech.littlefamilytree.sprites;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Rect;
import android.media.MediaPlayer;
import android.view.HapticFeedbackConstants;

import com.yellowforktech.littlefamilytree.events.EventQueue;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by kids on 5/21/15.
 */
public class TouchStateAnimatedBitmapSprite extends AnimatedBitmapSprite {
    public static final int TRANSITION_CLICK = 0;
    public static final int TRANSITION_LOOP1 = 1;
    public static final int TRANSITION_LOOP2 = 2;
    public static final int TRANSITION_LOOP3 = 3;
    public static final int TRANSITION_LOOP4 = 4;
    public static final int TRANSITION_LOOP5 = 5;

    protected Map<Integer, Integer> audio;
    protected Map<Integer, Integer> stateTransitions;
    protected Map<Integer, String> stateTransitionEvents;
    protected Map<Integer, Rect> touchRectangles;
    protected boolean stateChanged;
    protected Context context;
    protected int loops;
    protected boolean moved;

    public TouchStateAnimatedBitmapSprite(Context context) {
        audio = new HashMap<>();
        this.context = context;
        stateTransitions = new HashMap<>();
        stateTransitionEvents = new HashMap<>();
        touchRectangles = new HashMap<>();
    }

    public TouchStateAnimatedBitmapSprite(Bitmap bitmap, Context context) {
        super(bitmap);
        audio = new HashMap<>();
        this.context = context;
        stateTransitions = new HashMap<>();
        stateTransitionEvents = new HashMap<>();
        touchRectangles = new HashMap<>();
    }

    public TouchStateAnimatedBitmapSprite(Map<Integer, List<Bitmap>> bitmaps, Context context) {
        super(bitmaps);
        audio = new HashMap<>();
        this.context = context;
        stateTransitions = new HashMap<>();
    }

    public Map<Integer, Integer> getAudio() {
        return audio;
    }

    public void setAudio(Map<Integer, Integer> audio) {
        this.audio = audio;
    }

    public void setStateTransition(int state, int transition) {
        stateTransitions.put(state, transition);
    }

    public void setStateTransitionEvent(int state, String topic) {
        stateTransitionEvents.put(state, topic);
    }

    public void setTouchRectangles(int state, Rect rect) {
        touchRectangles.put(state, rect);
    }

    @Override
    public void doStep() {
        if (stateChanged) {
            frame = 0;
            loops = 0;
            stateChanged = false;
            if (audio.containsKey(state)) {
                try {
                    MediaPlayer mediaPlayer = MediaPlayer.create(context, audio.get(state));
                    mediaPlayer.start();
                    mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                        @Override
                        public void onCompletion(MediaPlayer mp) {
                            mp.release();
                        }
                    });
                } catch (Exception e) {
                    // just let things go on
                }
            }
            if (stateTransitionEvents.containsKey(state)) {
                EventQueue.getInstance().publish(stateTransitionEvents.get(state), this);
            }
        }
        int oldframe = frame;
        super.doStep();
        if (oldframe!=0 && frame==0) {
            loops++;
        }
        if (stateTransitions.containsKey(state) && loops >= stateTransitions.get(state)) {
            nextState();
        }
    }

    public boolean inSprite(float tx, float ty) {
        if (tx>=x*scale && tx<=(x+width)*scale && ty>=y*scale && ty<=(y+height)*scale) {
            if (!stateTransitions.containsKey(state) || stateTransitions.get(state)==TRANSITION_CLICK) {
                if (ignoreAlpha) {
                    return true;
                }
                if (touchRectangles.get(state)!=null) {
                    int px = (int)(tx-x*scale);
                    int py = (int)(ty-y*scale);
                    if (touchRectangles.get(state).contains(px, py)) {
                        return true;
                    }
                }
                if (bitmaps!=null) {
                    List<Bitmap> frames = bitmaps.get(state);
                    Bitmap bitmap = frames.get(frame);
                    int px = (int)(tx-x*scale);
                    int py = (int)(ty-y*scale);
                    if (px<bitmap.getWidth() && py < bitmap.getHeight()) {
                        int color = bitmap.getPixel(px, py);
                        int alpha = Color.alpha(color);
                        if (alpha > 50) {
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }

    @Override
    public void doDraw(Canvas canvas) {
        super.doDraw(canvas);

        /*
        if (touchRectangles.get(state)!=null) {
            Paint p  = new Paint();
            p.setColor(Color.BLUE);
            Rect rect = new Rect();
            Rect tr = touchRectangles.get(state);
            rect.set((int)(x+tr.left), (int)(y+tr.top), (int)(x+tr.right), (int) (y+tr.bottom));
            canvas.drawRect(rect, p);
        }
        */
    }

    @Override
    public boolean onMove(float oldX, float oldY, float newX, float newY) {
        super.onMove(oldX, oldY, newX, newY);
        moved = true;
        return false;
    }

    @Override
    public void onRelease(float x, float y) {
        super.onRelease(x, y);
        if (!moved) {
            if (!stateTransitions.containsKey(state) || stateTransitions.get(state) == TRANSITION_CLICK) {
                if (surfaceView != null) {
                    surfaceView.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP, HapticFeedbackConstants.FLAG_IGNORE_VIEW_SETTING);
                }
                nextState();
            }
        }
        moved = false;
    }

    public void nextState() {
        int nextState = state+1;
        if (nextState >= bitmaps.size() && bitmapIds.get(nextState)==null) nextState = 0;
        if (bitmaps.get(nextState)==null && bitmapIds.get(nextState)!=null && resources!=null) {
            List<Bitmap> loaded = new ArrayList<>(bitmapIds.get(nextState).size());
            for (Integer rid : bitmapIds.get(nextState)) {
                loaded.add(BitmapFactory.decodeResource(getResources(), rid));
            }
            bitmaps.put(nextState, loaded);
        }
        state = nextState;
        stateChanged = true;
    }
}
