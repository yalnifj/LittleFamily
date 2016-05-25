package com.yellowforktech.littlefamilytree.sprites;

import android.content.Context;
import android.media.MediaPlayer;
import android.preference.PreferenceManager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by jfinlay on 5/18/2016.
 */
public class SpriteAnimator {

    private List<AnimatorTiming> timings = new ArrayList<>();
    private long startmillis;
    private long currentTime;
    private int currentPosition;
    private boolean started = false;
    private boolean finished = false;

    public void start() {
        startmillis = System.currentTimeMillis();
        currentTime = 0;
        currentPosition = 0;
        Collections.sort(timings);
        started = true;
        finished = false;
    }

    public void addTiming(long time, Sprite sprite, int state) {
        SpriteAnimatorTiming timing = new SpriteAnimatorTiming(time, sprite, state);
        timings.add(timing);
    }

    public void addAudioTiming(long time, int audioId, Context context) {
        AudioAnimatorTiming timing = new AudioAnimatorTiming(time, audioId, context);
        timings.add(timing);
    }

    public void doStep() {
        if (started) {
            if (currentPosition >= timings.size()) {
                finished = true;
                started = false;
                return;
            }
            currentTime = System.currentTimeMillis() - startmillis;

            while(currentPosition < timings.size() && timings.get(currentPosition).time < currentTime) {
                timings.get(currentPosition).apply();
                currentPosition++;
            }
        }
    }

    public boolean isFinished() {
        return finished;
    }

    public boolean isStarted() {
        return started;
    }

    public int getCurrentPosition() {
        return currentPosition;
    }

    public long getCurrentTime() {
        return currentTime;
    }

    public abstract class AnimatorTiming implements Comparable<AnimatorTiming> {
        long time;

        public abstract void apply();

        @Override
        public int compareTo(AnimatorTiming another) {
            return (int) (time - another.time);
        }
    }

    public class SpriteAnimatorTiming extends AnimatorTiming {
        Sprite sprite;
        int state;

        public SpriteAnimatorTiming(long time, Sprite sprite, int state) {
            this.sprite = sprite;
            this.state = state;
            this.time = time;
        }

        public void apply() {
            if (state < 0) {
                sprite.setRemoveMe(true);
            } else {
                sprite.setState(state);
            }
        }
    }

    public class AudioAnimatorTiming extends AnimatorTiming {
        int audioId;
        MediaPlayer player;
        Context context;

        public AudioAnimatorTiming(long time, int audioId, Context context) {
            this.time = time;
            this.audioId = audioId;
            this.context = context;
        }

        @Override
        public void apply() {
            Boolean quietMode = PreferenceManager.getDefaultSharedPreferences(context).getBoolean("quiet_mode", false);
            if (!quietMode) {
                player = MediaPlayer.create(context, audioId);
                player.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                    @Override
                    public void onCompletion(MediaPlayer mp) {
                        mp.release();
                    }
                });
                player.start();
            }
        }
    }
}
