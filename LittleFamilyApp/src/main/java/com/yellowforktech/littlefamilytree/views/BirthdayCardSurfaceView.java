package com.yellowforktech.littlefamilytree.views;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;

import com.yellowforktech.littlefamilytree.R;
import com.yellowforktech.littlefamilytree.activities.BirthdayCardActivity;
import com.yellowforktech.littlefamilytree.activities.LittleFamilyActivity;
import com.yellowforktech.littlefamilytree.data.DataService;
import com.yellowforktech.littlefamilytree.data.LittlePerson;
import com.yellowforktech.littlefamilytree.events.EventListener;
import com.yellowforktech.littlefamilytree.sprites.AnimatedBitmapSprite;
import com.yellowforktech.littlefamilytree.sprites.DraggablePersonSprite;
import com.yellowforktech.littlefamilytree.sprites.DraggableSprite;
import com.yellowforktech.littlefamilytree.sprites.Sprite;
import com.yellowforktech.littlefamilytree.util.ImageHelper;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by jfinlay on 3/21/2016.
 */
public class BirthdayCardSurfaceView extends SpritedSurfaceView implements EventListener {

    private LittleFamilyActivity activity;

    private LittlePerson birthdayPerson;

    private List<DraggableSprite> stickerSprites;
    private List<DraggableSprite> onMirror;

    private float xOffset;
    private float yOffset;

    private boolean portrait = true;
    private DisplayMetrics dm;
    private boolean spritesCreated = false;
    private boolean peopleCreated = false;

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

    @Override
    public void onEvent(String topic, Object o) {

    }

    @Override
    public void doStep() {
        super.doStep();
    }

    public void createSprites() {
        synchronized (sprites) {
            sprites.clear();

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

            vanityTop = ImageHelper.loadBitmapFromResource(context, R.drawable.vanity_top, 0, (int) (vanityWidth * .89f), (int)vanityHeight);
            vanityBottom = ImageHelper.loadBitmapFromResource(context, R.drawable.vanity_bottom, 0, (int) (vanityWidth), (int)vanityHeight);

            xOffset = (this.getWidth() - vanityBottom.getWidth()) / 2;
            yOffset = 10;

            float topOffset = xOffset + (vanityBottom.getWidth() - vanityTop.getWidth()) / 2;

            wordsRect = new Rect();
            wordsRect.set((int)topOffset, (int)(yOffset + 5*dm.density), (int)(topOffset+100*dm.density), (int)(yOffset + 50*dm.density));
            heartsRect = new Rect();
            cakesRect = new Rect();
            balloonsRect = new Rect();
            confettiRect = new Rect();
            peopleRect = new Rect();
            peopleRect.set((int)(xOffset + (vanityTop.getWidth() / 2) + 100*dm.density), (int)(yOffset + (vanityTop.getHeight() / 2) - 20*dm.density),
                    (int)(xOffset+vanityTop.getWidth()), (int)(yOffset + (vanityTop.getHeight() / 2) + 50*dm.density));
            hatsRect = new Rect();

            spritesCreated = true;
        }
    }

    public void createPeople() {
        synchronized (sprites) {
            if (activity!=null && activity.getSelectedPerson()!=null && birthdayPerson!=null) {
                int width = vanityTop.getWidth() / 13;

                Bitmap birthdayPhoto = null;
                if (birthdayPerson.getPhotoPath() != null) {
                    birthdayPhoto = ImageHelper.loadBitmapFromFile(birthdayPerson.getPhotoPath(), ImageHelper.getOrientation(birthdayPerson.getPhotoPath()), width, width, false);
                }
                if (birthdayPhoto == null) {
                    birthdayPhoto = ImageHelper.loadBitmapFromResource(activity, birthdayPerson.getDefaultPhotoResource(), 0, width, width);
                }
                AnimatedBitmapSprite birthdayPersonSprite = new AnimatedBitmapSprite(birthdayPhoto);
                birthdayPersonSprite.setX(xOffset + (vanityTop.getWidth() / 2) + 105*dm.density);
                birthdayPersonSprite.setY(yOffset + (vanityTop.getHeight() / 2) - 6*dm.density);
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
                personSprite.setX(xOffset + (vanityTop.getWidth() / 2) + 105*dm.density + width + 4*dm.density);
                personSprite.setY(yOffset + (vanityTop.getHeight() / 2) - 6*dm.density);
                sprites.add(personSprite);

                peopleCreated = true;
            }
        }
    }

    public void showPeopleOnMirror() {
        synchronized (sprites) {
            for(Sprite s : onMirror) {
                stickerSprites.remove(s);
            }
            onMirror.clear();

            int width = vanityTop.getWidth() / 8;

            if (birthdayPerson!=null) {
                try {
                    List<LittlePerson> people = DataService.getInstance().getFamilyMembers(birthdayPerson, false);
                    int x = (int) (xOffset + (vanityTop.getWidth() / 2) - 95*dm.density);
                    int y = (int) (yOffset + 10 * dm.density);
                    for(LittlePerson p : people) {
                        Bitmap photo = null;
                        if (p.getPhotoPath() != null) {
                            photo = ImageHelper.loadBitmapFromFile(p.getPhotoPath(), ImageHelper.getOrientation(p.getPhotoPath()), width, width, false);
                        }
                        if (photo == null) {
                            photo = ImageHelper.loadBitmapFromResource(activity, p.getDefaultPhotoResource(), 0, width, width);
                        }
                        DraggablePersonSprite ds = new DraggablePersonSprite(photo, p, getWidth(), getHeight(), BirthdayCardActivity.TOPIC_PERSON_TOUCHED, dm);
                        ds.setX(xOffset + (vanityTop.getWidth() / 2) + 104 * dm.density);
                        ds.setY(yOffset + (vanityTop.getHeight() / 2) - 11 * dm.density);
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
                        if (x > xOffset + (vanityTop.getWidth() / 2) + 95 * dm.density) {
                            x = (int) (xOffset + (vanityTop.getWidth() / 2) - 95*dm.density);
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
        if (!spritesCreated) {
            createSprites();
        }
        if (!peopleCreated) {
            createPeople();
        }
        synchronized (sprites) {
            canvas.drawBitmap(vanityTop, xOffset + (vanityBottom.getWidth() - vanityTop.getWidth()) / 2, yOffset, null);
            canvas.drawBitmap(vanityBottom, xOffset, yOffset + vanityTop.getHeight() - 10, null);

            for (Sprite s : sprites) {
                if (s.getX() + s.getWidth() >= 0 && s.getX() <= getWidth() && s.getY() + s.getHeight() >= 0 && s.getY() <= getHeight()) {
                    s.doDraw(canvas);
                }
            }

            for (Sprite s : stickerSprites) {
                if (s.getX() + s.getWidth() >= 0 && s.getX() <= getWidth() && s.getY() + s.getHeight() >= 0 && s.getY() <= getHeight()) {
                    s.doDraw(canvas);
                }
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
    }

    @Override
    protected void touch_up(float x, float y) {
        super.touch_up(x, y);
        if (!moved) {
            if (peopleRect.contains((int)x, (int) y)) {
                showPeopleOnMirror();
            } else if (heartsRect.contains((int)x, (int) y)) {
                //showStickersOnMirror("hearts");
            } else if (hatsRect.contains((int)x, (int) y)) {
                //showStickersOnMirror("hats");
            }
        }
        moved = false;
    }

    public Bitmap getSharingBitmap() {
        return null;
    }
}
