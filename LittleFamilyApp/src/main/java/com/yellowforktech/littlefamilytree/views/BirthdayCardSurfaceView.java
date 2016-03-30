package com.yellowforktech.littlefamilytree.views;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;

import com.yellowforktech.littlefamilytree.R;
import com.yellowforktech.littlefamilytree.activities.BirthdayCardActivity;
import com.yellowforktech.littlefamilytree.activities.LittleFamilyActivity;
import com.yellowforktech.littlefamilytree.activities.tasks.FamilyLoaderTask;
import com.yellowforktech.littlefamilytree.data.DataService;
import com.yellowforktech.littlefamilytree.data.LittlePerson;
import com.yellowforktech.littlefamilytree.events.EventListener;
import com.yellowforktech.littlefamilytree.sprites.AnimatedBitmapSprite;
import com.yellowforktech.littlefamilytree.sprites.CupcakeSprite;
import com.yellowforktech.littlefamilytree.sprites.DraggablePersonSprite;
import com.yellowforktech.littlefamilytree.sprites.DraggableSprite;
import com.yellowforktech.littlefamilytree.sprites.Sprite;
import com.yellowforktech.littlefamilytree.util.ImageHelper;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * Created by jfinlay on 3/21/2016.
 */
public class BirthdayCardSurfaceView extends SpritedSurfaceView implements EventListener {

    private LittleFamilyActivity activity;

    private List<LittlePerson> birthdayPeople;
    private LittlePerson birthdayPerson;

    private List<DraggableSprite> stickerSprites;
    private List<DraggableSprite> onMirror;

    private float xOffset;
    private float yOffset;

    private boolean portrait = true;
    private DisplayMetrics dm;
    private boolean spritesCreated = false;
    private boolean peopleCreated = false;
    private boolean cupcakesCreated = false;

    private List<Bitmap> cupcakeBitmaps;

    private Bitmap vanityTop;
    private Bitmap vanityBottom;

    private Rect wordsRect;
    private Rect heartsRect;
    private Rect cakesRect;
    private Rect balloonsRect;
    private Rect confettiRect;
    private Rect peopleRect;
    private Rect hatsRect;

    private boolean moved = false;

    private float clipY = 0;
    private float maxHeight = 0;

    private Map<String, List<String>> stickerMap;

    public BirthdayCardSurfaceView(Context context) {
        super(context);
        setup();
    }

    public BirthdayCardSurfaceView(Context context, AttributeSet attrs) {
        super(context, attrs);
        setup();
    }

    private void setup() {
        stickerSprites = new ArrayList<>();
        onMirror = new ArrayList<>();

        stickerMap = new HashMap<>();
        List<String> hearts = new ArrayList<>(6);
        hearts.add("heart1.png");
        hearts.add("heart2.png");
        hearts.add("heart3.png");
        hearts.add("heart4.png");
        hearts.add("heart5.png");
        hearts.add("heart6.png");
        stickerMap.put("hearts", hearts);
        List<String> hats = new ArrayList<>(6);
        hats.add("hat1.png");
        hats.add("hat2.png");
        hats.add("hat3.png");
        hats.add("hat4.png");
        hats.add("hat5.png");
        hats.add("hat6.png");
        stickerMap.put("hats", hats);
        List<String> balloons = new ArrayList<>(6);
        balloons.add("balloons1.png");
        balloons.add("balloons2.png");
        balloons.add("balloons3.png");
        balloons.add("balloons4.png");
        stickerMap.put("balloons", balloons);
        List<String> cakes = new ArrayList<>(6);
        cakes.add("cake1.png");
        cakes.add("cake2.png");
        cakes.add("cake3.png");
        cakes.add("cake4.png");
        cakes.add("cake5.png");
        cakes.add("cake6.png");
        stickerMap.put("cakes", cakes);
        List<String> confetti = new ArrayList<>(6);
        confetti.add("confetti1.png");
        confetti.add("confetti2.png");
        confetti.add("confetti3.png");
        confetti.add("confetti4.png");
        confetti.add("confetti5.png");
        stickerMap.put("confetti", confetti);
        List<String> words = new ArrayList<>(6);
        words.add("word1.png");
        words.add("word2.png");
        words.add("word3.png");
        words.add("word4.png");
        words.add("word5.png");
        stickerMap.put("words", words);

        cupcakeBitmaps = new ArrayList<>(4);
        cupcakeBitmaps.add(BitmapFactory.decodeResource(getResources(), R.drawable.cupcake1));
        cupcakeBitmaps.add(BitmapFactory.decodeResource(getResources(), R.drawable.cupcake2));
        cupcakeBitmaps.add(BitmapFactory.decodeResource(getResources(), R.drawable.cupcake3));
        cupcakeBitmaps.add(BitmapFactory.decodeResource(getResources(), R.drawable.cupcake4));
    }

