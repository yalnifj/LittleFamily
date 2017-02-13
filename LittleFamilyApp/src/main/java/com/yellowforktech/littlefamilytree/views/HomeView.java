package com.yellowforktech.littlefamilytree.views;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.DisplayMetrics;

import com.yellowforktech.littlefamilytree.R;
import com.yellowforktech.littlefamilytree.activities.LittleFamilyActivity;
import com.yellowforktech.littlefamilytree.sprites.Sprite;
import com.yellowforktech.littlefamilytree.sprites.TouchEventGameSprite;
import com.yellowforktech.littlefamilytree.util.ImageHelper;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Created by kids on 7/7/15.
 */
public class HomeView extends ScaledSpritedClippedSurfaceView {
    private Bitmap lockBitmap;
    private TouchEventGameSprite lockSprite;
	private TouchEventGameSprite profileSprite;
    private long timer;
    private long ptimer;
    private List<Sprite> activitySprites;
    private List<Sprite> premiumSprites;
    private Random random;
    private int state = 0;
    private Sprite activeSprite = null;
    private boolean scaleSet = false;

    public HomeView(Context context) {
        super(context);
        activitySprites = new ArrayList<>();
        premiumSprites = new ArrayList<>();
        random = new Random();
        timer = 0L;
        ptimer = 0L;
        DisplayMetrics dm = getContext().getResources().getDisplayMetrics();
        lockBitmap = ImageHelper.loadBitmapFromResource(context, R.drawable.settings, 0, (int) (30 * dm.density), (int) (30 * dm.density));
        lockSprite = new TouchEventGameSprite(lockBitmap, LittleFamilyActivity.TOPIC_START_SETTINGS, dm);
    }

    public HomeView(Context context, AttributeSet attrs) {
        super(context, attrs);
        activitySprites = new ArrayList<>();
        premiumSprites = new ArrayList<>();
        random = new Random();
        timer = 0L;
        ptimer = 0L;
        DisplayMetrics dm = getContext().getResources().getDisplayMetrics();
        lockBitmap = ImageHelper.loadBitmapFromResource(context, R.drawable.settings, 0, (int)(30*dm.density), (int)(30*dm.density));
        lockSprite = new TouchEventGameSprite(lockBitmap, LittleFamilyActivity.TOPIC_START_SETTINGS, dm);
        lockSprite.setIgnoreAlpha(true);
    }

	public void setProfileSprite(TouchEventGameSprite profileSprite)
	{
		this.profileSprite = profileSprite;
	}

	public TouchEventGameSprite getProfileSprite()
	{
		return profileSprite;
	}

    public void addActivitySprite(Sprite s) {
        activitySprites.add(s);
    }

    public void addPremiumSprite(Sprite s) {
        premiumSprites.add(s);
    }

    public boolean isScaleSet() {
        return scaleSet;
    }

    public void setScaleSet(boolean scaleSet) {
        this.scaleSet = scaleSet;
    }

    @Override
    public void doStep() {
        super.doStep();
        if (state==0) {
            if (timer <= 0) {
                state = 1;
                timer = random.nextInt(50);
                boolean yellow = random.nextBoolean();
                if (yellow) {
                    int activity = random.nextInt(activitySprites.size());
                    while (activitySprites.get(activity) == activeSprite) {
                        activity = random.nextInt(activitySprites.size());
                    }
                    activeSprite = activitySprites.get(activity);
                    int starCount = 4 + random.nextInt(2 + activeSprite.getHeight() / starBitmap.getHeight());
                    Rect r = new Rect();
                    r.set((int) activeSprite.getX(), (int) activeSprite.getY(), (int) (activeSprite.getX() + activeSprite.getWidth()),
                            (int) (activeSprite.getY() + activeSprite.getHeight()));
                    addStars(r, true, starCount);
                } else {
                    if (redStarBitmap!=null) {
                        int activity = random.nextInt(premiumSprites.size());
                        while (premiumSprites.get(activity) == activeSprite) {
                            activity = random.nextInt(premiumSprites.size());
                        }
                        activeSprite = premiumSprites.get(activity);
                        int starCount = 4 + random.nextInt(2 + activeSprite.getHeight() / redStarBitmap.getHeight());
                        Rect r = new Rect();
                        r.set((int) activeSprite.getX(), (int) activeSprite.getY(), (int) (activeSprite.getX() + activeSprite.getWidth()),
                                (int) (activeSprite.getY() + activeSprite.getHeight()));
                        addRedStars(r, true, starCount);
                    }
                }
            }
            timer--;
        }
        if (state==1) {
            if (starCount == 0) {
                state = 0;
            }
            if (redStarCount == 0) {
                state = 0;
            }
        }
    }

    @Override
    public void doDraw(Canvas canvas) {

        if (!scaleSet) {
            minScale = (float) getHeight() / (float) maxHeight;
            if (getHeight() > maxHeight) {
                scale = minScale;
            }
            scaleSet = true;
        }

        super.doDraw(canvas);

        canvas.setMatrix(new Matrix());
        canvas.translate(0, 0);
        if (lockSprite!=null) {
            lockSprite.setX(getWidth() - lockSprite.getWidth() * 1.5f);
            lockSprite.setY(getHeight() - lockSprite.getHeight() * 1.5f);
            lockSprite.doDraw(canvas);
        }

        if (profileSprite!=null) {
            profileSprite.setX(lockSprite.getX() - profileSprite.getWidth() * 1.2f);
            profileSprite.setY(lockSprite.getY());
            profileSprite.doDraw(canvas);
        }
    }

    @Override
    protected void touch_start(float x, float y) {
        super.touch_start(x, y);

        if (lockSprite!=null && lockSprite.inSprite(x, y)) {
            lockSprite.onSelect(x, y);
        }
		if (profileSprite!=null && profileSprite.inSprite(x, y)) {
            profileSprite.onSelect(x, y);
        }
    }

    @Override
    protected void touch_up(float x, float y) {
        super.touch_up(x, y);

        if (lockSprite!=null) {
            if (lockSprite.inSprite(x, y)) {
                lockSprite.onRelease(x, y);
            }
            lockSprite.setSelected(false);
        }
		if (profileSprite!=null) {
            if (profileSprite.inSprite(x, y)) {
                profileSprite.onRelease(x, y);
            }
            profileSprite.setSelected(false);
        }
    }

}
