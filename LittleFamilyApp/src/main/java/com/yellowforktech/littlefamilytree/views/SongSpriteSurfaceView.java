package com.yellowforktech.littlefamilytree.views;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.DisplayMetrics;

import com.yellowforktech.littlefamilytree.R;
import com.yellowforktech.littlefamilytree.activities.LittleFamilyActivity;
import com.yellowforktech.littlefamilytree.data.LittlePerson;
import com.yellowforktech.littlefamilytree.events.EventListener;
import com.yellowforktech.littlefamilytree.events.EventQueue;
import com.yellowforktech.littlefamilytree.sprites.AnimatedBitmapSprite;
import com.yellowforktech.littlefamilytree.sprites.DraggablePersonSprite;
import com.yellowforktech.littlefamilytree.sprites.Sprite;
import com.yellowforktech.littlefamilytree.sprites.TouchEventGameSprite;
import com.yellowforktech.littlefamilytree.sprites.TouchStateAnimatedBitmapSprite;
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
	
	private List<DraggablePersonSprite> onStage;

    private Rect dropRect;
    private int nextSpot = 0;
    private boolean dropReady = false;
    private boolean replaceReady = false;
    private int manWidth = 0;
    private int manHeight = 0;
    private int womanWidth = 0;
    private int womanHeight = 0;

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
		onStage = new ArrayList<>();
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
        if (selPerson1!=null) {
            if (dropReady) {
                selPerson1.setWidth((int) (manWidth + 5 * dm.density));
                selPerson1.setHeight((int) (manHeight + 5 * dm.density));
                selPerson3.setWidth((int) (manWidth + 5 * dm.density));
                selPerson3.setHeight((int) (manHeight + 5 * dm.density));
                selPerson2.setWidth((int) (womanWidth + 5 * dm.density));
                selPerson2.setHeight((int) (womanHeight + 5 * dm.density));
                selPerson4.setWidth((int) (womanWidth + 5 * dm.density));
                selPerson4.setHeight((int) (womanHeight + 5 * dm.density));
            } else {
                selPerson1.setWidth((int) (manWidth));
                selPerson1.setHeight((int) (manHeight));
                selPerson3.setWidth((int) (manWidth));
                selPerson3.setHeight((int) (manHeight));
                selPerson2.setWidth((int) (womanWidth));
                selPerson2.setHeight((int) (womanHeight));
                selPerson4.setWidth((int) (womanWidth));
                selPerson4.setHeight((int) (womanHeight));
            }
        }

        super.doStep();
		
		Iterator<DraggablePersonSprite> i = peopleSprites.iterator();
		while (i.hasNext()) {
			Sprite s = i.next();
			s.doStep();
			if (s.isRemoveMe()) i.remove();
		}
		
		i = onStage.iterator();
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

        manWidth = stage.getWidth()/7;
        womanWidth = (int) (manWidth + 4 * dm.density);
        Bitmap gPianoBm = ImageHelper.loadBitmapFromResource(activity, R.drawable.piano, 0, (int)(width*1.7), (int)(width*1.7));
        TouchStateAnimatedBitmapSprite gPiano = new TouchStateAnimatedBitmapSprite(gPianoBm, activity);
        gPiano.setX(getWidth() - (10 * dm.density + width + gPianoBm.getWidth()));
        gPiano.setY(stage.getHeight() - (20 * dm.density + gPianoBm.getHeight()));
        gPiano.setResources(getResources());
        addSprite(gPiano);

        Bitmap man = ImageHelper.loadBitmapFromResource(activity, R.drawable.man_silhouette, 0, 
				manWidth, manWidth);
		selPerson1 = new AnimatedBitmapSprite(man);
		selPerson1.setX(width);
        selPerson1.setY(stage.getHeight() / 2 - width / 2);
		addSprite(selPerson1);
        manWidth = selPerson1.getWidth();
        manHeight = selPerson1.getHeight();

        Bitmap woman = ImageHelper.loadBitmapFromResource(activity, R.drawable.woman_silhouette, 0,
				 womanWidth, womanWidth);
		selPerson2 = new AnimatedBitmapSprite(woman);
		selPerson2.setX(selPerson1.getX() + selPerson1.getWidth());
        selPerson2.setY(selPerson1.getY());
		addSprite(selPerson2);
        womanWidth = selPerson2.getWidth();
        womanHeight = selPerson2.getHeight();
		
		selPerson3 = new AnimatedBitmapSprite(man);
		selPerson3.setX(selPerson2.getX() + selPerson2.getWidth());
		selPerson3.setY(selPerson2.getY());
		addSprite(selPerson3);
		
		selPerson4 = new AnimatedBitmapSprite(woman);
		selPerson4.setX(selPerson3.getX() + selPerson3.getWidth());
		selPerson4.setY(selPerson3.getY());
		addSprite(selPerson4);

        dropRect = new Rect();
        dropRect.set((int)selPerson1.getX(), (int)selPerson1.getY()-50, (int)selPerson4.getX()+selPerson4.getWidth(), (int)selPerson4.getY()+selPerson4.getHeight()+50);
		
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
                sprite.setThresholdX((int) (5*dm.density));
                peopleSprites.add(sprite);
                y = y + sprite.getHeight() + (10 * dm.density);
                if (y > maxHeight) maxHeight = (int) y;
            }
			
			for(DraggablePersonSprite s : peopleSprites){
				s.setMaxHeight(maxHeight);
			}
        }

        Bitmap play = ImageHelper.loadBitmapFromResource(context, android.R.drawable.ic_media_play, 0, width, width);
        Bitmap pause = ImageHelper.loadBitmapFromResource(context, android.R.drawable.ic_media_pause, 0, width, width);
        TouchStateAnimatedBitmapSprite playButton = new TouchStateAnimatedBitmapSprite(play, context);
        List<Bitmap> pauseList = new ArrayList<>(1);
        pauseList.add(pause);
        playButton.getBitmaps().put(1, pauseList);
        playButton.setX(stage.getWidth()/2 - width/2);
        playButton.setY(70*dm.density);
        addSprite(playButton);
		
        spritesCreated = true;
    }
	
	private void reorderPeople() {
		float x = getWidth() * 0.82f;
		float y = 10*dm.density;
		for(DraggablePersonSprite s : peopleSprites) {
			s.setX(x);
			s.setY(y);
			y = y + s.getHeight() + (10 * dm.density);
		}
	}

    @Override
    protected void touch_start(float x, float y) {
        super.touch_start(x, y+clipY);

        for(int i=peopleSprites.size()-1; i>=0; i--) {
            Sprite s = peopleSprites.get(i);
            if (s.inSprite(x, y+clipY)) {
                selectedSprites.add(s);
                s.onSelect(x, y+clipY);
                if (!multiSelect) return;
            }
        }
		
		for(int i=onStage.size()-1; i>=0; i--) {
            Sprite s = onStage.get(i);
            if (s.inSprite(x, y)) {
                selectedSprites.add(s);
                s.onSelect(x, y);
                if (!multiSelect) return;
            }
        }
    }

    @Override
    public void doMove(float oldX, float oldY, float newX, float newY) {
        //super.doMove(oldX, oldY+clipY, newX, newY+clipY);

        boolean selectedMoved = false;
        if (selectedSprites.size() > 0) {
            for (Sprite s : selectedSprites) {
                boolean moved = s.onMove(oldX, oldY+clipY, newX, newY+clipY);
                selectedMoved |= moved;
                dropReady = false;
                replaceReady = false;
				if (s instanceof DraggablePersonSprite) {
					DraggablePersonSprite ds = (DraggablePersonSprite) s;
                    if (moved && dropRect.contains((int)s.getX(), (int)(s.getY()-clipY))) {
                        dropReady = true;
                        if (nextSpot==0)
                            ds.setTarget(selPerson1);
                        else if (nextSpot==1)
                            ds.setTarget(selPerson2);
                        else if (nextSpot==2)
                            ds.setTarget(selPerson3);
                        else ds.setTarget(selPerson4);

                        if (ds.getTarget()!=null) {
                            ds.setTargetX((int) ds.getTarget().getX());
                            ds.setTargetY((int) ds.getTarget().getY());
                            ds.setTargetHeight(womanHeight);
                        }
                    } else {
                        ds.setTarget(null);
                    }
                    if (onStage.contains(ds)) {
                        if (moved && ds.getX() > getWidth() * 0.82f) {
                            float py = 0;
                            replaceReady = true;
                            boolean adjusted = false;
                            for (DraggablePersonSprite p : peopleSprites) {
                                if (!adjusted && p.getY() > ds.getY()) {
                                    py = py + p.getHeight() + (10 * dm.density);
                                    adjusted = true;
                                }
                                p.setY(py);
                                py = py + p.getHeight() + (10 * dm.density);
                            }
                        } else {
                            reorderPeople();
                        }
                    }
				}
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
    protected void touch_up(float x, float y) {

		if(dropReady) {
			for(Sprite s : selectedSprites) {
				if (s instanceof DraggablePersonSprite) {
					DraggablePersonSprite ds = (DraggablePersonSprite) s;
					peopleSprites.remove(ds);
					onStage.add(ds);
					ds.setY(ds.getY()-clipY);
                    ds.setThresholdX(5);
                    ds.setThresholdY(5);
				}
			}
			nextSpot++;
			reorderPeople();
		}
        dropReady = false;

        if (replaceReady) {
            int i=0;
            while(i<peopleSprites.size()) {
                if (y>peopleSprites.get(i).getY()) {
                    break;
                }
                i++;
            }
            for(Sprite s : selectedSprites) {
                if (s instanceof DraggablePersonSprite) {
                    onStage.remove(s);
                    peopleSprites.add(i, (DraggablePersonSprite) s);
                }
            }
            reorderPeople();
        }
        replaceReady = false;
		
		super.touch_up(x, y);
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
			
			for (Sprite s : onStage) {
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
