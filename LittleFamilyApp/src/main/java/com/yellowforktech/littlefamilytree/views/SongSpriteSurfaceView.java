package com.yellowforktech.littlefamilytree.views;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.util.DisplayMetrics;

import com.yellowforktech.littlefamilytree.R;
import com.yellowforktech.littlefamilytree.activities.LittleFamilyActivity;
import com.yellowforktech.littlefamilytree.data.LittlePerson;
import com.yellowforktech.littlefamilytree.events.EventListener;
import com.yellowforktech.littlefamilytree.events.EventQueue;
import com.yellowforktech.littlefamilytree.sprites.Sprite;
import com.yellowforktech.littlefamilytree.sprites.TouchEventGameSprite;
import com.yellowforktech.littlefamilytree.sprites.TouchStateAnimatedBitmapSprite;
import com.yellowforktech.littlefamilytree.util.ImageHelper;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by kids on 6/17/15.
 */
public class SongSpriteSurfaceView extends SpritedSurfaceView implements EventListener {
    public static final String TOPIC_PERSON_TOUCHED = "topic_person_touched";
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

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        EventQueue.getInstance().unSubscribe(TOPIC_PERSON_TOUCHED, this);
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        EventQueue.getInstance().subscribe(TOPIC_PERSON_TOUCHED, this);
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
        spritesCreated = false;
    }

    @Override
    public void doStep() {
        super.doStep();
    }

    public void createSprites() {
        synchronized (sprites) {
            sprites.clear();
        }
        int width = (int) (getWidth() * 0.2f);

		Bitmap pianoBm = BitmapFactory.decodeResource(getResources(), R.drawable.house_music_piano);
		TouchStateAnimatedBitmapSprite piano = new TouchStateAnimatedBitmapSprite(pianoBm, activity);
		piano.setX((getWidth()/2) - (pianoBm.getWidth()/2) - (width/2));
		piano.setY(getHeight()/2 + width*2);
		addSprite(piano);
		
		Bitmap guitarBm = BitmapFactory.decodeResource(getResources(), R.drawable.house_music_guitar);
		TouchStateAnimatedBitmapSprite guitar = new TouchStateAnimatedBitmapSprite(guitarBm, activity);
		guitar.setResources(getResources());
		guitar.setX(piano.getX()+piano.getWidth());
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
		trumpet.setX(piano.getX() - trumpetBm.getWidth());
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
		drums.setX(piano.getX() + piano.getWidth()/2 - (drumsBm.getWidth() / 2));
		drums.setY(piano.getY() - (width*2));
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

        if (family!=null) {
            float x = getWidth() * 0.8f;
            float y = 10*dm.density;
            for (LittlePerson person : family) {
                Bitmap photo = null;
                if (person.getPhotoPath() != null) {
                    photo = ImageHelper.loadBitmapFromFile(person.getPhotoPath(), ImageHelper.getOrientation(person.getPhotoPath()), width, width, false);
                }
                if (photo == null) {
                    photo = ImageHelper.loadBitmapFromResource(activity, person.getDefaultPhotoResource(), 0, width, width);
                }
                TouchEventGameSprite sprite = new TouchEventGameSprite(photo, TOPIC_PERSON_TOUCHED);
                sprite.setX(x);
                sprite.setY(y);
                sprite.setData("person", person);
                addSprite(sprite);
                y = y + sprite.getHeight() + (10 * dm.density);
            }
        }
		
        spritesCreated = true;
    }

    @Override
    public void doDraw(Canvas canvas) {
        canvas.drawColor(0, android.graphics.PorterDuff.Mode.CLEAR);
        if (!spritesCreated) {
            createSprites();
        }
        synchronized (sprites) {
            for (Sprite s : sprites) {
                if (s.getX() + s.getWidth() >= 0 && s.getX() <= getWidth() && s.getY() + s.getHeight() >= 0 && s.getY() <= getHeight()) {
                    s.doDraw(canvas);
                }
            }
        }
    }

    @Override
    public void onEvent(String topic, Object o) {
        if (TOPIC_PERSON_TOUCHED.equals(topic)) {
            TouchEventGameSprite sprite = (TouchEventGameSprite) o;
            LittlePerson person = (LittlePerson) sprite.getData("person");
            if (person!=null) {
                activity.speak(person.getName());
            }
        }
    }
}
