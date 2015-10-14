package com.yellowforktech.littlefamilytree.views;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.util.AttributeSet;

import com.yellowforktech.littlefamilytree.activities.LittleFamilyActivity;
import com.yellowforktech.littlefamilytree.sprites.Sprite;
import com.yellowforktech.littlefamilytree.sprites.TouchEventGameSprite;

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
    private List<Sprite> activitySprites;
    private Random random;
    private int state = 0;
    private Sprite activeSprite = null;
    private boolean scaleSet = false;

    public HomeView(Context context) {
        super(context);
        activitySprites = new ArrayList<>();
        random = new Random();
        timer = 0L;
        lockBitmap = BitmapFactory.decodeResource(context.getResources(), android.R.drawable.ic_menu_manage);
        lockSprite = new TouchEventGameSprite(lockBitmap, LittleFamilyActivity.TOPIC_START_SETTINGS);
    }

    public HomeView(Context context, AttributeSet attrs) {
        super(context, attrs);
        activitySprites = new ArrayList<>();
        random = new Random();
        timer = 0L;
        lockBitmap = BitmapFactory.decodeResource(context.getResources(), android.R.drawable.ic_menu_manage);
        lockSprite = new TouchEventGameSprite(lockBitmap, LittleFamilyActivity.TOPIC_START_SETTINGS);
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
                int activity = random.nextInt(activitySprites.size());
                while(activitySprites.get(activity)==activeSprite) {
                    activity = random.nextInt(activitySprites.size());
                }
                activeSprite = activitySprites.get(activity);
                int starCount = 4 + random.nextInt(2+activeSprite.getHeight()/starBitmap.getHeight());
				Rect r = new Rect();
				r.set((int)activeSprite.getX(), (int)activeSprite.getY(), (int)(activeSprite.getX()+activeSprite.getWidth()),
						(int)(activeSprite.getY()+activeSprite.getHeight()));
				addStars(r, true, starCount);
            }
            timer--;
        }
        if (state==1) {
                if (starCount == 0) {
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
        lockSprite.setX(getWidth() - lockSprite.getWidth() * 1.5f);
        lockSprite.setY(getHeight() - lockSprite.getHeight() * 1.5f);
        lockSprite.doDraw(canvas);
		
		profileSprite.setX(lockSprite.getX() - profileSprite.getWidth() * 1.2f);
        profileSprite.setY(lockSprite.getY());
        profileSprite.doDraw(canvas);
    }

    @Override
    protected void touch_start(float x, float y) {
        super.touch_start(x, y);

        if (lockSprite.inSprite(x, y)) {
            lockSprite.onSelect(x, y);
        }
		if (profileSprite.inSprite(x, y)) {
            profileSprite.onSelect(x, y);
        }
    }

    @Override
    protected void touch_up(float x, float y) {
        super.touch_up(x, y);

        if (lockSprite.inSprite(x, y)) {
            lockSprite.onRelease(x, y);
        }
		if (profileSprite.inSprite(x, y)) {
            profileSprite.onRelease(x, y);
        }

        lockSprite.setSelected(false);
		profileSprite.setSelected(false);
    }

}
