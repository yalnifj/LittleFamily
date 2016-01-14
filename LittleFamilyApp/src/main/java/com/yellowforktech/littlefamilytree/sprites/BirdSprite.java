package com.yellowforktech.littlefamilytree.sprites;

import android.content.Context;
import android.graphics.Bitmap;
import android.media.MediaPlayer;
import android.preference.PreferenceManager;
import android.util.Log;

import com.yellowforktech.littlefamilytree.R;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Created by jfinlay on 8/28/2015.
 */
public class BirdSprite extends AnimatedBitmapSprite {
    protected int delay;
    protected boolean isFlipped;
    protected Context context;
    protected Random random;
    private boolean moved = false;
    private String eventTopic;
    private MediaPlayer tweet;
    private boolean stateChanged = false;

    public BirdSprite(Bitmap bitmap, Context context, String eventTopic) {
        super(bitmap);
        this.context = context;
        this.eventTopic = eventTopic;

        List<Integer> state2 = new ArrayList<>(5);
        state2.add(R.drawable.house_tree_bird3);
        state2.add(R.drawable.house_tree_bird4);
        state2.add(R.drawable.house_tree_bird5);
        state2.add(R.drawable.house_tree_bird6);
        state2.add(R.drawable.house_tree_bird7);
        bitmapIds.put(2, state2);

        tweet = new MediaPlayer();

        random = new Random();
        delay = random.nextInt(80);
    }

    @Override
    public void doStep() {
        int oldFrame = frame;
        super.doStep();

        if (delay > 0) {
            delay--;
        } else {
            if (state==0) {
                state = 1;

                isFlipped = random.nextBoolean();

                if (isFlipped) {
                    setFlipHoriz(true);
                } else {
                    setFlipHoriz(false);
                }
            } else {
                if (frame!=oldFrame && frame == 0) {
                    delay = random.nextInt(80);
                    state = 0;
                }
            }
        }

        if (stateChanged && state==2 && visible) {
            try {
                Boolean quietMode = PreferenceManager.getDefaultSharedPreferences(context).getBoolean("quiet_mode", false);
                if (!quietMode) {
                    if (tweet == null || !tweet.isPlaying()) {
                        tweet = MediaPlayer.create(context, R.raw.bird);
                        tweet.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                            @Override
                            public void onCompletion(MediaPlayer mp) {
                                mp.release();
                                tweet = null;
                            }
                        });
                        tweet.start();
                    }
                }
            } catch (Exception e) {
                // just let things go on
                Log.e(getClass().getSimpleName(), "Error playing audio", e);
            }
        }
        if (state==2) {
            if (bitmaps.get(state)==null || frame>=bitmaps.get(state).size()-1) {
                //EventQueue.getInstance().publish(eventTopic, this);
                state = 0;
                frame = 0;
            }
        }
    }

    @Override
    public void onRelease(float x, float y) {
        if (!moved) {
            state = 2;
            frame = 0;
            stateChanged = true;
        }
        moved = false;
    }
}
