package com.yellowforktech.littlefamilytree.views;

import com.yellowforktech.littlefamilytree.sprites.*;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import com.yellowforktech.littlefamilytree.R;
import com.yellowforktech.littlefamilytree.activities.LittleFamilyActivity;
import com.yellowforktech.littlefamilytree.data.LittlePerson;
import com.yellowforktech.littlefamilytree.events.EventListener;
import com.yellowforktech.littlefamilytree.events.EventQueue;
import com.yellowforktech.littlefamilytree.util.ImageHelper;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Created by kids on 6/17/15.
 */
public class SongSpriteSurfaceView extends SpritedSurfaceView implements EventListener {
    public static final String TOPIC_PERSON_TOUCHED = "topic_person_touched";
    private List<LittlePerson> family;
    private DisplayMetrics dm;
    private boolean spritesCreated = false;
    private List<DraggablePersonSprite> peopleSprites;

    private LittleFamilyActivity activity;
    private float clipY = 0;
    private int maxHeight;
    private Bitmap stage;
	
	private AnimatedBitmapSprite selPerson1;
	private AnimatedBitmapSprite selPerson2;
	private AnimatedBitmapSprite selPerson3;
	private AnimatedBitmapSprite selPerson4;

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
        peopleSprites = new ArrayList<>();
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
		
