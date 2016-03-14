package com.yellowforktech.littlefamilytree.views;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.media.MediaPlayer;
import android.preference.PreferenceManager;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;

import com.yellowforktech.littlefamilytree.R;
import com.yellowforktech.littlefamilytree.activities.SongActivity;
import com.yellowforktech.littlefamilytree.data.LittlePerson;
import com.yellowforktech.littlefamilytree.events.EventListener;
import com.yellowforktech.littlefamilytree.events.EventQueue;
import com.yellowforktech.littlefamilytree.games.InstrumentType;
import com.yellowforktech.littlefamilytree.games.Song;
import com.yellowforktech.littlefamilytree.games.SongAlbum;
import com.yellowforktech.littlefamilytree.sprites.AnimatedBitmapSprite;
import com.yellowforktech.littlefamilytree.sprites.DraggablePersonSprite;
import com.yellowforktech.littlefamilytree.sprites.Sprite;
import com.yellowforktech.littlefamilytree.sprites.TextSprite;
import com.yellowforktech.littlefamilytree.sprites.TouchEventGameSprite;
import com.yellowforktech.littlefamilytree.sprites.TouchStateAnimatedBitmapSprite;
import com.yellowforktech.littlefamilytree.util.ImageHelper;

import java.util.ArrayList;
import java.util.Arrays;
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
    public static final String TOPIC_CHOOSE_SONG1 = "chooseSong1";
    public static final String TOPIC_CHOOSE_SONG2 = "chooseSong2";
    public static final String TOPIC_CHOOSE_SONG3 = "chooseSong3";

    private LittlePerson player;
    private List<LittlePerson> family;
    private List<LittlePerson> finishedPeople;
    private DisplayMetrics dm;
    private boolean spritesCreated = false;
    private List<DraggablePersonSprite> peopleSprites;

    private SongActivity activity;
    private float clipY = 0;
    private int maxHeight;
    private Bitmap stage;
	
	private AnimatedBitmapSprite selPerson1;
	private AnimatedBitmapSprite selPerson2;
	private AnimatedBitmapSprite selPerson3;
	private AnimatedBitmapSprite selPerson4;
	
	private TouchStateAnimatedBitmapSprite drumKit;
	private TouchStateAnimatedBitmapSprite gPiano;
	private TouchStateAnimatedBitmapSprite violin;
	private TouchStateAnimatedBitmapSprite clarinet;
    private TouchStateAnimatedBitmapSprite guitar;
    private TouchStateAnimatedBitmapSprite bass;
	
	private TouchStateAnimatedBitmapSprite playButton;
    private TouchStateAnimatedBitmapSprite resetButton;

    private TouchEventGameSprite song1Button;
    private TouchEventGameSprite song2Button;
    private TouchEventGameSprite song3Button;
    private TouchEventGameSprite growButton;
	
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
    protected MediaPlayer voicePlayer;
	protected boolean pianoOn = true;
	protected boolean drumsOn = true;
	protected boolean fluteOn = true;
	protected boolean violinOn = true;

    private boolean inSeats = false;
    private boolean playing = false;

    private SongAlbum album;
    private Song song;

    private long lastSongTime = 0;
    private long currentSongTime = 0;
    private int dancer = 0;
    private int nextPerson = 0;
    private int speakPerson = 0;
    private int wordNumber = 0;
    private int wordChange = 0;
    private int textSpriteNum = 0;
    private List<String> words;
    private List<TextSprite> textSprites;
    private Paint textPaint;
    private Paint textHiPaint;
    private float xOffset;
    private boolean songReady = false;

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
		
		textPaint = new Paint();
		textPaint.setColor(Color.BLACK);
		textPaint.setTextSize(40);

        textSprites = new ArrayList<>();
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
        EventQueue.getInstance().unSubscribe(TOPIC_CHOOSE_SONG1, this);
        EventQueue.getInstance().unSubscribe(TOPIC_CHOOSE_SONG2, this);
        EventQueue.getInstance().unSubscribe(TOPIC_CHOOSE_SONG3, this);
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
        EventQueue.getInstance().subscribe(TOPIC_CHOOSE_SONG1, this);
        EventQueue.getInstance().subscribe(TOPIC_CHOOSE_SONG2, this);
        EventQueue.getInstance().subscribe(TOPIC_CHOOSE_SONG3, this);

        album = new SongAlbum(player, activity);
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

    public LittlePerson getPlayer() {
        return player;
    }

    public void setPlayer(LittlePerson player) {
        this.player = player;
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
            currentSongTime = System.currentTimeMillis() - lastSongTime;

            if (wordNumber >= wordChange) {
                synchronized (sprites) {
                    for (TextSprite ts : textSprites) {
                        sprites.remove(ts);
                    }
                    textSprites.clear();
                    float tx = xOffset + 10 * dm.density;
                    float ty = playButton.getY() + playButton.getHeight()*2 + 20*dm.density;
                    int replaceCount = 0;
                    for (int w = wordChange; w < wordChange + 4; w++) {
                        if (w >= words.size()) break;
                        String word = words.get(w);
                        String origWord = word;
                        if (word.isEmpty()) continue;
                        if (word.startsWith("_")) {
                            if (replaceCount > 0) break;
                            DraggablePersonSprite ds = null;
                            if (nextPerson < onStage.size()) {
                                ds = onStage.get(nextPerson);
                            } else {
                                ds = onStage.get(onStage.size() - 1);
                            }
                            if (ds != null) {
                                LittlePerson person = ds.getPerson();
                                word = word.replace("_", song.getAttributor().getAttributeFromPerson(person, nextPerson));
                                Log.d("SongSpriteSurfaceView", "word "+word+" speakPerson="+speakPerson+" nextPerson="+nextPerson);
                                nextPerson++;
                            }
                            replaceCount++;
                        }
                        if (w>wordChange && word.startsWith("-")) {
                            tx -= 9 * dm.density;
                        }
                        Log.d("SongSpriteSurfaceView", word);
                        TextSprite ts = new TextSprite();
                        ts.setText(word);
                        ts.setX(tx);
                        ts.setY(ty);
                        ts.setHeight((int) (playButton.getHeight() * 0.6f));
                        if (w==wordChange) ts.setTextPaint(textHiPaint);
                        else ts.setTextPaint(textPaint);
                        Rect bounds = new Rect();
                        textPaint.getTextBounds(word, 0, word.length(), bounds);
                        ts.setWidth(bounds.width());
                        sprites.add(ts);
                        textSprites.add(ts);
                        tx += bounds.width() + 10 * dm.density;
                        if (tx > stage.getWidth()) {
                            if (textSprites.size()>1) {
                                tx -= bounds.width() + 10 * dm.density;
                                textSprites.remove(ts);
                                sprites.remove(ts);
                                if (origWord.startsWith("_")) {
                                    nextPerson--;
                                }
                            } else {
                                Paint smallerPaint = new Paint();
                                smallerPaint.setColor(Color.WHITE);
                                smallerPaint.setTextSize(playButton.getHeight() * 0.4f);
                                ts.setTextPaint(smallerPaint);
                            }
                            break;
                        }
                    }
                    wordChange += textSprites.size();
                    textSpriteNum = 0;

                    float xdiff = (stage.getWidth() - tx)/2;
                    if (textSprites.size() > 0 && textSprites.get(0).getX() + xdiff > 0) {
                        for (TextSprite ts : textSprites) {
                            ts.setX(ts.getX() + xdiff);
                        }
                    }
                }
            }

            if (wordNumber < song.getWordTimings().size() && currentSongTime > song.getWordTimings().get(wordNumber)) {
                if (textSpriteNum < textSprites.size()) {
                    textSprites.get(textSpriteNum).setTextPaint(textPaint);
                }
                wordNumber++;
                textSpriteNum++;
                if (textSpriteNum < textSprites.size()) {
                    textSprites.get(textSpriteNum).setTextPaint(textHiPaint);
                }

                if (wordNumber < words.size()) {
                    String word = words.get(wordNumber);
                    if (word.startsWith("_")) {
                        if (speakPerson < onStage.size()) {
                            DraggablePersonSprite ds = onStage.get(speakPerson);
                            LittlePerson person = ds.getPerson();
							String attr = song.getAttributor().getAttributeFromPerson(person, speakPerson);
							if (attr.matches("\\d[1-9]\\d{2}") ) {
								attr = attr.substring(0,2) + " " + attr.substring(2);
							}
                            activity.speak(attr);
                            speakPerson++;
                            Log.d("SongSpriteSurfaceView", "speak speakPerson="+speakPerson+" nextPerson="+nextPerson);
                        }
                    }
                }
            }

            if (onStage.size() > 0) {
                if (currentSongTime > song.getDanceTimings().get(0) && currentSongTime < song.getDanceTimings().get(0) + 500) {
                    DraggablePersonSprite ds = onStage.get(0);
                    dancer = 0;
                    ds.setTargetX((int) (xOffset + stage.getWidth() / 2 - (int) (ds.getHeight() * 1.25 / 2.0)));
                    ds.setTargetHeight((int) (ds.getHeight() * 1.25));
                    ds.setTargetY((int) (selPerson1.getY() + selPerson1.getHeight()));
                    ds.setMoving(true);
                }

                if (currentSongTime > song.getDanceTimings().get(1) && currentSongTime < song.getDanceTimings().get(1) + 500) {
                    DraggablePersonSprite ds = onStage.get(0);
                    ds.setTargetX((int) selPerson1.getX());
                    ds.setTargetHeight(womanHeight);
                    ds.setTargetY((int) (selPerson1.getY()));
                    ds.setMoving(true);

                    ds = onStage.get(1);
                    dancer = 1;
                    ds.setTargetX((int) (xOffset + stage.getWidth() / 2 - (int) (ds.getHeight() * 1.25 / 2.0)));
                    ds.setTargetHeight((int) (ds.getHeight() * 1.25));
                    ds.setTargetY((int) (selPerson1.getY() + selPerson1.getHeight()));
                    ds.setMoving(true);
                }

                if (currentSongTime > song.getDanceTimings().get(2) && currentSongTime < song.getDanceTimings().get(2) + 500) {
                    DraggablePersonSprite ds = onStage.get(1);
                    ds.setTargetX((int) selPerson2.getX());
                    ds.setTargetHeight(womanHeight);
                    ds.setTargetY((int) (selPerson2.getY()));
                    ds.setMoving(true);

                    ds = onStage.get(2);
                    dancer = 2;
                    ds.setTargetX((int) (xOffset + stage.getWidth() / 2 - (int) (ds.getHeight() * 1.25 / 2.0)));
                    ds.setTargetHeight((int) (ds.getHeight() * 1.25));
                    ds.setTargetY((int) (selPerson1.getY() + selPerson1.getHeight()));
                    ds.setMoving(true);
                }

                if (currentSongTime > song.getDanceTimings().get(3) && currentSongTime < song.getDanceTimings().get(3) + 500) {
                    DraggablePersonSprite ds = onStage.get(2);
                    ds.setTargetX((int) selPerson3.getX());
                    ds.setTargetHeight(womanHeight);
                    ds.setTargetY((int) (selPerson3.getY()));
                    ds.setMoving(true);

                    ds = onStage.get(3);
                    dancer = 3;
                    ds.setTargetX((int) (xOffset + stage.getWidth() / 2 - (int) (ds.getHeight() * 1.25 / 2.0)));
                    ds.setTargetHeight((int) (ds.getHeight() * 1.25));
                    ds.setTargetY((int) (selPerson1.getY() + selPerson1.getHeight()));
                    ds.setMoving(true);
                }

                if (currentSongTime > song.getDanceTimings().get(4) && currentSongTime < song.getDanceTimings().get(4) + 500) {
                    DraggablePersonSprite ds = onStage.get(3);
                    ds.setTargetX((int) selPerson4.getX());
                    ds.setTargetHeight(womanHeight);
                    ds.setTargetY((int) (selPerson4.getY()));
                    ds.setMoving(true);
                }
                if (currentSongTime > song.getDanceTimings().get(5)) {
                    for (DraggablePersonSprite ds : onStage) {
                        LittlePerson p = ds.getPerson();
                        family.remove(p);
                        finishedPeople.add(p);
                        ds.setTargetX((int) (-100 * dm.density));
                        ds.setMoving(true);
                    }
                }
                if (currentSongTime > song.getDanceTimings().get(6)) {
                    song = album.nextSong();
                    resetSong();
                    synchronized (sprites) {
                        onStage.clear();
                        for (TextSprite ts : textSprites) {
                            sprites.remove(ts);
                        }
                        textSprites.clear();

                    }
                    showSongButtons();
                    if (family.size() < 8) activity.loadMorePeople();
                    reorderPeople();
                }
            }

        }

        rotation += rotateDir;
        if (rotation > 10) {
            rotateDir = -1;
        }
        if (!songReady && rotation==0 && rotateDir==-1) {
            if (growButton==song1Button) growButton = song2Button;
            else if (growButton==song2Button) growButton = song3Button;
            else growButton = song1Button;
            Rect sr = new Rect((int)growButton.getX(), (int)growButton.getY(), (int)(growButton.getX()+growButton.getWidth()),(int)(growButton.getY()+growButton.getHeight()));
            addStars(sr, false, 2);
        }
        if (rotation < -10) {
            rotateDir = 1;
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
            textSprites.clear();


            maxHeight = this.getHeight();
            float stageWidth = Math.min(this.getWidth(), this.getHeight());

            int width = (int) (stageWidth * 0.17f);
            if (width > 250) width = 250;

            stage = ImageHelper.loadBitmapFromResource(context, R.drawable.stage, 0, (int) (stageWidth - (10*dm.density + width)), getHeight());

            xOffset = (this.getWidth() - (width + stage.getWidth()))/2f;

            manWidth = stage.getWidth() / 7;
            womanWidth = (int) (manWidth + 4 * dm.density);

            Bitmap song1Bm = ImageHelper.loadBitmapFromResource(activity, R.drawable.song1, 0, (int) (width * 1.7), (int) (width * 1.7));
            song1Button = new TouchEventGameSprite(song1Bm, TOPIC_CHOOSE_SONG1, dm);
            song1Button.setX(xOffset + 15 * dm.density);
            song1Button.setY(stage.getHeight() - (50 * dm.density + song1Bm.getHeight()));
            addSprite(song1Button);

            Bitmap song2Bm = ImageHelper.loadBitmapFromResource(activity, R.drawable.song2, 0, (int) (width * 1.7), (int) (width * 1.7));
            song2Button = new TouchEventGameSprite(song2Bm, TOPIC_CHOOSE_SONG2, dm);
            song2Button.setX(xOffset + (stage.getWidth() / 2) - (song2Bm.getWidth() / 2) + 5 * dm.density);
            song2Button.setY(stage.getHeight() - (20 * dm.density + song1Bm.getHeight()));
            addSprite(song2Button);

            Bitmap song3Bm = ImageHelper.loadBitmapFromResource(activity, R.drawable.song3, 0, (int) (width * 1.7), (int) (width * 1.7));
            song3Button = new TouchEventGameSprite(song3Bm, TOPIC_CHOOSE_SONG3, dm);
            song3Button.setX(xOffset + stage.getWidth() - (song3Bm.getWidth() + 15 * dm.density));
            song3Button.setY(stage.getHeight() - (55 * dm.density + song1Bm.getHeight()));
            addSprite(song3Button);

            Bitmap drumsBm = ImageHelper.loadBitmapFromResource(activity, R.drawable.drums, 0, (int) (width * 1.6), (int) (width * 1.6));
            drumKit = new TouchStateAnimatedBitmapSprite(drumsBm, activity);
            drumKit.setX(xOffset + 10 * dm.density);
            drumKit.setY(stage.getHeight() - (55 * dm.density + drumsBm.getHeight()));
            drumKit.setResources(getResources());
            drumKit.setStateTransitionEvent(0, TOPIC_TOGGLE_DRUMS);
            drumKit.setStateTransitionEvent(1, TOPIC_TOGGLE_DRUMS);
            drumKit.addBitmap(1, ImageHelper.loadBitmapFromResource(activity, R.drawable.drums_off, 0, (int) (width * 1.6), (int) (width * 1.6)));
            //addSprite(drumKit);

            Bitmap gPianoBm = ImageHelper.loadBitmapFromResource(activity, R.drawable.piano, 0, (int) (width * 1.6), (int) (width * 1.6));
            gPiano = new TouchStateAnimatedBitmapSprite(gPianoBm, activity);
            gPiano.setX(xOffset + stage.getWidth() - (15 * dm.density + gPianoBm.getWidth()));
            gPiano.setY(stage.getHeight() - (35 * dm.density + gPianoBm.getHeight()));
            gPiano.setResources(getResources());
            gPiano.setStateTransitionEvent(0, TOPIC_TOGGLE_PIANO);
            gPiano.setStateTransitionEvent(1, TOPIC_TOGGLE_PIANO);
            gPiano.addBitmap(1, ImageHelper.loadBitmapFromResource(activity, R.drawable.piano_off, 0, (int) (width * 1.6), (int) (width * 1.6)));
            //addSprite(gPiano);

            Bitmap violinBm = ImageHelper.loadBitmapFromResource(activity, R.drawable.violin, 0, (int) (width * 1.6), (int) (width * 1.6));
            violin = new TouchStateAnimatedBitmapSprite(violinBm, activity);
            violin.setX(xOffset + stage.getWidth() / 2 - violinBm.getWidth() / 4f);
            violin.setY(stage.getHeight() - (violinBm.getHeight()));
            violin.setResources(getResources());
            violin.setStateTransitionEvent(0, TOPIC_TOGGLE_VIOLIN);
            violin.setStateTransitionEvent(1, TOPIC_TOGGLE_VIOLIN);
            violin.addBitmap(1, ImageHelper.loadBitmapFromResource(activity, R.drawable.violin_off, 0, (int) (width * 1.6), (int) (width * 1.6)));
            //addSprite(violin);

            Bitmap bassBm = ImageHelper.loadBitmapFromResource(activity, R.drawable.bass, 0, (int) (width * 1.8), (int) (width * 1.8));
            bass = new TouchStateAnimatedBitmapSprite(bassBm, activity);
            bass.setX(xOffset + stage.getWidth() / 2 - (bassBm.getWidth()*1.25f));
            bass.setY(stage.getHeight() - (20 * dm.density + bassBm.getHeight()));
            bass.setResources(getResources());
            bass.setStateTransitionEvent(0, TOPIC_TOGGLE_FLUTE);
            bass.setStateTransitionEvent(1, TOPIC_TOGGLE_FLUTE);
            bass.setIgnoreAlpha(true);
            bass.addBitmap(1, ImageHelper.loadBitmapFromResource(activity, R.drawable.bass_off, 0, (int) (width * 1.8), (int) (width * 1.8)));
            //addSprite(bass);

            Bitmap clarinetBm = ImageHelper.loadBitmapFromResource(activity, R.drawable.clarinet, 0, (int) (width * 1.4), (int) (width * 1.4));
            clarinet = new TouchStateAnimatedBitmapSprite(clarinetBm, activity);
            clarinet.setX(xOffset + stage.getWidth() / 2 - clarinetBm.getWidth());
            clarinet.setY(stage.getHeight() - (20 * dm.density + clarinetBm.getHeight()));
            clarinet.setResources(getResources());
            clarinet.setStateTransitionEvent(0, TOPIC_TOGGLE_FLUTE);
            clarinet.setStateTransitionEvent(1, TOPIC_TOGGLE_FLUTE);
            clarinet.setIgnoreAlpha(true);
            clarinet.addBitmap(1, ImageHelper.loadBitmapFromResource(activity, R.drawable.clarinet_off, 0, (int) (width * 1.4), (int) (width * 1.4)));
            //addSprite(clarinet);

            Bitmap guitarBm = ImageHelper.loadBitmapFromResource(activity, R.drawable.guitar, 0, (int) (width * 1.6), (int) (width * 1.6));
            guitar = new TouchStateAnimatedBitmapSprite(guitarBm, activity);
            guitar.setX(xOffset + stage.getWidth() / 2);
            guitar.setY(stage.getHeight() - (20 * dm.density + guitarBm.getHeight()));
            guitar.setResources(getResources());
            guitar.setStateTransitionEvent(0, TOPIC_TOGGLE_VIOLIN);
            guitar.setStateTransitionEvent(1, TOPIC_TOGGLE_VIOLIN);
            guitar.setIgnoreAlpha(true);
            guitar.addBitmap(1, ImageHelper.loadBitmapFromResource(activity, R.drawable.guitar_off, 0, (int) (width * 1.6), (int) (width * 1.6)));
            //addSprite(guitar);

            Bitmap man = ImageHelper.loadBitmapFromResource(activity, R.drawable.man_silhouette, 0,
                    manWidth, manWidth);
            selPerson1 = new AnimatedBitmapSprite(man);
            selPerson1.setX(xOffset + width);
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
            dropRect.set(0, (int) ((int) selPerson1.getY() - (70 * dm.density)), stage.getWidth(), (int) ((int) selPerson4.getY() + selPerson4.getHeight() + (70 * dm.density)));

            if (family != null) {
                float x = xOffset + stage.getWidth() + 10 * dm.density;
                float y = 10 * dm.density;
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
                    sprite.setThresholdX((int) (8 * dm.density));
                    peopleSprites.add(sprite);
                    y = y + sprite.getHeight() + (10 * dm.density);
                    if (y > maxHeight) maxHeight = (int) y;
                }

                for (DraggablePersonSprite s : peopleSprites) {
                    s.setMaxHeight(maxHeight);
                }
            }

            Bitmap play = ImageHelper.loadBitmapFromResource(context, android.R.drawable.ic_media_play, 0, width, width);
            Bitmap pause = ImageHelper.loadBitmapFromResource(context, android.R.drawable.ic_media_pause, 0, width, width);
            playButton = new TouchStateAnimatedBitmapSprite(play, context);
            List<Bitmap> pauseList = new ArrayList<>(1);
            pauseList.add(pause);
            playButton.getBitmaps().put(1, pauseList);
            playButton.setX(xOffset + stage.getWidth() / 2 - width);
            playButton.setY(50 * dm.density);
            playButton.setStateTransitionEvent(0, TOPIC_PLAY_SONG);
            playButton.setStateTransitionEvent(1, TOPIC_PLAY_SONG);
            playButton.setIgnoreAlpha(true);
            //addSprite(playButton);

            Bitmap reset = ImageHelper.loadBitmapFromResource(context, android.R.drawable.ic_menu_revert, 0, width, width);
            resetButton = new TouchStateAnimatedBitmapSprite(reset, context);
            resetButton.setX(xOffset + stage.getWidth() / 2);
            resetButton.setY(50 * dm.density);
            resetButton.setStateTransitionEvent(0, TOPIC_PLAY_RESET);
            resetButton.setIgnoreAlpha(true);
            //addSprite(resetButton);

            textPaint = new Paint();
            textPaint.setColor(Color.WHITE);
            textPaint.setTextSize(playButton.getHeight() * 0.5f);

            textHiPaint = new Paint();
            textHiPaint.setColor(Color.YELLOW);
            textHiPaint.setTextSize(playButton.getHeight() * 0.5f);

            spritesCreated = true;
        }
    }
	
	private void reorderPeople() {
        synchronized (sprites) {
            float x = xOffset + stage.getWidth() + 10 * dm.density;
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
        super.touch_start(x, y);

        if (!playing) {
            for (int i = peopleSprites.size() - 1; i >= 0; i--) {
                Sprite s = peopleSprites.get(i);
                if (s.inSprite(x, y + clipY)) {
                    selectedSprites.add(s);
                    s.onSelect(x, y + clipY);
                    if (!multiSelect) break;
                }
            }

            for (int i = onStage.size() - 1; i >= 0; i--) {
                Sprite s = onStage.get(i);
                if (s.inSprite(x, y)) {
                    selectedSprites.add(s);
                    s.onSelect(x, y);
                    if (!multiSelect) break;
                }
            }
        }
        if (x > getWidth() * 0.82f) {
            inSeats = true;
        } else {
            inSeats = false;
        }
    }

    @Override
    public void doMove(float oldX, float oldY, float newX, float newY) {
        //super.doMove(oldX, oldY+clipY, newX, newY+clipY);

        boolean selectedMoved = false;
        if (selectedSprites.size() > 0) {
            for (Sprite s : selectedSprites) {
                boolean moved = false;
                if (s instanceof DraggablePersonSprite) {
                    moved = s.onMove(oldX, oldY + clipY, newX, newY + clipY);
                    selectedMoved |= moved;
                } else {
                    moved = s.onMove(oldX, oldY, newX, newY);
                    selectedMoved |= moved;
                }
                dropReady = false;
                replaceReady = false;
				if (s instanceof DraggablePersonSprite) {
					DraggablePersonSprite ds = (DraggablePersonSprite) s;
                    if (!onStage.contains(ds)) {
                        if (moved && onStage.size()<4 && dropRect.contains((int) s.getX(), (int) (s.getY() - clipY))) {
                            if (songReady) dropReady = true;
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
        if (!selectedMoved && inSeats) {
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
                        activity.speak(ds.getPerson().getGivenName());
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
        inSeats = false;

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
            canvas.drawBitmap(stage, xOffset, 5, null);

            for (Sprite s : sprites) {
                if (s.getX() + s.getWidth() >= 0 && s.getX() <= getWidth() && s.getY() + s.getHeight() >= 0 && s.getY() <= getHeight()) {
                    if (s==growButton) {
                        canvas.save();
                        float sc = rotation;
                        canvas.scale(1f + sc / 2000f, 1f + sc / 2000f);
                    }
                    s.doDraw(canvas);
                    if (s==growButton) {
                        canvas.restore();
                    }
                }
            }
			
			for (Sprite s : onStage) {
                if (s.getX() + s.getWidth() >= 0 && s.getX() <= getWidth() && s.getY() + s.getHeight() >= 0 && s.getY() <= getHeight()) {
                    canvas.save();
                    if (playing) canvas.rotate(rotation, s.getX()+s.getWidth()/2, s.getY()+s.getHeight()/2);
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
        //canvas.drawText(String.valueOf(currentSongTime), 0, getHeight() - 50, textPaint);
    }
	
	public void resetSong() {
		playing = false;
        dancer = 0;
        nextPerson = 0;
        wordNumber = 0;
        wordChange = 0;
        currentSongTime = 0;
        if (pianoPlayer!=null) pianoPlayer.release();
		pianoPlayer = MediaPlayer.create(context, song.getPianoTrack());
        if (drumsPlayer!=null) drumsPlayer.release();
		drumsPlayer = MediaPlayer.create(context, song.getDrumTrack());
        if (flutePlayer!=null) flutePlayer.release();
		flutePlayer = MediaPlayer.create(context, song.getFluteTrack());
        if (violinPlayer!=null) violinPlayer.release();
		violinPlayer = MediaPlayer.create(context, song.getViolinTrack());
        if (voicePlayer!=null) voicePlayer.release();
        voicePlayer = MediaPlayer.create(context, song.getVoiceTrack());
		if (!fluteOn) {
			flutePlayer.setVolume(0,0);
		} else {
			flutePlayer.setVolume(1f,1f);
		}
		if (!drumsOn) {
			drumsPlayer.setVolume(0,0);
		} else {
			drumsPlayer.setVolume(1f,1f);
		}
		if (!violinOn) {
			violinPlayer.setVolume(0,0);
		} else {
			violinPlayer.setVolume(1f,1f);
		}
		if (!pianoOn) {
			pianoPlayer.setVolume(0,0);
		} else {
			pianoPlayer.setVolume(1f,1f);
		}
		playButton.setState(0);
        synchronized (sprites) {
            if (speakPerson < 4) {
                for (DraggablePersonSprite ds : onStage) {
                    peopleSprites.add(0, ds);
                }
            }
            onStage.clear();
            reorderPeople();
            sprites.removeAll(textSprites);
            textSprites.clear();
        }
        words = Arrays.asList(song.getWords().split("\\s+"));
        speakPerson = 0;
	}

    public void showInstruments() {
        synchronized (sprites) {
            activity.speak(context.getString(R.string.choose_dancers));
            sprites.remove(song1Button);
            sprites.remove(song2Button);
            sprites.remove(song3Button);

            for(InstrumentType type : song.getInstruments()) {
                switch (type) {
                    case Drums:
                        sprites.add(drumKit);
                        drumKit.setState(drumsOn?0:1);
                        break;
                    case Flute:
                        sprites.add(clarinet);
                        clarinet.setState(fluteOn ? 0 : 1);
                        break;
                    case Violin:
                        sprites.add(violin);
                        violin.setState(violinOn ? 0 : 1);
                        break;
                    case Piano:
                        sprites.add(gPiano);
                        gPiano.setState(pianoOn ? 0 : 1);
                        break;
                    case Bass:
                        sprites.add(bass);
                        bass.setState(fluteOn ? 0 : 1);
                        break;
                    case Guitar:
                        sprites.add(guitar);
                        guitar.setState(violinOn ? 0 : 1);
                        break;
                    default:
                        //-- do nothing
                        break;
                }
            }

            sprites.add(playButton);
            sprites.add(resetButton);
            songReady = true;
        }
    }

    public void showSongButtons() {
        synchronized (sprites) {
            activity.speak(context.getString(R.string.choose_a_song));
            sprites.add(song1Button);
            sprites.add(song2Button);
            sprites.add(song3Button);

            sprites.remove(drumKit);
            sprites.remove(clarinet);
            sprites.remove(violin);
            sprites.remove(gPiano);
            sprites.remove(guitar);
            sprites.remove(bass);

            sprites.remove(playButton);
            sprites.remove(resetButton);
            songReady = false;
        }
    }

    @Override
    public void onEvent(String topic, Object o) {
        if (TOPIC_PERSON_TOUCHED.equals(topic)) {
            TouchEventGameSprite sprite = (TouchEventGameSprite) o;
            LittlePerson person = (LittlePerson) sprite.getData("person");
            if (person!=null) {
                activity.speak(person.getGivenName());
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
                lastSongTime = System.currentTimeMillis() - currentSongTime;
                Boolean quietMode = PreferenceManager.getDefaultSharedPreferences(activity).getBoolean("quiet_mode", false);
                if (!quietMode) {
                    pianoPlayer.start();
                    drumsPlayer.start();
                    flutePlayer.start();
                    violinPlayer.start();
                    voicePlayer.start();
                }
            }
            else {
                pianoPlayer.pause();
                drumsPlayer.pause();
                flutePlayer.pause();
                violinPlayer.pause();
                voicePlayer.pause();
                playing = false;
            }
        }
        else if (topic.equals(TOPIC_PLAY_RESET)) {
            resetSong();
			//createSprites();

        }
		else if (topic.equals(TOPIC_TOGGLE_FLUTE)) {
			fluteOn = !fluteOn;
			if (!fluteOn) {
				flutePlayer.setVolume(0,0);
			} else {
				flutePlayer.setVolume(1f,1f);
			}
		}
		else if (topic.equals(TOPIC_TOGGLE_VIOLIN)) {
			violinOn = !violinOn;
			if (!violinOn) {
				violinPlayer.setVolume(0,0);
			} else {
				violinPlayer.setVolume(1f,1f);
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
        else if (topic.equalsIgnoreCase(TOPIC_CHOOSE_SONG1)) {
            song = album.getSongs().get(0);
            showInstruments();
            resetSong();
        }
        else if (topic.equalsIgnoreCase(TOPIC_CHOOSE_SONG2)) {
            song = album.getSongs().get(1);
            showInstruments();
            resetSong();
        }
        else if (topic.equalsIgnoreCase(TOPIC_CHOOSE_SONG3)) {
            song = album.getSongs().get(2);
            showInstruments();
            resetSong();
        }
    }
}
