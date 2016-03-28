package com.yellowforktech.littlefamilytree.views;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

/**
 * Created by kids on 4/30/15.
 */
public abstract class AbstractTouchAnimatedSurfaceView extends SurfaceView implements SurfaceHolder.Callback {
    public static final long FPS = 30L;
    protected Context context;
    protected AnimationThread animationThread;
    protected long animationDelay = 1000/FPS;
    protected float touchTolerance = 8;
    protected long onTouchStartTime;

    public AbstractTouchAnimatedSurfaceView(Context context) {
        super(context);
        SurfaceHolder holder = getHolder();
        holder.addCallback(this);
        this.context = context;
    }

    public AbstractTouchAnimatedSurfaceView(Context context, AttributeSet attrs) {
        super(context, attrs);
        SurfaceHolder holder = getHolder();
        holder.addCallback(this);
        this.context = context;
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        if (animationThread==null || !animationThread.isAlive()) {
            animationThread = new AnimationThread(holder, context, this);
            animationThread.setRunning(true);
            animationThread.start();
        }
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        animationThread.setRunning(false);
        boolean retry = true;
        while(retry)  {
            try {
                animationThread.join();
                retry = false;
            } catch(Exception e) {
                Log.v("Exception Occured", e.getMessage());
            }
        }
    }

    public void pause() {
        stop();
    }

    public void resume() {
        if (animationThread==null || !animationThread.isAlive()) {
            animationThread = new AnimationThread(getHolder(), context, this);
            animationThread.setRunning(true);
            animationThread.start();
        }
    }

    protected float mX, mY;

    protected void touch_start(float x, float y) {
        onTouchStartTime = System.currentTimeMillis();
        mX = x;
        mY = y;
    }
    protected void touch_move(float x, float y) {
        float dx = Math.abs(x - mX);
        float dy = Math.abs(y - mY);
        if (dx >= touchTolerance || dy >= touchTolerance) {
            doMove(mX, mY, x, y);
            mX = x;
            mY = y;
        }
    }

    protected void touch_up(float x, float y) {

    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float x = event.getX();
        float y = event.getY();

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                touch_start(x, y);
                break;
            case MotionEvent.ACTION_MOVE:
                touch_move(x, y);
                break;
            case MotionEvent.ACTION_UP:
                touch_up(x, y);
                break;
        }
        return true;
    }

    public float getTouchTolerance() {
        return touchTolerance;
    }

    public void setTouchTolerance(float touchTolerance) {
        this.touchTolerance = touchTolerance;
    }

    public long getAnimationDelay() {
        return animationDelay;
    }

    public void setAnimationDelay(long animationDelay) {
        this.animationDelay = animationDelay;
    }

    public abstract void doStep();

    public abstract void doDraw(Canvas canvas);

    public abstract void doMove(float oldX, float oldY, float newX, float newY);

    public void stop() {
        if (animationThread!=null) {
            animationThread.setRunning(false);
            boolean retry = true;
            while (retry) {
                try {
                    animationThread.join();
                    retry = false;
                } catch (Exception e) {
                    Log.v("Exception Occured", e.getMessage());
                }
            }
        }
    }

    public class AnimationThread extends Thread {
        private SurfaceHolder holder;
        private Context context;
        private AbstractTouchAnimatedSurfaceView view;

        private boolean running;

        public AnimationThread(SurfaceHolder holder, Context context, AbstractTouchAnimatedSurfaceView view) {
            this.holder = holder;
            this.view = view;
            this.context = context;
        }

        public void setRunning(boolean r) {
            this.running = r;
        }

        @Override
        public void run() {
            super.run();

            while(running) {
                long starttime = System.currentTimeMillis();
                view.doStep();
                Canvas canvas = holder.lockCanvas();
                try {
                    synchronized (view.getHolder()) {
                        if (canvas != null) {
                            view.doDraw(canvas);
                        }
                    }
                } finally {
                    if (canvas!=null) {
                        try {
                            holder.unlockCanvasAndPost(canvas);
                        } catch (Exception e) {
                            Log.e("AnimationThread", "Error unlocking canvas", e);
                        }
                    }
                }
                long calctime = System.currentTimeMillis() - starttime;
                long sleeptime = animationDelay - calctime;

                if (sleeptime <= 0) {
                    int skippedFrames = (int) (calctime / animationDelay);
                    if (skippedFrames > 1) {
                        Log.v(this.getClass().getSimpleName(), "Slow calculations missed " + skippedFrames + " frames.");
                        int catchUp = (int) (skippedFrames);
                        for (int c = 0; c < catchUp-1; c++) {
                            view.doStep();
                        }
                    }
                } else {
                    try {
                        sleep(sleeptime);
                    } catch (Exception e) {
                        Log.d("Exception Occured", e.getMessage(), e);
                    }
                }
            }
        }
    }
}