    public LittleFamilyActivity getActivity() {
        return activity;
    }

    public void setActivity(LittleFamilyActivity activity) {
        this.activity = activity;
        dm = new DisplayMetrics();
        activity.getWindowManager().getDefaultDisplay().getMetrics(dm);
    }

    public LittlePerson getBirthdayPerson() {
        return birthdayPerson;
    }

    public void setBirthdayPerson(LittlePerson birthdayPerson) {
        this.birthdayPerson = birthdayPerson;
    }

    public List<LittlePerson> getBirthdayPeople() {
        return birthdayPeople;
    }

    public void setBirthdayPeople(List<LittlePerson> birthdayPeople) {
        this.birthdayPeople = birthdayPeople;
    }

    @Override
    public void onEvent(String topic, Object o) {

    }

    @Override
    public void doStep() {
        super.doStep();

        synchronized (sprites) {
            try {
                Iterator<DraggableSprite> i = stickerSprites.iterator();
                while (i.hasNext()) {
                    Sprite s = i.next();
                    s.doStep();
                }
            } catch(Exception e) {
                Log.e("BirthdayCardSurfaceView", "error stepping sprites", e);
                return;
            }
        }
    }

    public void createCupcakes() {
        synchronized (sprites) {
            if (birthdayPeople != null) {
                int width = (int) ((Math.min(this.getWidth(), this.getHeight()) / 3) - 10*dm.density);
                Random rand = new Random();
                float x = 10 * dm.density;
                float y = 10 * dm.density;
                for(LittlePerson person : birthdayPeople) {
                    int i = rand.nextInt(cupcakeBitmaps.size());
                    Bitmap cupcakebm = cupcakeBitmaps.get(i);
                    float ratio = ((float)cupcakebm.getWidth()) / (float)(cupcakebm.getHeight());
                    CupcakeSprite cs = new CupcakeSprite(cupcakebm, person, activity, BirthdayCardActivity.TOPIC_BIRTHDAY_PERSON_SELECTED, dm);
                    cs.setWidth(width);
                    cs.setHeight((int) (width / ratio));
                    cs.setX(x);
                    cs.setY(y);
                    addSprite(cs);

                    x += cs.getWidth() + 10 * dm.density;
                    if (x > getWidth()) {
                        x = 10 * dm.density;
                        y = y + cs.getHeight() + 10 * dm.density;
                    }
                }
                maxHeight = y;

                cupcakesCreated = true;
            }
        }
    }

    public void createSprites() {
        synchronized (sprites) {
            sprites.clear();
            clipY = 0;

            if (this.getWidth() > this.getHeight()) {
                portrait = false;
            }

            float width = Math.min(this.getWidth(), this.getHeight());

            float vanityWidth = width;
            float vanityHeight = getHeight() / 2f;
            if (!portrait) {
                vanityHeight = width;
                vanityWidth = this.getWidth() / 2f;
            }

            vanityTop = ImageHelper.loadBitmapFromResource(context, R.drawable.vanity_top, 0, (int) (vanityWidth * .885f), (int)vanityHeight);
            vanityBottom = ImageHelper.loadBitmapFromResource(context, R.drawable.vanity_bottom, 0, (int) (vanityWidth), (int)vanityHeight);

            xOffset = (this.getWidth() - vanityBottom.getWidth()) / 2;
            yOffset = 10;

            float topOffset = xOffset + (vanityBottom.getWidth() - vanityTop.getWidth()) / 2;

            wordsRect = new Rect();
            wordsRect.set((int)topOffset, (int)(yOffset + 20*dm.density), (int)(topOffset+90*dm.density), (int)(yOffset + 90*dm.density));
            heartsRect = new Rect();
            heartsRect.set((int)topOffset, (int)(yOffset + (vanityTop.getHeight() / 2) - 16*dm.density), (int)(topOffset+70*dm.density), (int)(yOffset + (vanityTop.getHeight() / 2) + 37*dm.density));
            cakesRect = new Rect();
            balloonsRect = new Rect();
            confettiRect = new Rect();
            peopleRect = new Rect();
            peopleRect.set((int)(xOffset + (vanityTop.getWidth() / 2) + 100*dm.density), (int)(yOffset + (vanityTop.getHeight() / 2) - 16*dm.density),
                    (int)(topOffset+vanityTop.getWidth()), (int)(yOffset + (vanityTop.getHeight() / 2) + 37*dm.density));
            hatsRect = new Rect();

            spritesCreated = true;
        }
    }

