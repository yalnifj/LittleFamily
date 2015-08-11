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
import java.util.ArrayList;

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
		piano.setX(getWidth()/2 - pianoBm.getWidth()/2);
		piano.setY(getHeight() - pianoBm.getHeight());
		addSprite(piano);
		
		Bitmap guitarBm = BitmapFactory.decodeResource(getResources(), R.drawable.house_music_guitar);
		TouchStateAnimatedBitmapSprite guitar = new TouchStateAnimatedBitmapSprite(guitarBm, activity);
		guitar.setResources(getResources());
		guitar.setX(getWidth()*0.70f);
		guitar.setY(piano.getY()-guitarBm.getHeight());
		List<Integer> playing = new ArrayList<>(4);
		playing.add(R.drawable.house_music_guitar1);
		playing.add(R.drawable.house_music_guitar2);
		playing.add(R.drawable.house_music_guitar3);
		playing.add(R.drawable.house_music_guitar2);
		guitar.getBitmapIds().put(1, playing);
		guitar.setStateTransition(1, TouchStateAnimatedBitmapSprite.TRANSITION_LOOP2);
		addSprite(guitar);
		
		Bitmap trumpetBm = BitmapFactory.decodeResource(getResources(), R.drawable.house_music_trumpet);
		TouchStateAnimatedBitmapSprite trumpet = new TouchStateAnimatedBitmapSprite(trumpetBm, activity);
		trumpet.setResources(getResources());
		trumpet.setIgnoreAlpha(true);
		trumpet.setX(0);
		trumpet.setY(guitar.getY());
		List<Integer> playingTrumptet = new ArrayList<>(4);
		playingTrumptet.add(R.drawable.house_music_trumpet1);
		playingTrumptet.add(R.drawable.house_music_trumpet2);
		playingTrumptet.add(R.drawable.house_music_trumpet3);
		playingTrumptet.add(R.drawable.house_music_trumpet2);
		trumpet.getBitmapIds().put(1, playingTrumptet);
		trumpet.setStateTransition(1, TouchStateAnimatedBitmapSprite.TRANSITION_LOOP2);
		addSprite(trumpet);
		
		Bitmap drumsBm = BitmapFactory.decodeResource(getResources(), R.drawable.house_music_drums);
		TouchStateAnimatedBitmapSprite drums = new TouchStateAnimatedBitmapSprite(drumsBm, activity);
		drums.setResources(getResources());
		drums.setX(getWidth()/2 - drumsBm.getWidth()/2);
		drums.setY(0);
		List<Integer> playingDrums = new ArrayList<>(8);
		playingDrums.add(R.drawable.house_music_drums1);
		playingDrums.add(R.drawable.house_music_drums2);
		playingDrums.add(R.drawable.house_music_drums3);
		playingDrums.add(R.drawable.house_music_drums4);
		playingDrums.add(R.drawable.house_music_drums5);
		playingDrums.add(R.drawable.house_music_drums6);
		playingDrums.add(R.drawable.house_music_drums7);
		playingDrums.add(R.drawable.house_music_drums8);
		drums.getBitmapIds().put(1, playingDrums);
		drums.setStateTransition(1, TouchStateAnimatedBitmapSprite.TRANSITION_LOOP1);
		addSprite(drums);
		
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
