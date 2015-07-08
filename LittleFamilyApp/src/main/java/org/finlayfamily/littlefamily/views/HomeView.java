package org.finlayfamily.littlefamily.views;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.util.AttributeSet;

import org.finlayfamily.littlefamily.activities.LittleFamilyActivity;
import org.finlayfamily.littlefamily.sprites.Sprite;
import org.finlayfamily.littlefamily.sprites.StarSprite;
import org.finlayfamily.littlefamily.sprites.TouchEventGameSprite;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Created by kids on 7/7/15.
 */
public class HomeView extends SpritedClippedSurfaceView {

    private Bitmap starBitmap;
    private Bitmap lockBitmap;
    private Sprite lockSprite;
    private long timer;
    private List<Sprite> activitySprites;
    private Random random;
    private int state = 0;
    private int starCount = 0;
    private Sprite activeSprite = null;
    private int starDelay = 3;

    public HomeView(Context context) {
        super(context);
        activitySprites = new ArrayList<>();
        random = new Random();
        timer = 0L;
        lockBitmap = BitmapFactory.decodeResource(context.getResources(), android.R.drawable.ic_lock_idle_lock);
        lockSprite = new TouchEventGameSprite(lockBitmap, LittleFamilyActivity.TOPIC_START_SETTINGS);
    }

    public HomeView(Context context, AttributeSet attrs) {
        super(context, attrs);
        activitySprites = new ArrayList<>();
        random = new Random();
        timer = 0L;
        lockBitmap = BitmapFactory.decodeResource(context.getResources(), android.R.drawable.ic_lock_idle_lock);
        lockSprite = new TouchEventGameSprite(lockBitmap, LittleFamilyActivity.TOPIC_START_SETTINGS);
    }

    public Bitmap getStarBitmap() {
        return starBitmap;
    }

    public void setStarBitmap(Bitmap starBitmap) {
        this.starBitmap = starBitmap;
    }

    public void addActivitySprite(Sprite s) {
        activitySprites.add(s);
    }

    @Override
    public void doStep() {
        super.doStep();
        if (state==0) {
            if (timer <= 0) {
                state = 1;
                timer = random.nextInt(50);
                int activity = random.nextInt(activitySprites.size());
                while(activitySprites.get(activity)==activeSprite) {
                    activity = random.nextInt(activitySprites.size());
                }
                activeSprite = activitySprites.get(activity);
                starCount = 4 + random.nextInt(2+activeSprite.getHeight()/starBitmap.getHeight());
            }
            timer--;
        }
        if (state==1) {
            if (starDelay == 0) {
                starDelay = 4;
                if (starCount == 0) {
                    state = 0;
                } else {
                    starCount--;
                    if (activeSprite != null) {
                        StarSprite star = new StarSprite(starBitmap, true, true);
                        int x = (int) (activeSprite.getX() + random.nextInt(activeSprite.getWidth()));
                        int y = (int) (activeSprite.getY() + random.nextInt(activeSprite.getHeight()));
                        star.setX(x);
                        star.setY(y);
                        addSprite(star);
                    }
                }
            }
            starDelay--;
        }
    }

    @Override
    public void doDraw(Canvas canvas) {
        super.doDraw(canvas);

        canvas.setMatrix(new Matrix());
        canvas.translate(0,0);
        lockSprite.setX(getWidth() - lockSprite.getWidth() * 1.5f);
        lockSprite.setY(getHeight() - lockSprite.getHeight() * 1.5f);
        lockSprite.doDraw(canvas);
    }

    @Override
    protected void touch_up(float x, float y) {
        super.touch_up(x, y);

        if (lockSprite.inSprite(x, y)) {
            lockSprite.onRelease(x, y);
        }
    }
}
