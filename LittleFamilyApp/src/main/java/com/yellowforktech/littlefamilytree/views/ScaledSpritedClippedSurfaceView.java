package com.yellowforktech.littlefamilytree.views;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;

import com.yellowforktech.littlefamilytree.sprites.Sprite;

/**
 * Created by Parents on 10/12/2015.
 */
public class ScaledSpritedClippedSurfaceView extends SpritedClippedSurfaceView {
    private ScaleGestureDetector mScaleDetector;
    protected float scale = 1.0f;
    protected float maxScale = 1.5f;
    protected float minScale = 0.25f;
    protected boolean moved;
    protected DisplayMetrics dm;

    public float getScale() {
        return scale;
    }

    public void setScale(float scale) {
        this.scale = scale;
    }

    public float getMaxScale() {
        return maxScale;
    }

    public void setMaxScale(float maxScale) {
        this.maxScale = maxScale;
    }

    public float getMinScale() {
        return minScale;
    }

    public void setMinScale(float minScale) {
        this.minScale = minScale;
    }

    public ScaledSpritedClippedSurfaceView(Context context) {
        super(context);
        dm = context.getResources().getDisplayMetrics();
        setupScaleGestureDetector();
    }

    public ScaledSpritedClippedSurfaceView(Context context, AttributeSet attrs) {
        super(context, attrs);
        dm = context.getResources().getDisplayMetrics();
        setupScaleGestureDetector();
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
                scale -= old - detector.getScaleFactor();
                old = detector.getScaleFactor();
                if (scale > maxScale) scale = maxScale;
                if (scale < minScale) scale = minScale;
                return false;
            }
        });
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        mScaleDetector.onTouchEvent(event);
        return super.onTouchEvent(event);
    }

    @Override
    protected void touch_start(float x, float y) {
        mX = x;
        mY = y;

        synchronized (sprites) {
            for (Sprite s : sprites) {
                if (s.inSprite(x + clipX * scale, y + clipY * scale)) {
                    selectedSprites.add(s);
                    s.onSelect(x + clipX * scale, y + clipY * scale);
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
    }

    @Override
    public void doMove(float oldX, float oldY, float newX, float newY) {
        boolean selectedMoved = false;
        if (selectedSprites.size() > 0) {
            for (Sprite s : selectedSprites) {
                selectedMoved |= s.onMove(oldX+clipX*scale, oldY+clipY*scale, newX+clipX*scale, newY+clipY*scale);
            }
        }
        if (!selectedMoved) {
            backgroundSprite.onMove(oldX, oldY, newX, newY);
            clipX -= (newX-oldX);
            clipY -= (newY-oldY);
            if (Math.abs(clipX) > 6*dm.density || Math.abs(clipY) > 6*dm.density ) {
                moved = true;
            }

            if (clipX < 0) clipX = 0;
            if (clipX*scale + getWidth() >= maxWidth*scale) {
                clipX = (int) (maxWidth - getWidth()/scale);
            }

            if (clipY < 0) clipY = 0;
            if (clipY*scale + getHeight() >= maxHeight*scale) {
                clipY = (int) (maxHeight - getHeight()/scale);
            }
        }
    }

    @Override
    public void doDraw(Canvas canvas) {
        canvas.drawRect(0, 0, getWidth(), getHeight(), basePaint);
        if (backgroundSprite!=null) {
            backgroundSprite.setScale(scale);
            backgroundSprite.setWidth(this.getWidth());
            backgroundSprite.setHeight(this.getHeight());
            backgroundSprite.doDraw(canvas);
        }

        canvas.scale(scale, scale);
        canvas.translate(-clipX, -clipY);
        synchronized (sprites) {
            for (Sprite s : sprites) {
                s.setScale(scale);
                if ((s.getX() + s.getWidth()) * scale >= clipX*scale && s.getX() * scale <= getWidth() + clipX*scale
                        && (s.getY() + s.getHeight()) * scale >= clipY * scale && s.getY() * scale <= getHeight() + clipY*scale) {
						Matrix m = s.getMatrix();
						Matrix old = null;
						if (m != null) {
							old = new Matrix();
							old.set(m);
							m.postTranslate(-clipX*scale, -clipY*scale);
						}
						s.doDraw(canvas);
						if (m != null) {
							m.set(old);
						}
                }
            }
        }
    }
}
