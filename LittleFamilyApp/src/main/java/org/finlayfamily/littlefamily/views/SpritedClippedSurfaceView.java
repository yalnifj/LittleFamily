package org.finlayfamily.littlefamily.views;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
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


    public SpritedClippedSurfaceView(Context context) {
        super(context);
        sprites = new ArrayList<>();
        selectedSprites = new ArrayList<>();
        basePaint = new Paint();
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
        sprites.add(s);
    }

    public void removeSprite(Sprite s) {
        sprites.remove(s);
    }

    @Override
    public void doStep() {
        Iterator<Sprite> i = sprites.iterator();
        while(i.hasNext()){
            Sprite s = i.next();
            s.doStep();
            if (s.isRemoveMe()) i.remove();
        }
    }

    @Override
    public void doDraw(Canvas canvas) {
        if (backgroundSprite!=null) {
            backgroundSprite.doDraw(canvas);
        } else {
            basePaint.setColor(Color.WHITE);
            canvas.drawRect(0,0,getWidth(),getHeight(),basePaint);
        }

        for(Sprite s : sprites) {
            if (s.getX()+s.getWidth()>=0 && s.getX()<=getWidth() && s.getY()+s.getHeight()>=0 && s.getY()<=getHeight()) {
                s.doDraw(canvas);
            }
        }
    }

    @Override
    public void doMove(float oldX, float oldY, float newX, float newY) {
        if (selectedSprites.size() > 0) {
            for (Sprite s : selectedSprites) {
                s.onMove(oldX, oldY, newX, newY);
            }
        } else {
            backgroundSprite.onMove(oldX, oldY, newX, newY);
        }
    }

    @Override
    protected void touch_start(float x, float y) {
        super.touch_start(x, y);

        for(Sprite s : sprites) {
            if (s.inSprite(x, y)) {
                selectedSprites.add(s);
                s.onSelect(x, y);
            }
        }
    }

    @Override
    protected void touch_up(float x, float y) {
        super.touch_up(x, y);

        for(Sprite s : selectedSprites) {
            s.onRelease(x, y);
        }
        selectedSprites.clear();
    }
}
