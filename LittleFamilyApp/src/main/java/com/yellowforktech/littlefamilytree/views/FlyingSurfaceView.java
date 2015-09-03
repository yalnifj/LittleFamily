package com.yellowforktech.littlefamilytree.views;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.AttributeSet;
import android.util.DisplayMetrics;

import com.yellowforktech.littlefamilytree.R;
import com.yellowforktech.littlefamilytree.activities.FlyingActivity;
import com.yellowforktech.littlefamilytree.data.LittlePerson;
import com.yellowforktech.littlefamilytree.sprites.AnimatedBitmapSprite;
import com.yellowforktech.littlefamilytree.sprites.MovingAnimatedBitmapSprite;
import com.yellowforktech.littlefamilytree.util.ImageHelper;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.Set;

public class FlyingSurfaceView extends SpritedSurfaceView implements SensorEventListener
{
    private SensorManager mSensorManager;
    protected Sensor rotation;
    protected int mLastAccuracy;
    protected float xRad, yRad, zRad;
    protected Paint textPaint;
    private DisplayMetrics dm;

    private FlyingActivity activity;

    private boolean spritesCreated = false;
    private int nestHeight;

    protected AnimatedBitmapSprite bird;
    private List<MovingAnimatedBitmapSprite> peopleSprites;

    private List<LittlePerson> family;
    private Set<LittlePerson> inNest;
    private Set<LittlePerson> onBoard;

    private int maxDelay = 70;
    private int delay;
    private Random random;
    private int nestWidth;


	public FlyingSurfaceView(Context context) {
        super(context);
        setup();
    }

    public FlyingSurfaceView(Context context, AttributeSet attrs) {
        super(context, attrs);
        setup();
    }

    public FlyingActivity getActivity() {
        return activity;
    }

    public void setActivity(FlyingActivity activity) {
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

    private void setup() {
        mSensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
        rotation = mSensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR);

        textPaint = new Paint();
        textPaint.setColor(Color.BLACK);
        textPaint.setTextSize(25);

        inNest = new HashSet<>();
        onBoard = new HashSet<>();

        random = new Random();
        delay = random.nextInt(maxDelay);
    }

    @Override
    public void pause() {
        super.pause();
        mSensorManager.unregisterListener(this);
    }

    @Override
    public void resume() {
        super.resume();
        mSensorManager.registerListener(this, rotation, SensorManager.SENSOR_DELAY_NORMAL);
    }

    @Override
    public final void onSensorChanged(SensorEvent event) {
        if (mLastAccuracy == SensorManager.SENSOR_STATUS_UNRELIABLE) {
            return;
        }
        if (event.sensor == rotation) {
            xRad = event.values[0];
            yRad = event.values[1];
            zRad = event.values[2];
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        if (mLastAccuracy != accuracy) {
            mLastAccuracy = accuracy;
        }
    }

    public void createSprites() {
        synchronized (sprites) {
            sprites.clear();
            peopleSprites = new ArrayList<>();

            nestHeight = (int) (50 * dm.density);
            nestWidth = (int) (30 *dm.density);

            Bitmap birdBm = BitmapFactory.decodeResource(context.getResources(), R.drawable.flying_bird1);
            bird = new AnimatedBitmapSprite(birdBm);
            bird.setX(this.getWidth() / 2 - birdBm.getWidth() / 2);
            bird.setY(this.getHeight() - (birdBm.getHeight() * 2));
            bird.addBitmap(0, BitmapFactory.decodeResource(context.getResources(), R.drawable.flying_bird2));
            bird.addBitmap(0, BitmapFactory.decodeResource(context.getResources(), R.drawable.flying_bird3));
            bird.setBounce(true);
            bird.setState(0);
            addSprite(bird);

            spritesCreated = true;
        }
    }

    public void addRandomPersonSprite() {
        if (family!=null && family.size()>0) {
            if (inNest.size() > family.size()/2) {
                activity.loadMorePeople();
            }
            int r = random.nextInt(family.size());
            LittlePerson person = family.get(r);
            int oldR = r;
            while (inNest.contains(person) || onBoard.contains(person)) {
                r++;
                if (r >= family.size()) r = 0;
                person = family.get(r);
                if (r == oldR) break;
            }
            onBoard.add(person);
            int width = bird.getWidth() / 2;
            Bitmap photo = null;
            if (person.getPhotoPath() != null) {
                photo = ImageHelper.loadBitmapFromFile(person.getPhotoPath(), ImageHelper.getOrientation(person.getPhotoPath()), width, width, false);
            }
            if (photo == null) {
                photo = ImageHelper.loadBitmapFromResource(activity, person.getDefaultPhotoResource(), 0, width, width);
            }
            MovingAnimatedBitmapSprite personSprite = new MovingAnimatedBitmapSprite(photo, getWidth(), getHeight() - (nestHeight + width));
            personSprite.setSelectable(true);
            personSprite.setY(0);
            int x = 20 + random.nextInt(getWidth() - width - 20);
            personSprite.setX(x);
            personSprite.setWrap(false);
            personSprite.setSpeed(0);
            personSprite.setSlope(5f + random.nextFloat() * 5f);
            personSprite.setData("person", person);
            addSprite(personSprite);
            peopleSprites.add(personSprite);
        }
    }

    @Override
    public void doStep() {
        super.doStep();

        if (spritesCreated) {
            if (Math.abs(yRad) > 0.05) {
                float x = bird.getX();
                x += yRad * 50;
                if (x + bird.getWidth() > getWidth()) x = getWidth() - bird.getWidth();
                if (x < 0) x = 0;
                bird.setX(x);
            }

            if (Math.abs(xRad - 0.2) > 0.05) {
                float y = bird.getY();
                y += (xRad - 0.2) * 60;
                if (y + bird.getHeight() + nestHeight > getHeight())
                    y = getHeight() - (bird.getHeight() + nestHeight);
                if (y < getHeight() * 2 / 3) y = getHeight() * 0.66f;
                bird.setY(y);
            }

            Iterator<MovingAnimatedBitmapSprite> i = peopleSprites.iterator();
            while (i.hasNext()) {
                MovingAnimatedBitmapSprite s = i.next();
                if (s.isRemoveMe()) i.remove();
                else {
                    if (s.inSprite(bird.getX() + bird.getWidth() / 2, bird.getY())) {
                        s.setSlope(0);
                        s.setSpeed(0);
                        float r = (float)s.getWidth()/(float)s.getHeight();
                        s.setWidth((int) (nestWidth*r));
                        s.setHeight(nestWidth);
                        s.setX(inNest.size() * nestWidth);
                        s.setY(getHeight() - nestHeight);
                        LittlePerson person = (LittlePerson) s.getData("person");
                        inNest.add(person);
                        onBoard.remove(person);
                        activity.speak(person.getName());
                        i.remove();
                    }
                }
            }

            if (delay > 0) {
                delay--;
            } else {
                delay = maxDelay/2 + random.nextInt(maxDelay);
                addRandomPersonSprite();
            }
        }
    }

    @Override
    public void doDraw(Canvas canvas) {
        if (!spritesCreated) {
            createSprites();
        }
        super.doDraw(canvas);

        /*
        canvas.drawText(String.format("xRad: %.2f", xRad), 0, 40, textPaint);
        canvas.drawText(String.format("yRad: %.2f", yRad), 0, 80, textPaint);
        canvas.drawText(String.format("zRad: %.2f", zRad), 0, 120, textPaint);
        */
    }
}
