package org.finlayfamily.littlefamily.sprites;

import android.content.Context;
import android.graphics.Bitmap;
import android.media.MediaPlayer;

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


    protected Map<Integer, Integer> audio;
    protected Map<Integer, Integer> stateTransitions;
    protected boolean stateChanged;
    protected Context context;
    protected int loops;

    public TouchStateAnimatedBitmapSprite(Context context) {
        audio = new HashMap<>();
        this.context = context;
        stateTransitions = new HashMap<>();
    }

    public TouchStateAnimatedBitmapSprite(Bitmap bitmap, Context context) {
        super(bitmap);
        audio = new HashMap<>();
        this.context = context;
        stateTransitions = new HashMap<>();
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
        }
        int oldframe = frame;
        super.doStep();
        if (oldframe!=0 && frame==0) {
            loops++;
        }
        if (stateTransitions.containsKey(state) && stateTransitions.get(state)==loops) {
            state++;
            stateChanged = true;
            if (state>=bitmaps.size()) {
                state=0;
            }
        }
    }

    public boolean inSprite(float tx, float ty) {
        if (tx>=x && tx<=x+width && ty>=y && ty<=y+height) {
            if (!stateTransitions.containsKey(state) || stateTransitions.get(state)==TRANSITION_CLICK) {
                state++;
                if (state >= bitmaps.size()) state = 0;
                stateChanged = true;
            }
        }
        return false;
    }
}
