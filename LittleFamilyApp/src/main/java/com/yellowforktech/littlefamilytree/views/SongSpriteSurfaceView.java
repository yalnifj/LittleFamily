package com.yellowforktech.littlefamilytree.views;

import android.graphics.*;

import android.content.Context;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import com.yellowforktech.littlefamilytree.activities.LittleFamilyActivity;
import com.yellowforktech.littlefamilytree.data.LittlePerson;
import com.yellowforktech.littlefamilytree.sprites.TouchStateAnimatedBitmapSprite;
import java.util.List;
import com.yellowforktech.littlefamilytree.R;

/**
 * Created by kids on 6/17/15.
 */
public class SongSpriteSurfaceView extends SpritedSurfaceView {
    private List<LittlePerson> family;
    private DisplayMetrics dm;
    private boolean spritesCreated = false;

    private LittleFamilyActivity activity;

    public SongSpriteSurfaceView(Context context) {
        super(context);
        setup();
    }

    public SongSpriteSurfaceView(Context context, AttributeSet attrs) {
        super(context, attrs);
        setup();
    }

    public void setup() {
        multiSelect = false;
    }

    public LittleFamilyActivity getActivity() {
        return activity;
    }

    public void setActivity(LittleFamilyActivity activity) {
        this.activity = activity;
        dm = new DisplayMetrics();
        activity.getWindowManager().getDefaultDisplay().getMetrics(dm);
    }

    public List<LittlePerson> getFamily() {
        return family;
    }

    public void setFamily(List<LittlePerson> family) {
        this.family = family;
    }

    @Override
    public void doStep() {
        super.doStep();
    }

    public void createSprites() {
        synchronized (sprites) {
            sprites.clear();
        }
		Bitmap pianoBm = BitmapFactory.decodeResource(getResources(), R.drawable.house_music_piano);
		TouchStateAnimatedBitmapSprite piano = new TouchStateAnimatedBitmapSprite(pianoBm, activity);
		piano.setX(getWidth()/2 - pianoBm.getWidth()*dm.density/2);
		piano.setY(getHeight() - pianoBm.getHeight()*dm.density);
		addSprite(piano);
        spritesCreated = true;
    }

    @Override
    public void doDraw(Canvas canvas) {
        if (!spritesCreated) {
            createSprites();
        }

        if (backgroundBitmap!=null) {
            Rect rect = new Rect();
            rect.set(0,0,getWidth(),getHeight());
            canvas.drawBitmap(backgroundBitmap, null, rect, basePaint);
        } else {
            basePaint.setColor(Color.WHITE);
            canvas.drawRect(0,0,getWidth(),getHeight(),basePaint);
        }

        super.doDraw(canvas);
    }
}
