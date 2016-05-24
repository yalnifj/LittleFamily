package com.yellowforktech.littlefamilytree.sprites;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by jfinlay on 5/18/2016.
 */
public class SpriteAnimator {

    private List<SpriteAnimatorTiming> timings = new ArrayList<>();
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

    public class SpriteAnimatorTiming implements Comparable<SpriteAnimatorTiming> {
        Sprite sprite;
        int state;
        long time;

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

        @Override
        public int compareTo(SpriteAnimatorTiming another) {
            return (int) (time - another.time);
        }
    }
}
