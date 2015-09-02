package com.yellowforktech.littlefamilytree.sprites;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.media.MediaPlayer;
import android.util.Log;

import com.yellowforktech.littlefamilytree.R;
import com.yellowforktech.littlefamilytree.events.EventQueue;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Created by jfinlay on 8/28/2015.
 */
public class BirdSprite extends AnimatedBitmapSprite {
    protected  Matrix flipped;
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

        flipped = new Matrix();

        List<Integer> state1 = new ArrayList<>(5);
        state1.add(R.drawable.house_tree_bird);
        state1.add(R.drawable.house_tree_bird1);
        state1.add(R.drawable.house_tree_bird2);
        state1.add(R.drawable.house_tree_bird1);
        state1.add(R.drawable.house_tree_bird);
        bitmapIds.put(1, state1);

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
                    flipped.postScale(-1, 1);
                    flipped.postTranslate(getWidth() + x * 2, 0);
                    this.matrix = flipped;
                } else {
                    matrix = null;
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
                tweet = MediaPlayer.create(context, R.raw.bird);
                tweet.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                    @Override
                    public void onCompletion(MediaPlayer mp) {
                        mp.release();
                    }
                });
                tweet.start();
            } catch (Exception e) {
                // just let things go on
                Log.e(getClass().getSimpleName(), "Error playing audio", e);
            }
        }
        if (state==2) {
            if (frame>=bitmaps.get(state).size()-1) {
                EventQueue.getInstance().publish(eventTopic, this);
                state = 0;
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