    public void createPeople() {
        synchronized (sprites) {
            if (activity!=null && activity.getSelectedPerson()!=null && birthdayPerson!=null) {
                int width = vanityTop.getWidth() / 13;
                clipY = 0;

                Bitmap birthdayPhoto = null;
                if (birthdayPerson.getPhotoPath() != null) {
                    birthdayPhoto = ImageHelper.loadBitmapFromFile(birthdayPerson.getPhotoPath(), ImageHelper.getOrientation(birthdayPerson.getPhotoPath()), width, width, false);
                }
                if (birthdayPhoto == null) {
                    birthdayPhoto = ImageHelper.loadBitmapFromResource(activity, birthdayPerson.getDefaultPhotoResource(), 0, width, width);
                }
                AnimatedBitmapSprite birthdayPersonSprite = new AnimatedBitmapSprite(birthdayPhoto);
                birthdayPersonSprite.setX(xOffset + (vanityTop.getWidth() / 2) + 106*dm.density);
                birthdayPersonSprite.setY(yOffset + (vanityTop.getHeight() / 2) - 3*dm.density);
                sprites.add(birthdayPersonSprite);

                LittlePerson person = activity.getSelectedPerson();
                Bitmap photo = null;
                if (person.getPhotoPath() != null) {
                    photo = ImageHelper.loadBitmapFromFile(person.getPhotoPath(), ImageHelper.getOrientation(person.getPhotoPath()), width, width, false);
                }
                if (photo == null) {
                    photo = ImageHelper.loadBitmapFromResource(activity, person.getDefaultPhotoResource(), 0, width, width);
                }
                AnimatedBitmapSprite personSprite = new AnimatedBitmapSprite(photo);
                personSprite.setX(xOffset + (vanityTop.getWidth() / 2) + 106*dm.density + width + 3*dm.density);
                personSprite.setY(yOffset + (vanityTop.getHeight() / 2) - 3*dm.density);
                sprites.add(personSprite);

                peopleCreated = true;
            }
        }
    }

    public void showPeopleOnMirror(List<LittlePerson> people) {
        synchronized (sprites) {
            for(Sprite s : onMirror) {
                stickerSprites.remove(s);
            }
            onMirror.clear();

            int width = vanityTop.getWidth() / 8;
            clipY = 0;

            if (birthdayPerson!=null) {
                try {
                    int x = (int) (xOffset + (vanityTop.getWidth() / 2) - 35*dm.density);
                    int y = (int) (yOffset + 15 * dm.density);
                    for(LittlePerson p : people) {
                        Bitmap photo = null;
                        if (p.getPhotoPath() != null) {
                            photo = ImageHelper.loadBitmapFromFile(p.getPhotoPath(), ImageHelper.getOrientation(p.getPhotoPath()), width, width, false);
                        }
                        if (photo == null) {
                            photo = ImageHelper.loadBitmapFromResource(activity, p.getDefaultPhotoResource(), 0, width, width);
                        }
                        DraggablePersonSprite ds = new DraggablePersonSprite(photo, p, getWidth(), getHeight(), BirthdayCardActivity.TOPIC_PERSON_TOUCHED, dm);
                        ds.setX(xOffset + (vanityTop.getWidth() / 2) + 106 * dm.density);
                        ds.setY(yOffset + (vanityTop.getHeight() / 2) - 3 * dm.density);
                        ds.setWidth(vanityTop.getWidth() / 13);
                        ds.setHeight(vanityTop.getWidth() / 13);
                        ds.setTargetX(x);
                        ds.setTargetY(y);
                        ds.setTargetWidth(width);
                        ds.setTargetHeight(width);
                        ds.setMoving(true);
                        stickerSprites.add(ds);
                        onMirror.add(ds);

                        x += width + 10 * dm.density;
                        if (x > xOffset + (vanityTop.getWidth() / 2) + 90 * dm.density) {
                            x = (int) (xOffset + (vanityTop.getWidth() / 2) - 45*dm.density);
                            y += width + 10 * dm.density;
                        }
                        if (y > yOffset + vanityTop.getHeight() - 10 * dm.density) {
                            break;
                        }
                    }
                } catch (Exception e) {
                    Log.e("BirthdayCardSurfaceView", "Error getting family members", e);
                }
            }
        }
    }

