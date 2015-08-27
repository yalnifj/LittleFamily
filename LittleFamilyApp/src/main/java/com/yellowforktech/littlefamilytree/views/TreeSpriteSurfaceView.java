package com.yellowforktech.littlefamilytree.views;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;

import com.yellowforktech.littlefamilytree.R;
import com.yellowforktech.littlefamilytree.activities.MyTreeActivity;
import com.yellowforktech.littlefamilytree.sprites.Sprite;
import com.yellowforktech.littlefamilytree.sprites.TouchStateAnimatedBitmapSprite;
import com.yellowforktech.littlefamilytree.sprites.TreePersonAnimatedSprite;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Parents on 6/4/2015.
 */
public class TreeSpriteSurfaceView extends SpritedClippedSurfaceView {
    private ScaleGestureDetector mScaleDetector;
    protected float scale = 0.5f;
    protected boolean moved;

    protected TouchStateAnimatedBitmapSprite searchSprite;

    public TreeSpriteSurfaceView(Context context) {
        super(context);
        setupScaleGestureDetector();
    }

    public TreeSpriteSurfaceView(Context context, AttributeSet attrs) {
        super(context, attrs);
        setupScaleGestureDetector();
    }

    public TouchStateAnimatedBitmapSprite getSearchSprite() {
        return searchSprite;
    }

