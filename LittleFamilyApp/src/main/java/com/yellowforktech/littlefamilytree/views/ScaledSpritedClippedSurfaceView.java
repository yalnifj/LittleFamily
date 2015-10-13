package com.yellowforktech.littlefamilytree.views;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.util.AttributeSet;

import com.yellowforktech.littlefamilytree.sprites.Sprite;

/**
 * Created by Parents on 10/12/2015.
 */
public class ScaledSpritedClippedSurfaceView extends SpritedClippedSurfaceView {

    protected float scale = 1.0f;
    protected boolean moved;

    public float getScale() {
        return scale;
    }

    public void setScale(float scale) {
        this.scale = scale;
    }

    public ScaledSpritedClippedSurfaceView(Context context) {
        super(context);
    }

    public ScaledSpritedClippedSurfaceView(Context context, AttributeSet attrs) {
        super(context, attrs);
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
            //-- keep selected sprite on top
            for (Sprite s : selectedSprites) {
                sprites.remove(s);
                sprites.add(s);
            }
        }
    }

    @Override
    protected void touch_up(float x, float y) {
        for (Sprite s : selectedSprites) {
            s.onRelease(x + clipX * scale, y + clipY * scale);
        }
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
    public void doDraw(Canvas canvas) {
        canvas.drawRect(0, 0, getWidth(), getHeight(), basePaint);
        canvas.scale(scale, scale);
        if (backgroundSprite!=null) {
            backgroundSprite.setScale(scale);
            backgroundSprite.setWidth(this.getWidth());
            backgroundSprite.setHeight(this.getHeight());
            backgroundSprite.doDraw(canvas);
        }

        canvas.translate(-clipX*scale, -clipY*scale);
        synchronized (sprites) {
            for (Sprite s : sprites) {
                s.setScale(scale);
                if ((s.getX() + s.getWidth()) * scale >= clipX * scale && s.getX() * scale <= getWidth() + clipX * scale
                        && (s.getY() + s.getHeight()) * scale >= clipY * scale && s.getY() * scale <= getHeight() + clipY * scale) {
                    Matrix m = s.getMatrix();
                    Matrix old = null;
                    if (m != null) {
                        old = new Matrix();
                        old.set(m);
                        m.postTranslate(scale, scale);
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