    public void showStickersOnMirror(String type) {
        synchronized (sprites) {
            for (Sprite s : onMirror) {
                stickerSprites.remove(s);
            }
            onMirror.clear();

            int x = (int) (xOffset + (vanityTop.getWidth() / 2) - 95*dm.density);
            int y = (int) (yOffset + 10 * dm.density);
            clipY = 0;

            List<String> stickerFiles = stickerMap.get(type);
            for(String stickerFile : stickerFiles) {
                String filename = "stickers/"+type+"/"+stickerFile;
                InputStream cis = null;
                try {
                    cis = context.getAssets().open(filename);
                    Bitmap bm = BitmapFactory.decodeStream(cis);
                    DraggableSprite ds = new DraggableSprite(bm, getWidth(), getHeight(), null, dm);
                    ds.setX(xOffset + (vanityTop.getWidth() / 2) + 104 * dm.density);
                    ds.setY(yOffset + (vanityTop.getHeight() / 2) - 11 * dm.density);
                    ds.setWidth(vanityTop.getWidth() / 10);
                    ds.setHeight(vanityTop.getWidth() / 10);
                    ds.setTargetX(x);
                    ds.setTargetY(y);
                    ds.setTargetWidth(bm.getWidth());
                    ds.setTargetHeight(bm.getHeight());
                    ds.setMoving(true);
                    stickerSprites.add(ds);
                    onMirror.add(ds);

                    x += bm.getWidth() + 10 * dm.density;
                    if (x > xOffset + (vanityTop.getWidth() / 2) + 95 * dm.density) {
                        x = (int) (xOffset + (vanityTop.getWidth() / 2) - 95*dm.density);
                        y += bm.getHeight() + 10 * dm.density;
                    }
                    if (y > yOffset + vanityTop.getHeight() - 10 * dm.density) {
                        break;
                    }
                } catch (IOException e) {
                    Log.e("BirthdayCardSurfaceView", "Error loading sticker", e);
                }
            }
        }
    }

    @Override
    public void doDraw(Canvas canvas) {
        canvas.drawColor(0, android.graphics.PorterDuff.Mode.CLEAR);
        if (birthdayPerson == null) {
            if (!cupcakesCreated) {
                createCupcakes();
            }
        } else {
            if (!spritesCreated) {
                createSprites();
            }
            if (!peopleCreated) {
                createPeople();
            }
        }
        synchronized (sprites) {
            if (vanityTop != null) {
                canvas.drawBitmap(vanityTop, xOffset + 3*dm.density + (vanityBottom.getWidth() - vanityTop.getWidth()) / 2, yOffset, null);
            }
            if (vanityBottom != null) {
                canvas.drawBitmap(vanityBottom, xOffset, yOffset + vanityTop.getHeight() - 12, null);
            }

            canvas.translate(0, -clipY);
            for (Sprite s : sprites) {
                if (s.getX() + s.getWidth() >= 0 && s.getX() <= getWidth() && s.getY() + s.getHeight() >= clipY && s.getY() <= getHeight() + clipY) {
                    s.doDraw(canvas);
                }
            }

            for (Sprite s : stickerSprites) {
                if (s.getX() + s.getWidth() >= 0 && s.getX() <= getWidth() && s.getY() + s.getHeight() >= clipY && s.getY() <= getHeight() + clipY) {
                    s.doDraw(canvas);
                }
            }

            if (peopleRect != null) {
                Paint tempPaint = new Paint();
                tempPaint.setColor(Color.RED);
                tempPaint.setStyle(Paint.Style.STROKE);
                canvas.drawRect(peopleRect, tempPaint);
                canvas.drawRect(wordsRect, tempPaint);
                canvas.drawRect(heartsRect, tempPaint);
            }
        }
    }

    @Override
    protected void touch_start(float x, float y) {
        super.touch_start(x, y);

        moved = false;
    }

    @Override
    public void doMove(float oldX, float oldY, float newX, float newY) {
        super.doMove(oldX, oldY, newX, newY);
        if (Math.abs(newX - oldX) > 8*dm.density || Math.abs(newY - oldY) > 8*dm.density ) {
            moved = true;
        }
        if (birthdayPerson==null) {
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
        super.touch_up(x, y);
        if (!moved) {
            if (peopleRect != null) {
                if (peopleRect.contains((int) x, (int) y)) {
                    startBirthdayPeopleTask();
                } else if (wordsRect.contains((int) x, (int) y)) {
                    showStickersOnMirror("words");
                } else if (heartsRect.contains((int) x, (int) y)) {
                    showStickersOnMirror("hearts");
                } else if (hatsRect.contains((int) x, (int) y)) {
                    //showStickersOnMirror("hats");
                }
            }
        }
        moved = false;
    }

    public Bitmap getSharingBitmap() {
        return null;
    }

    private boolean loadingPeople = false;
    public void startBirthdayPeopleTask() {
        if (!loadingPeople) {
            DataService.getInstance().registerNetworkStateListener(activity);
            FamilyLoaderTask task = new FamilyLoaderTask(new BirthdayFamilyListener(), activity);
            task.execute(birthdayPerson);
            loadingPeople = true;
        }
    }

    public class BirthdayFamilyListener implements FamilyLoaderTask.Listener {
        @Override
        public void onComplete(ArrayList<LittlePerson> family) {
            DataService.getInstance().unregisterNetworkStateListener(activity);
            loadingPeople = false;
            showPeopleOnMirror(family);
        }

        @Override
        public void onStatusUpdate(String message) {

        }
    }
}
