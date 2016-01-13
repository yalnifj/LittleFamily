package com.yellowforktech.littlefamilytree.views;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.media.MediaPlayer;
import android.util.AttributeSet;
import android.util.DisplayMetrics;

import com.yellowforktech.littlefamilytree.R;
import com.yellowforktech.littlefamilytree.activities.SongActivity;
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
    public static final String TOPIC_PLAY_SONG = "playPauseSong";
    public static final String TOPIC_PLAY_RESET = "resetStage";
	public static final String TOPIC_TOGGLE_PIANO = "togglePiano";
	public static final String TOPIC_TOGGLE_DRUMS = "toggleDrums";
	public static final String TOPIC_TOGGLE_FLUTE = "toggleFlute";
	public static final String TOPIC_TOGGLE_VIOLIN = "toggleViolin";
	
    private List<LittlePerson> family;
    private List<LittlePerson> finishedPeople;
    private DisplayMetrics dm;
    private boolean spritesCreated = false;
    private List<DraggablePersonSprite> peopleSprites;

    private SongActivity activity;
    private float clipY = 0;
    private int maxHeight;
    private Bitmap stage;
	private Paint textPaint;
	
	private AnimatedBitmapSprite selPerson1;
	private AnimatedBitmapSprite selPerson2;
	private AnimatedBitmapSprite selPerson3;
	private AnimatedBitmapSprite selPerson4;
	
	private TouchStateAnimatedBitmapSprite drumKit;
	private TouchStateAnimatedBitmapSprite gPiano;
	private TouchStateAnimatedBitmapSprite violin;
	private TouchStateAnimatedBitmapSprite clarinet;
	
	private TouchStateAnimatedBitmapSprite playButton;
	
	private List<DraggablePersonSprite> onStage;

    private Rect dropRect;
    private int nextSpot = 0;
    private boolean dropReady = false;
    private boolean replaceReady = false;
    private int manWidth = 0;
    private int manHeight = 0;
    private int womanWidth = 0;
    private int womanHeight = 0;
    protected int rotation = 0;
    protected int rotateDir = 1;
    protected MediaPlayer pianoPlayer;
    protected MediaPlayer drumsPlayer;
    protected MediaPlayer flutePlayer;
    protected MediaPlayer violinPlayer;
	protected boolean pianoOn = true;
	protected boolean drumsOn = false;
	protected boolean fluteOn = true;
	protected boolean violinOn = false;

    private boolean playing = false;
	
	private long playSteps;
	private boolean name1Spoken;
	private boolean name2Spoken;
	private boolean name3Spoken;
	private boolean name4Spoken;

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
        finishedPeople = new ArrayList<>();
        peopleSprites = new ArrayList<>();
		onStage = new ArrayList<>();
        pianoPlayer = MediaPlayer.create(context, R.raw.piano_allinourfamilytree);
		pianoPlayer.setVolume(1,1);
        drumsPlayer = MediaPlayer.create(context, R.raw.drums_allinourfamilytree);
        drumsPlayer.setVolume(0, 0);
        flutePlayer = MediaPlayer.create(context, R.raw.flute_allinourfamilytree);
        flutePlayer.setVolume(0.3f, 0.3f);
        violinPlayer = MediaPlayer.create(context, R.raw.violin_allinourfamilytree);
        violinPlayer.setVolume(0, 0);
		touchTolerance=6;
		
		textPaint = new Paint();
		textPaint.setColor(Color.BLACK);
		textPaint.setTextSize(40);
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        EventQueue.getInstance().unSubscribe(TOPIC_PERSON_TOUCHED, this);
        EventQueue.getInstance().unSubscribe(TOPIC_PLAY_SONG, this);
        EventQueue.getInstance().unSubscribe(TOPIC_PLAY_RESET, this);
		EventQueue.getInstance().unSubscribe(TOPIC_TOGGLE_PIANO, this);
		EventQueue.getInstance().unSubscribe(TOPIC_TOGGLE_FLUTE, this);
		EventQueue.getInstance().unSubscribe(TOPIC_TOGGLE_VIOLIN, this);
		EventQueue.getInstance().unSubscribe(TOPIC_TOGGLE_DRUMS, this);
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        EventQueue.getInstance().subscribe(TOPIC_PERSON_TOUCHED, this);
        EventQueue.getInstance().subscribe(TOPIC_PLAY_SONG, this);
        EventQueue.getInstance().subscribe(TOPIC_PLAY_RESET, this);
		EventQueue.getInstance().subscribe(TOPIC_TOGGLE_PIANO, this);
		EventQueue.getInstance().subscribe(TOPIC_TOGGLE_FLUTE, this);
		EventQueue.getInstance().subscribe(TOPIC_TOGGLE_VIOLIN, this);
		EventQueue.getInstance().subscribe(TOPIC_TOGGLE_DRUMS, this);
		
    }

    public SongActivity getActivity() {
        return activity;
    }

    public void setActivity(SongActivity activity) {
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

        if (playing) {
			playSteps++;
			if (playSteps>220 && playSteps<230) {
				DraggablePersonSprite ds = onStage.get(0);
				ds.setTargetX(stage.getWidth()/2 - (int)(50*dm.density));
				ds.setTargetHeight((int)(100*dm.density));
				ds.setTargetY((int)(selPerson1.getY()+selPerson1.getHeight()));
				ds.setMoving(true);
			}
			if (!name1Spoken && playSteps>230) {
				DraggablePersonSprite ds = onStage.get(0);
				activity.speak(ds.getPerson().getGivenName());
				name1Spoken=true;
			}
			if (playSteps>280 && playSteps<290) {
				DraggablePersonSprite ds = onStage.get(0);
				ds.setTargetX((int)selPerson1.getX());
				ds.setTargetHeight(womanHeight);
				ds.setTargetY((int)(selPerson1.getY()));
				ds.setMoving(true);
				
				ds = onStage.get(1);
				ds.setTargetX(stage.getWidth()/2 - (int)(50*dm.density));
				ds.setTargetHeight((int)(100*dm.density));
				ds.setTargetY((int)(selPerson1.getY()+selPerson1.getHeight()));
				ds.setMoving(true);
			}
			if (!name2Spoken && playSteps>290) {
				DraggablePersonSprite ds = onStage.get(1);
				activity.speak(ds.getPerson().getGivenName());
				name2Spoken=true;
			}
			if (playSteps>340 && playSteps<350) {
				DraggablePersonSprite ds = onStage.get(1);
				ds.setTargetX((int)selPerson2.getX());
				ds.setTargetHeight(womanHeight);
				ds.setTargetY((int)(selPerson2.getY()));
				ds.setMoving(true);

				ds = onStage.get(2);
				ds.setTargetX(stage.getWidth()/2 - (int)(50*dm.density));
				ds.setTargetHeight((int)(100*dm.density));
				ds.setTargetY((int)(selPerson1.getY()+selPerson1.getHeight()));
				ds.setMoving(true);
			}
			if (!name3Spoken && playSteps>365) {
				DraggablePersonSprite ds = onStage.get(2);
				activity.speak(ds.getPerson().getGivenName());
				name3Spoken=true;
			}
			if (playSteps>410 && playSteps<420) {
				DraggablePersonSprite ds = onStage.get(2);
				ds.setTargetX((int)selPerson3.getX());
				ds.setTargetHeight(womanHeight);
				ds.setTargetY((int)(selPerson3.getY()));
				ds.setMoving(true);

				ds = onStage.get(3);
				ds.setTargetX(stage.getWidth()/2 - (int)(50*dm.density));
				ds.setTargetHeight((int)(100*dm.density));
				ds.setTargetY((int)(selPerson1.getY()+selPerson1.getHeight()));
				ds.setMoving(true);
			}
			if (!name4Spoken && playSteps>420) {
				DraggablePersonSprite ds = onStage.get(3);
				activity.speak(ds.getPerson().getGivenName());
				name4Spoken=true;
			}
			if (playSteps>480 && playSteps<490) {
				DraggablePersonSprite ds = onStage.get(3);
				ds.setTargetX((int)selPerson4.getX());
				ds.setTargetHeight(womanHeight);
				ds.setTargetY((int)(selPerson4.getY()));
				ds.setMoving(true);
			}
			if (playSteps > 720) {
                for(DraggablePersonSprite ds : onStage) {
                    LittlePerson p = ds.getPerson();
                    family.remove(p);
                    finishedPeople.add(p);
                    ds.setTargetX(-100);
                    ds.setMoving(true);
                }
			}
            if (playSteps > 780) {
                resetSong();
                synchronized (sprites) {
                    onStage.clear();
                }
                if (family.size()<8) activity.loadMorePeople();
                reorderPeople();
            }
            rotation += rotateDir;
            if (rotation > 10) {
                rotateDir = -1;
            }
            if (rotation < -10) {
                rotateDir = 1;
            }
        } else {
            rotation = 0;
        }
		
		Iterator<DraggablePersonSprite> i = peopleSprites.iterator();
		while (i.hasNext()) {
            DraggablePersonSprite s = i.next();
			s.doStep();
			if (s.isRemoveMe()) i.remove();
		}
		
		i = onStage.iterator();
		while (i.hasNext()) {
            DraggablePersonSprite s = i.next();
			s.doStep();
			if (s.isRemoveMe()) i.remove();
		}
    }

    public void createSprites() {
        synchronized (sprites) {
            sprites.clear();
            onStage.clear();
            peopleSprites.clear();
        }

        maxHeight = this.getHeight();
        int width = (int) (getWidth() * 0.17f);
        if (width > 250) width = 250;

        stage = ImageHelper.loadBitmapFromResource(context, R.drawable.stage, 0, getWidth()-width, getHeight());

        manWidth = stage.getWidth()/7;
        womanWidth = (int) (manWidth + 4 * dm.density);

        Bitmap drumsBm = ImageHelper.loadBitmapFromResource(activity, R.drawable.drums, 0, (int)(width*1.7), (int)(width*1.7));
        drumKit = new TouchStateAnimatedBitmapSprite(drumsBm, activity);
        drumKit.setX(10 * dm.density);
        drumKit.setY(stage.getHeight() - (55 * dm.density + drumsBm.getHeight()));
        drumKit.setResources(getResources());
        drumKit.setStateTransitionEvent(0, TOPIC_TOGGLE_DRUMS);
        drumKit.setStateTransitionEvent(1, TOPIC_TOGGLE_DRUMS);
        drumKit.addBitmap(1, ImageHelper.loadBitmapFromResource(activity, R.drawable.drums_off, 0, (int)(width*1.7), (int)(width * 1.7)));
        drumKit.setState(1);
        addSprite(drumKit);

        Bitmap gPianoBm = ImageHelper.loadBitmapFromResource(activity, R.drawable.piano, 0, (int)(width*1.7), (int)(width*1.7));
        gPiano = new TouchStateAnimatedBitmapSprite(gPianoBm, activity);
        gPiano.setX(getWidth() - (15 * dm.density + width + gPianoBm.getWidth()));
        gPiano.setY(stage.getHeight() - (35 * dm.density + gPianoBm.getHeight()));
        gPiano.setResources(getResources());
		gPiano.setStateTransitionEvent(0, TOPIC_TOGGLE_PIANO);
        gPiano.setStateTransitionEvent(1, TOPIC_TOGGLE_PIANO);
        gPiano.addBitmap(1, ImageHelper.loadBitmapFromResource(activity, R.drawable.piano_off, 0, (int)(width*1.7), (int)(width*1.7)));
        addSprite(gPiano);

        Bitmap violinBm = ImageHelper.loadBitmapFromResource(activity, R.drawable.violin, 0, (int)(width*1.8), (int)(width*1.8));
        violin = new TouchStateAnimatedBitmapSprite(violinBm, activity);
        violin.setX(stage.getWidth()/2 - violinBm.getWidth() / 3f);
        violin.setY(stage.getHeight() - (violinBm.getHeight()));
        violin.setResources(getResources());
        violin.setStateTransitionEvent(0, TOPIC_TOGGLE_VIOLIN);
        violin.setStateTransitionEvent(1, TOPIC_TOGGLE_VIOLIN);
        violin.addBitmap(1, ImageHelper.loadBitmapFromResource(activity, R.drawable.violin_off, 0, (int)(width*1.8), (int)(width*1.8)));
        violin.setState(1);
        addSprite(violin);

        Bitmap clarinetBm = ImageHelper.loadBitmapFromResource(activity, R.drawable.clarinet, 0, (int)(width*1.5), (int)(width*1.5));
        clarinet = new TouchStateAnimatedBitmapSprite(clarinetBm, activity);
        clarinet.setX(stage.getWidth()/2- clarinetBm.getWidth());
        clarinet.setY(stage.getHeight() - (20 * dm.density + clarinetBm.getHeight()));
        clarinet.setResources(getResources());
		clarinet.setStateTransitionEvent(0, TOPIC_TOGGLE_FLUTE);
        clarinet.setStateTransitionEvent(1, TOPIC_TOGGLE_FLUTE);
		clarinet.setIgnoreAlpha(true);
        clarinet.addBitmap(1, ImageHelper.loadBitmapFromResource(activity, R.drawable.clarinet_off, 0, (int)(width*1.5), (int)(width*1.5)));
        addSprite(clarinet);

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
                DraggablePersonSprite sprite = new DraggablePersonSprite(photo, person, getWidth(), maxHeight, TOPIC_PERSON_TOUCHED, dm);
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
        playButton = new TouchStateAnimatedBitmapSprite(play, context);
        List<Bitmap> pauseList = new ArrayList<>(1);
        pauseList.add(pause);
        playButton.getBitmaps().put(1, pauseList);
        playButton.setX(stage.getWidth() / 2 - width);
        playButton.setY(70 * dm.density);
        playButton.setStateTransitionEvent(0, TOPIC_PLAY_SONG);
        playButton.setStateTransitionEvent(1, TOPIC_PLAY_SONG);
        playButton.setIgnoreAlpha(true);
        addSprite(playButton);

        Bitmap reset = ImageHelper.loadBitmapFromResource(context, android.R.drawable.ic_menu_revert, 0, width, width);
        TouchStateAnimatedBitmapSprite resetButton = new TouchStateAnimatedBitmapSprite(reset, context);
        resetButton.setX(stage.getWidth() / 2);
        resetButton.setY(70 * dm.density);
        resetButton.setStateTransitionEvent(0, TOPIC_PLAY_RESET);
        resetButton.setIgnoreAlpha(true);
        addSprite(resetButton);
		
        spritesCreated = true;
    }
	
	private void reorderPeople() {
        synchronized (sprites) {
            float x = getWidth() * 0.82f;
            float y = 10 * dm.density;
            int width = (int) (getWidth() * 0.17f);
            if (width > 250) width = 250;
            for (DraggablePersonSprite s : peopleSprites) {
                s.setTargetX((int) x);
                s.setTargetY((int) y);
                s.setTargetHeight(width);
                s.setMoving(true);
                y = y + width + (10 * dm.density);
            }

            nextSpot = 0;
            for (DraggablePersonSprite ds : onStage) {
                if (nextSpot == 0)
                    ds.setTarget(selPerson1);
                else if (nextSpot == 1)
                    ds.setTarget(selPerson2);
                else if (nextSpot == 2)
                    ds.setTarget(selPerson3);
                else ds.setTarget(selPerson4);

                if (ds.getTarget() != null) {
                    ds.setTargetX((int) ds.getTarget().getX());
                    ds.setTargetY((int) ds.getTarget().getY());
                    ds.setTargetHeight(womanHeight);
                    ds.setMoving(true);
                }
                nextSpot++;
            }
        }
	}

    @Override
    protected void touch_start(float x, float y) {
        super.touch_start(x, y+clipY);

        if (!playing) {
            for (int i = peopleSprites.size() - 1; i >= 0; i--) {
                Sprite s = peopleSprites.get(i);
                if (s.inSprite(x, y + clipY)) {
                    selectedSprites.add(s);
                    s.onSelect(x, y + clipY);
                    if (!multiSelect) return;
                }
            }

            for (int i = onStage.size() - 1; i >= 0; i--) {
                Sprite s = onStage.get(i);
                if (s.inSprite(x, y)) {
                    selectedSprites.add(s);
                    s.onSelect(x, y);
                    if (!multiSelect) return;
                }
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
                    if (!onStage.contains(ds)) {
                        if (moved && onStage.size()<4 && dropRect.contains((int) s.getX(), (int) (s.getY() - clipY))) {
                            dropReady = true;
                        } else {
                            ds.setTarget(null);
                        }
                    } else {
                        if (moved && ds.getX() > getWidth() * 0.82f) {
                            float py = 10 * dm.density;
                            replaceReady = true;
                            boolean adjusted = false;
                            for (DraggablePersonSprite p : peopleSprites) {
                                if (!adjusted && p.getY() > ds.getY()+clipY) {
                                    py = py + p.getHeight() + (10 * dm.density);
                                    adjusted = true;
                                }
                                p.setTargetY((int) py);
                                p.setMoving(true);
                                py = py + p.getHeight() + (10 * dm.density);
                            }
                        } else if (replaceReady){
                            reorderPeople();
                            replaceReady=false;
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
            synchronized (sprites) {
                for (Sprite s : selectedSprites) {
                    if (s instanceof DraggablePersonSprite) {
                        DraggablePersonSprite ds = (DraggablePersonSprite) s;
                        peopleSprites.remove(ds);
                        onStage.add(ds);
                        ds.setY(ds.getY() - clipY);
                        ds.setThresholdX(5);
                        ds.setThresholdY(5);
                    }
                }
            }
		}
        dropReady = false;

        if (replaceReady) {
            synchronized (sprites) {
                int i = 0;
                while (i < peopleSprites.size()) {
                    if (y < peopleSprites.get(i).getY()) {
                        break;
                    }
                    i++;
                }
                for (Sprite s : selectedSprites) {
                    if (s instanceof DraggablePersonSprite) {
                        onStage.remove(s);
                        peopleSprites.add(i, (DraggablePersonSprite) s);
                    }
                }
            }
        }
        replaceReady = false;

        reorderPeople();
		
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
                    canvas.save();
                    canvas.rotate(rotation, s.getX()+s.getWidth()/2, s.getY()+s.getHeight()/2);
                    s.doDraw(canvas);
                    canvas.restore();
                }
            }
			
			//canvas.drawText("playSteps="+playSteps, 30, 30, textPaint);

            canvas.translate(0, -clipY);
            for (Sprite s : peopleSprites) {
                if (s.getX() + s.getWidth() >= 0 && s.getX() <= getWidth() && s.getY() + s.getHeight() >= clipY && s.getY() <= getHeight() + clipY) {
                    s.doDraw(canvas);
                }
            }
        }
    }
	
	public void resetSong() {
		playing = false;
		playSteps = 0;
		name1Spoken=false;
		name2Spoken=false;
		name3Spoken=false;
		name4Spoken=false;
        if (pianoPlayer!=null) pianoPlayer.release();
		pianoPlayer = MediaPlayer.create(context, R.raw.piano_allinourfamilytree);
        if (drumsPlayer!=null) drumsPlayer.release();
		drumsPlayer = MediaPlayer.create(context, R.raw.drums_allinourfamilytree);
        if (flutePlayer!=null) flutePlayer.release();
		flutePlayer = MediaPlayer.create(context, R.raw.flute_allinourfamilytree);
        if (violinPlayer!=null) violinPlayer.release();
		violinPlayer = MediaPlayer.create(context, R.raw.violin_allinourfamilytree);
		if (!fluteOn) {
			flutePlayer.setVolume(0,0);
		} else {
			flutePlayer.setVolume(0.3f,0.3f);
		}
		if (!drumsOn) {
			drumsPlayer.setVolume(0,0);
		} else {
			drumsPlayer.setVolume(1,1);
		}
		if (!violinOn) {
			violinPlayer.setVolume(0,0);
		} else {
			violinPlayer.setVolume(0.5f,0.5f);
		}
		if (!pianoOn) {
			pianoPlayer.setVolume(0,0);
		} else {
			pianoPlayer.setVolume(1,1);
		}
		playButton.setState(0);
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
        else if (topic.equals(TOPIC_PLAY_SONG)) {

            if (onStage.size()<4) {
                synchronized (sprites) {
                    while (onStage.size() < 4 && peopleSprites.size() > 0) {
                        DraggablePersonSprite sprite = peopleSprites.remove(0);
                        onStage.add(sprite);
                    }
                }
                reorderPeople();
            }

            //-- play or pause
            if (!playing) {
                playing = true;
                pianoPlayer.start();
                drumsPlayer.start();
                flutePlayer.start();
                violinPlayer.start();
            }
            else {
                pianoPlayer.pause();
                drumsPlayer.pause();
                flutePlayer.pause();
                violinPlayer.pause();
                playing = false;
            }
        }
        else if (topic.equals(TOPIC_PLAY_RESET)) {
            resetSong();
			createSprites();
        }
		else if (topic.equals(TOPIC_TOGGLE_FLUTE)) {
			fluteOn = !fluteOn;
			if (!fluteOn) {
				flutePlayer.setVolume(0,0);
			} else {
				flutePlayer.setVolume(0.3f,0.3f);
			}
		}
		else if (topic.equals(TOPIC_TOGGLE_VIOLIN)) {
			violinOn = !violinOn;
			if (!violinOn) {
				violinPlayer.setVolume(0,0);
			} else {
				violinPlayer.setVolume(0.5f,0.5f);
			}
		}
		else if (topic.equals(TOPIC_TOGGLE_PIANO)) {
			pianoOn = !pianoOn;
			if (!pianoOn) {
				pianoPlayer.setVolume(0,0);
			} else {
				pianoPlayer.setVolume(1,1);
			}
		}
		else if (topic.equals(TOPIC_TOGGLE_DRUMS)) {
			drumsOn=!drumsOn;
			if (!drumsOn) {
				drumsPlayer.setVolume(0,0);
			} else {
				drumsPlayer.setVolume(1,1);
			}
		}
    }
}