		Iterator<DraggablePersonSprite> i = peopleSprites.iterator();
		while (i.hasNext()) {
			Sprite s = i.next();
			s.doStep();
			if (s.isRemoveMe()) i.remove();
		}
    }

    public void createSprites() {
        synchronized (sprites) {
            sprites.clear();
        }

        maxHeight = this.getHeight();
        int width = (int) (getWidth() * 0.17f);
        if (width > 250) width = 250;

        stage = ImageHelper.loadBitmapFromResource(context, R.drawable.stage, 0, getWidth()-width, getHeight());

        int centerX = (getWidth()/2 - width);
        int centerY = (getHeight() /2);

		/*
		Bitmap pianoBm = BitmapFactory.decodeResource(getResources(), R.drawable.house_music_piano);
		TouchStateAnimatedBitmapSprite piano = new TouchStateAnimatedBitmapSprite(pianoBm, activity);
        float r = (float) pianoBm.getWidth() / pianoBm.getHeight();
        piano.setIgnoreAlpha(true);
        piano.setWidth(width);
        piano.setHeight((int) (width / r));
		piano.setX(centerX - (piano.getWidth()/2));
		piano.setY(centerY + width);
		addSprite(piano);
		
		Bitmap guitarBm = BitmapFactory.decodeResource(getResources(), R.drawable.house_music_guitar);
		TouchStateAnimatedBitmapSprite guitar = new TouchStateAnimatedBitmapSprite(guitarBm, activity);
        r = (float) guitarBm.getWidth() / guitarBm.getHeight();
        guitar.setIgnoreAlpha(true);
        guitar.setWidth(width);
        guitar.setHeight((int) (width / r));
		guitar.setResources(getResources());
		guitar.setX(centerX + width);
		guitar.setY(centerY-guitar.getHeight()/2);
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
        r = (float) trumpetBm.getWidth() / trumpetBm.getHeight();
        trumpet.setIgnoreAlpha(true);
        trumpet.setWidth(width);
        trumpet.setHeight((int) (width / r));
		trumpet.setResources(getResources());
		trumpet.setIgnoreAlpha(true);
		trumpet.setX((centerX - width) - trumpet.getWidth());
		trumpet.setY(centerY - trumpet.getHeight() / 2);
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
        r = (float) drumsBm.getWidth() / drumsBm.getHeight();
        drums.setIgnoreAlpha(true);
        drums.setWidth(width);
        drums.setHeight((int) (width / r));
		drums.setResources(getResources());
		drums.setX(centerX - drums.getWidth() / 2);
		drums.setY((centerY - width) - drums.getHeight());
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
		*/

        Bitmap gPianoBm = ImageHelper.loadBitmapFromResource(activity, R.drawable.piano, 0, (int)(width*1.7), (int)(width*1.7));
        TouchStateAnimatedBitmapSprite gPiano = new TouchStateAnimatedBitmapSprite(gPianoBm, activity);
        gPiano.setX(getWidth() - (10*dm.density + width + gPianoBm.getWidth()));
        gPiano.setY(stage.getHeight() - (20 * dm.density + gPianoBm.getHeight()));
        gPiano.setResources(getResources());
        addSprite(gPiano);

        Bitmap man = ImageHelper.loadBitmapFromResource(activity, R.drawable.man_silhouette, 0, 
				(int)(width-23*dm.density), (int)(width-23-dm.density));
		selPerson1 = new AnimatedBitmapSprite(man);
		selPerson1.setX(width);
		selPerson1.setY(stage.getHeight()/2 - width/2);
		addSprite(selPerson1);
		
		Bitmap woman = ImageHelper.loadBitmapFromResource(activity, R.drawable.woman_silhouette, 0, 
				 (int)(width-12*dm.density), (int)(width-12*dm.density));
		selPerson2 = new AnimatedBitmapSprite(woman);
		selPerson2.setX(selPerson1.getX()+selPerson1.getWidth());
		selPerson2.setY(selPerson1.getY());
		addSprite(selPerson2);
		
		selPerson3 = new AnimatedBitmapSprite(man);
		selPerson3.setX(selPerson2.getX()+selPerson2.getWidth());
		selPerson3.setY(selPerson2.getY());
		addSprite(selPerson3);
		
		selPerson4 = new AnimatedBitmapSprite(woman);
		selPerson4.setX(selPerson3.getX()+selPerson3.getWidth());
		selPerson4.setY(selPerson3.getY());
		addSprite(selPerson4);
		
        if (family!=null) {
            float x = getWidth() * 0.82f;
            float y = 10*dm.density;
            for (LittlePerson person : family) {
                Bitmap photo = null;
                if (person.getPhotoPath() != null) {
                    photo = ImageHelper.loadBitmapFromFile(person.getPhotoPath(), ImageHelper.getOrientation(person.getPhotoPath()), width, width, false);
                }
                if (photo == null) {
                    photo = ImageHelper.loadBitmapFromResource(activity, person.getDefaultPhotoResource(), 0, width, width);
                }
                DraggablePersonSprite sprite = new DraggablePersonSprite(photo, person, getWidth(), maxHeight, TOPIC_PERSON_TOUCHED);
                sprite.setX(x);
                sprite.setY(y);
                sprite.setData("person", person);
                peopleSprites.add(sprite);
                y = y + sprite.getHeight() + (10 * dm.density);
                if (y > maxHeight) maxHeight = (int) y;
            }
			
			for(DraggablePersonSprite s : peopleSprites){
				s.setMaxHeight(maxHeight);
			}
        }
		
        spritesCreated = true;
    }

    @Override
    protected void touch_start(float x, float y) {
        super.touch_start(x, y+clipY);

        for(int i=peopleSprites.size()-1; i>=0; i--) {
            Sprite s = peopleSprites.get(i);
            if (s.inSprite(x, y+clipY)) {
                selectedSprites.add(s);
                s.onSelect(x, y+clipY);
                if (!multiSelect) break;
            }
        }
    }

    @Override
    public void doMove(float oldX, float oldY, float newX, float newY) {
        //super.doMove(oldX, oldY+clipY, newX, newY+clipY);

        boolean selectedMoved = false;
        if (selectedSprites.size() > 0) {
            for (Sprite s : selectedSprites) {
                selectedMoved |= s.onMove(oldX, oldY+clipY, newX, newY+clipY);
            }
        }
        if (!selectedMoved) {
            clipY -= (newY-oldY);
            if (maxHeight <= getHeight()) {
                clipY = 0;
            } else {
                if (clipY < 0) clipY = 0;
                else if (clipY + getHeight() > maxHeight) clipY = maxHeight - getHeight();
            }
        }

    }

    @Override
    public void doDraw(Canvas canvas) {
        canvas.drawColor(0, android.graphics.PorterDuff.Mode.CLEAR);
        if (!spritesCreated) {
            createSprites();
        }
        synchronized (sprites) {
            canvas.drawBitmap(stage, 0, 5, null);

            for (Sprite s : sprites) {
                if (s.getX() + s.getWidth() >= 0 && s.getX() <= getWidth() && s.getY() + s.getHeight() >= 0 && s.getY() <= getHeight()) {
                    s.doDraw(canvas);
                }
            }

            canvas.translate(0, -clipY);
            for (Sprite s : peopleSprites) {
                if (s.getX() + s.getWidth() >= 0 && s.getX() <= getWidth() && s.getY() + s.getHeight() >= clipY && s.getY() <= getHeight() + clipY) {
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