    private void setupScaleGestureDetector() {
        mScaleDetector = new ScaleGestureDetector(context, new ScaleGestureDetector.OnScaleGestureListener() {
            float old;
            @Override
            public void onScaleEnd(ScaleGestureDetector detector) {
            }
            @Override
            public boolean onScaleBegin(ScaleGestureDetector detector) {
                old = detector.getScaleFactor();
                return true;
            }
            @Override
            public boolean onScale(ScaleGestureDetector detector) {
                //Log.d("TreeSpriteSurfaceView", "zoom ongoing, scale: " + detector.getScaleFactor());
                scale -= old - detector.getScaleFactor();
                old = detector.getScaleFactor();
                if (scale > 1.5f) scale = 1.5f;
                if (scale < 0.25f) scale = 0.25f;
                return false;
            }
        });

        Bitmap searchBm = BitmapFactory.decodeResource(context.getResources(), R.drawable.tree_search);
        searchSprite = new TouchStateAnimatedBitmapSprite(searchBm, context);
        searchSprite.setResources(context.getResources());
        searchSprite.setWidth((int) (searchBm.getWidth() * 0.75f));
        searchSprite.setHeight((int) (searchBm.getHeight() * 0.75f));
        List<Integer> searching = new ArrayList<>(8);
        searching.add(R.drawable.tree_search1);
        searching.add(R.drawable.tree_search2);
        searching.add(R.drawable.tree_search3);
        searching.add(R.drawable.tree_search4);
        searching.add(R.drawable.tree_search5);
        searching.add(R.drawable.tree_search6);
        searching.add(R.drawable.tree_search7);
        searching.add(R.drawable.tree_search8);
        searchSprite.getBitmapIds().put(1, searching);
        searchSprite.setStateTransition(1, TouchStateAnimatedBitmapSprite.TRANSITION_LOOP1);
        List<Integer> searching2 = new ArrayList<>(1);
        searching2.add(R.drawable.tree_search8);
        searchSprite.getBitmapIds().put(2, searching2);
        searchSprite.setStateTransition(2, TouchStateAnimatedBitmapSprite.TRANSITION_CLICK);
        searchSprite.setStateTransitionEvent(2, MyTreeActivity.TOPIC_START_FIND_PERSON);
        searchSprite.setStateTransitionEvent(0, MyTreeActivity.TOPIC_NEXT_CLUE);
        searchSprite.setIgnoreAlpha(true);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float x = event.getX();
        float y = event.getY();

        mScaleDetector.onTouchEvent(event);
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                touch_start(x, y);
                invalidate();
                break;
            case MotionEvent.ACTION_MOVE:
                touch_move(x, y);
                invalidate();
                break;
            case MotionEvent.ACTION_UP:
                touch_up(x, y);
                invalidate();
                break;
        }
        return true;
    }

    @Override
    protected void touch_start(float x, float y) {
        mX = x;
        mY = y;

        if (searchSprite.inSprite(x, y)) {
            searchSprite.onSelect(x, y);
        }
        else {
            synchronized (sprites) {
                for (Sprite s : sprites) {
                    if (s.inSprite(x + clipX * scale, y + clipY * scale)) {
                        selectedSprites.add(s);
                        s.onSelect(x + clipX * scale, y + clipY * scale);
                    }
                }
                //-- keep selected sprite on top
                for (Sprite s : selectedSprites) {
                    sprites.remove(s);
                    sprites.add(s);
                }
            }
        }
    }

    @Override
    protected void touch_up(float x, float y) {
        for (Sprite s : selectedSprites) {
            s.onRelease(x + clipX * scale, y + clipY * scale);
        }
        selectedSprites.clear();
        if (!moved) {
            for (Sprite s : sprites) {
                if (s.getState() == TreePersonAnimatedSprite.STATE_OPEN_LEFT)
                    s.setState(TreePersonAnimatedSprite.STATE_ANIMATING_CLOSED_LEFT);
                if (s.getState() == TreePersonAnimatedSprite.STATE_OPEN_RIGHT)
                    s.setState(TreePersonAnimatedSprite.STATE_ANIMATING_CLOSED_RIGHT);
            }

            if (searchSprite.inSprite(x, y)) {
                searchSprite.onRelease(x, y);
            }
        }

        searchSprite.setSelected(false);
        moved = false;
    }

    @Override
    public void doMove(float oldX, float oldY, float newX, float newY) {
        boolean selectedMoved = false;
        moved = true;
        if (selectedSprites.size() > 0) {
            for (Sprite s : selectedSprites) {
                selectedMoved |= s.onMove(oldX+clipX*scale, oldY+clipY*scale, newX+clipX*scale, newY+clipY*scale);
            }
        }
        if (!selectedMoved) {
            backgroundSprite.onMove(oldX, oldY, newX, newY);
            clipX -= (newX-oldX);
            clipY -= (newY-oldY);

            if (clipX < 0) clipX = 0;
            if (clipX + getWidth() > maxWidth*scale) clipX = (int) (maxWidth*scale - getWidth());

            if (clipY < 0) clipY = 0;
            if (clipY + getHeight() > maxHeight*scale) clipY = (int) (maxHeight*scale - getHeight());
        }
    }

    @Override
    public void doStep() {
        super.doStep();
        searchSprite.doStep();
    }

    @Override
    public void doDraw(Canvas canvas) {
        canvas.scale(1, 1);
        canvas.drawRect(0, 0, getWidth(), getHeight(), basePaint);
        if (backgroundSprite!=null) {
            backgroundSprite.setWidth(this.getWidth());
            backgroundSprite.setHeight(this.getHeight());
            backgroundSprite.doDraw(canvas);
        }

        canvas.translate(-clipX*scale, -clipY*scale);
        canvas.scale(scale, scale);
        synchronized (sprites) {
            for (Sprite s : sprites) {
                s.setScale(scale);
                if ((s.getX() + s.getWidth())*scale >= clipX*scale && s.getX()*scale <= getWidth() + clipX*scale
                        && (s.getY() + s.getHeight())*scale >= clipY*scale && s.getY()*scale <= getHeight()+ clipY*scale ) {
                    Matrix m = s.getMatrix();
                    Matrix old = null;
                    if (m != null) {
                        old = new Matrix();
                        old.set(m);
                        m.postTranslate(scale, scale);
                        m.postTranslate(-clipX, -clipY);
                    }
                    s.doDraw(canvas);
                    if (m != null) {
                        m.set(old);
                    }
                }
            }
        }

        if (searchSprite !=null) {
            canvas.setMatrix(new Matrix());
            canvas.translate(0,0);
            searchSprite.setX(getWidth() - searchSprite.getWidth());
            searchSprite.setY(10);
            searchSprite.doDraw(canvas);
        }
    }
}
