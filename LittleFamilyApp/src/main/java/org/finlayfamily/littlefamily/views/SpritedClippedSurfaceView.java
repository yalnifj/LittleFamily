package org.finlayfamily.littlefamily.views;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.util.AttributeSet;

import org.finlayfamily.littlefamily.sprites.Sprite;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Created by jfinlay on 5/8/2015.
 */
public class SpritedClippedSurfaceView extends AbstractTouchAnimatedSurfaceView {
    protected List<Sprite> sprites;
    protected Sprite backgroundSprite;
    protected Paint basePaint;
    protected List<Sprite> selectedSprites;

    protected int clipX;
    protected int clipY;
    protected int maxWidth;
    protected int maxHeight;

    public SpritedClippedSurfaceView(Context context) {
        super(context);
        sprites = new ArrayList<>();
        selectedSprites = new ArrayList<>();
        basePaint = new Paint();
        basePaint.setStyle(Paint.Style.FILL);
        basePaint.setColor(Color.WHITE);
    }

    public SpritedClippedSurfaceView(Context context, AttributeSet attrs) {
        super(context, attrs);
        sprites = new ArrayList<>();
        selectedSprites = new ArrayList<>();
        basePaint = new Paint();
    }

    public List<Sprite> getSprites() {
        return sprites;
    }

    public void setSprites(List<Sprite> sprites) {
        this.sprites = sprites;
    }

    public Sprite getBackgroundSprite() {
        return backgroundSprite;
    }

    public void setBackgroundSprite(Sprite backgroundSprite) {
        this.backgroundSprite = backgroundSprite;
    }

    public void addSprite(Sprite s) {
        synchronized (sprites) {
            sprites.add(s);
        }
    }

    public void removeSprite(Sprite s) {
        synchronized (sprites) {
            sprites.remove(s);
        }
    }

    public int getMaxHeight() {
        return maxHeight;
    }

    public void setMaxHeight(int maxHeight) {
        this.maxHeight = maxHeight;
    }

    public int getMaxWidth() {
        return maxWidth;
    }

    public void setMaxWidth(int maxWidth) {
        this.maxWidth = maxWidth;
    }

    public int getClipX() {
        return clipX;
    }

    public void setClipX(int clipX) {
        this.clipX = clipX;
    }

    public int getClipY() {
        return clipY;
    }

    public void setClipY(int clipY) {
        this.clipY = clipY;
    }

    @Override
    public void doStep() {
        synchronized (sprites) {
            Iterator<Sprite> i = sprites.iterator();
            while (i.hasNext()) {
                Sprite s = i.next();
                s.doStep();
                if (s.isRemoveMe()) i.remove();
            }
        }
    }

    @Override
    public void doDraw(Canvas canvas) {
        canvas.drawRect(0,0,getWidth(),getHeight(), basePaint);
        if (backgroundSprite!=null) {
            backgroundSprite.setWidth(this.getWidth());
            backgroundSprite.setHeight(this.getHeight());
            backgroundSprite.doDraw(canvas);
        }

        canvas.translate(-clipX, -clipY);
        synchronized (sprites) {
            for (Sprite s : sprites) {
                if (s.getX() + s.getWidth() >= clipX && s.getX() <= getWidth() + clipX && s.getY() + s.getHeight() >= clipY && s.getY() <= getHeight() + clipY) {
                    Matrix m = s.getMatrix();
                    Matrix old = null;
                    if (m != null) {
                        old = new Matrix();
                        old.set(m);
                        m.postTranslate(-clipX, -clipY);
                    }
                    s.doDraw(canvas);
                    if (m != null) {
                        m.set(old);
                    }
                }
            }
        }
    }

    @Override
    public void doMove(float oldX, float oldY, float newX, float newY) {
        boolean selectedMoved = false;
        if (selectedSprites.size() > 0) {
            for (Sprite s : selectedSprites) {
                selectedMoved |= s.onMove(oldX+clipX, oldY+clipY, newX+clipX, newY+clipY);
            }
        }
        if (!selectedMoved) {
            backgroundSprite.onMove(oldX, oldY, newX, newY);
            clipX -= (newX-oldX);
            clipY -= (newY-oldY);

            if (clipX < 0) clipX = 0;
            if (clipX + getWidth() > maxWidth) clipX = maxWidth - getWidth();

            if (clipY < 0) clipY = 0;
            if (clipY + getHeight() > maxHeight) clipY = maxHeight - getHeight();
        }
    }

    @Override
    protected void touch_start(float x, float y) {
        super.touch_start(x, y);

        synchronized (sprites) {
            for (Sprite s : sprites) {
                if (s.inSprite(x + clipX, y + clipY)) {
                    selectedSprites.add(s);
                    s.onSelect(x + clipX, y + clipY);
                }
            }
        }
    }

    @Override
    protected void touch_up(float x, float y) {
        super.touch_up(x, y);

        for(Sprite s : selectedSprites) {
            s.onRelease(x+clipX, y+clipY);
        }
        selectedSprites.clear();
    }

    public void onDestroy() {
        synchronized (sprites) {
            List<Sprite> spriteList = sprites;
            sprites = null;
            for (Sprite s : spriteList) {
                s.onDestroy();
            }
            spriteList.clear();
        }
    }
}
