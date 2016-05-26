package com.yellowforktech.littlefamilytree.views;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PointF;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Vibrator;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;

import com.yellowforktech.littlefamilytree.R;
import com.yellowforktech.littlefamilytree.activities.FlyingActivity;
import com.yellowforktech.littlefamilytree.data.LittlePerson;
import com.yellowforktech.littlefamilytree.events.EventListener;
import com.yellowforktech.littlefamilytree.events.EventQueue;
import com.yellowforktech.littlefamilytree.sprites.AnimatedBitmapSprite;
import com.yellowforktech.littlefamilytree.sprites.FlyingPersonLeafSprite;
import com.yellowforktech.littlefamilytree.sprites.MovingAnimatedBitmapSprite;
import com.yellowforktech.littlefamilytree.sprites.Sprite;
import com.yellowforktech.littlefamilytree.sprites.SpriteAnimator;
import com.yellowforktech.littlefamilytree.sprites.TextSprite;
import com.yellowforktech.littlefamilytree.sprites.TouchEventGameSprite;
import com.yellowforktech.littlefamilytree.util.ImageHelper;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.Set;

public class FlyingSurfaceView extends SpritedSurfaceView implements SensorEventListener, EventListener
{
    public static final String TOPIC_PLAY_AGAIN = "play_again";
    private SensorManager mSensorManager;
    protected Sensor rotation;
    protected int mLastAccuracy;
    protected float pitch, roll;
    protected Paint textPaint;
    private DisplayMetrics dm;

    private FlyingActivity activity;

    private boolean spritesCreated = false;
    private boolean cutSceneComplete = false;
    private boolean cutScenePlaying = false;
    private boolean gameOver = false;
    private int nestHeight;

    private SpriteAnimator animator;

    protected AnimatedBitmapSprite bird;
    private List<FlyingPersonLeafSprite> peopleSprites;
    private List<FlyingPersonLeafSprite> nestSprites;
    private List<FlyingPersonLeafSprite> missedSprites;

    private List<LittlePerson> family;
    private Set<LittlePerson> inNest;
    private Set<LittlePerson> onBoard;

    private int maxDelay = 75;
    private int delay;
    private int maxCloudDelay = 200;
    private int cloudDelay;
    private Random random;
    private int nestWidth;

    private List<Bitmap> tiles;
    private LinkedList<Bitmap> backgroundTiles;
    private List<Bitmap> leaves;
    private int tx = 0;
    private int ty = 0;
    private int wind = 0;
    private int windPower = 0;

    private int missed = 0;
    private int waitDelay = 350;

    private MovingAnimatedBitmapSprite cloud;
    private TouchEventGameSprite playAgain;
    private float pax;
    private float pay;

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
        cloudDelay = maxCloudDelay/2 + random.nextInt(maxCloudDelay/2);

        Bitmap tile1 = BitmapFactory.decodeResource(getResources(), R.drawable.bird_tile1);
        Bitmap tile2 = BitmapFactory.decodeResource(getResources(), R.drawable.bird_tile2);
        Bitmap tile3 = BitmapFactory.decodeResource(getResources(), R.drawable.bird_tile3);
        Bitmap tile4 = BitmapFactory.decodeResource(getResources(), R.drawable.bird_tile4);
        Bitmap tile5 = BitmapFactory.decodeResource(getResources(), R.drawable.bird_tile5);

        tiles = new ArrayList<>(10);
        tiles.add(tile1);
        tiles.add(tile1);
        tiles.add(tile1);
        tiles.add(tile1);
        tiles.add(tile1);
        tiles.add(tile1);
        tiles.add(tile2);
        tiles.add(tile3);
        tiles.add(tile4);
        tiles.add(tile5);

        backgroundTiles = new LinkedList<>();

        EventQueue.getInstance().subscribe(TOPIC_PLAY_AGAIN, this);
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        EventQueue.getInstance().unSubscribe(TOPIC_PLAY_AGAIN, this);
    }

    @Override
    public void pause() {
        super.pause();
        mSensorManager.unregisterListener(this);
    }

    @Override
    public void resume() {
        super.resume();
        mSensorManager.registerListener(this, rotation, SensorManager.SENSOR_DELAY_GAME);
    }

    @Override
    public final void onSensorChanged(SensorEvent event) {
        if (mLastAccuracy == SensorManager.SENSOR_STATUS_UNRELIABLE) {
            return;
        }
        if (event.sensor == rotation) {
            float[] rotationMatrix = new float[9];
            SensorManager.getRotationMatrixFromVector(rotationMatrix, event.values);
            // By default, remap the axes as if the front of the
            // device screen was the instrument panel.
            int worldAxisForDeviceAxisX = SensorManager.AXIS_X;
            int worldAxisForDeviceAxisY = SensorManager.AXIS_Z;

            float[] adjustedRotationMatrix = new float[9];
            SensorManager.remapCoordinateSystem(rotationMatrix, worldAxisForDeviceAxisX,
                    worldAxisForDeviceAxisY, adjustedRotationMatrix);

            // Transform rotation matrix into azimuth/pitch/roll
            float[] orientation = new float[3];
            SensorManager.getOrientation(adjustedRotationMatrix, orientation);

            // Convert radians to degrees
            pitch = orientation[1] * -57;
            roll = orientation[2] * -57;
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        if (mLastAccuracy != accuracy) {
            mLastAccuracy = accuracy;
        }
    }

    public void createCutScene() {
        synchronized (sprites) {
            sprites.clear();
            peopleSprites = new ArrayList<>();
            nestSprites = new ArrayList<>();
            missedSprites = new ArrayList<>();

            animator = new SpriteAnimator();

            Bitmap bbranch2 = ImageHelper.loadBitmapFromResource(getContext(), R.drawable.branch2, 0, (int) (this.getWidth() * 0.7f), (int) (this.getWidth() * 0.7f));
            Sprite branch2 = new AnimatedBitmapSprite(bbranch2);
            branch2.setX(this.getWidth() - branch2.getWidth());
            branch2.setY(this.getHeight()/2 - branch2.getHeight()/2);
            addSprite(branch2);

            Bitmap bbranch1 = BitmapFactory.decodeResource(getResources(), R.drawable.branch1);
            float br1 = (float)bbranch1.getWidth() / (float)(bbranch1.getHeight());
            Sprite branch1 = new AnimatedBitmapSprite(bbranch1);
            branch1.setHeight((int) (branch2.getWidth() * 0.8));
            branch1.setWidth((int) (branch1.getHeight() * br1));
            branch1.setX(this.getWidth() - branch1.getWidth() * 1.8f);
            branch1.setY(branch2.getY() - branch1.getHeight() / 2.6f);
            addSprite(branch1);

            Bitmap bbird = BitmapFactory.decodeResource(getResources(), R.drawable.house_tree_bird);
            float br = (float)(bbird.getWidth()) / (float)(bbird.getHeight());
            AnimatedBitmapSprite bird = new AnimatedBitmapSprite(bbird);
            bird.setWidth(branch1.getWidth()*2);
            bird.setHeight((int) (bird.getWidth() / br));
            bird.setX(branch2.getX() + bird.getWidth()/2);
            bird.setY(branch2.getY() + bird.getHeight()/4f);

            List<Bitmap> birdState1 = new ArrayList<>();
            birdState1.add(BitmapFactory.decodeResource(getResources(), R.drawable.house_tree_bird1));
            birdState1.add(BitmapFactory.decodeResource(getResources(), R.drawable.house_tree_bird2));
            birdState1.add(birdState1.get(0));
            birdState1.add(bbird);
            bird.getBitmaps().put(1, birdState1);

            bird.addBitmap(2, bbird);
            bird.addBitmap(2, bbird);

            List<Bitmap> birdState3 = new ArrayList<>();
            birdState3.add(BitmapFactory.decodeResource(getResources(), R.drawable.house_tree_bird3));
            birdState3.add(BitmapFactory.decodeResource(getResources(), R.drawable.house_tree_bird4));
            birdState3.add(BitmapFactory.decodeResource(getResources(), R.drawable.house_tree_bird5));
            birdState3.add(BitmapFactory.decodeResource(getResources(), R.drawable.house_tree_bird6));
            birdState3.add(BitmapFactory.decodeResource(getResources(), R.drawable.house_tree_bird7));
            bird.getBitmaps().put(3, birdState3);

            animator.addTiming(2000, bird, 1);
            animator.addTiming(2500, bird, 2);
            animator.addTiming(5000, bird, 1);
            animator.addTiming(5500, bird, 2);
            animator.addTiming(6000, bird, 3);
            animator.addAudioTiming(6000, R.raw.bird, getContext());
            animator.addTiming(7000, bird, 2);
            animator.addTiming(15000, bird, 2);

            Bitmap leaf = BitmapFactory.decodeResource(getResources(), R.drawable.leaf_stem);

            int leafWidth = (int) (bird.getWidth() * 0.8f);
            int leafHeight = (int) (bird.getWidth() * 0.8f);
            if (family != null && family.size() > 0) {

                peopleSprites = new ArrayList<>();

                float[][] leaves = new float[7][];
                leaves[0] = new float[]{0.1f, -0.4f, 45f};
                leaves[1] = new float[]{-0.75f, -0.4f, -55f};
                leaves[2] = new float[]{-0.75f, -1.0f, -30f};
                leaves[3] = new float[]{-0.3f, -1.0f, 15f};
                leaves[4] = new float[]{-0.8f, 0.3f, -75f};
                leaves[5] = new float[]{0.2f, 0.1f, 60f};
                leaves[6] = new float[]{0.25f, 0.7f, 90f};

                int p = 0;
                for (int f = 0; f < leaves.length; f++) {
                    LittlePerson person = null;
                    if (random.nextInt(6) > 0 && f < family.size()) person = family.get(p++);
                    FlyingPersonLeafSprite leaf1 = new FlyingPersonLeafSprite(leaf, getWidth(), getHeight(), person, activity);
                    leaf1.addBitmap(1, leaf);
                    leaf1.addBitmap(2, leaf);
                    float r = (random.nextInt(1) + 10) / 10f;

                    leaf1.setWidth((int) (leafWidth * r));
                    leaf1.setHeight((int) (leafHeight * r));
                    leaf1.setSelectable(false);
                    leaf1.setX(branch1.getX() + leaf1.getWidth() * leaves[f][0]);
                    leaf1.setY(branch1.getY() + leaf1.getHeight() * leaves[f][1]);
                    Matrix m = new Matrix();
                    m.setRotate(leaves[f][2], leaf1.getX() + leaf1.getWidth() / 2, leaf1.getY() + leaf1.getHeight() / 2);
                    leaf1.setBaseRotate(leaves[f][2]);
                    leaf1.setMatrix(m);
                    addSprite(leaf1);
                    peopleSprites.add(leaf1);
                    animator.addTiming(10000, leaf1, 1);
                    animator.addTiming(12000 + random.nextInt(1500), leaf1, 2);
                }

                float[][] smallleaves = new float[4][];
                smallleaves[0] = new float[]{0.75f, -0.6f, 45f};
                smallleaves[1] = new float[]{-0.05f, -0.6f, -55f};
                smallleaves[2] = new float[]{-0.5f, 0.0f, -15f};
                smallleaves[3] = new float[]{-0.95f, 0.7f, -115f};

                for (int f = 0; f < smallleaves.length; f++) {
                    LittlePerson person = null;
                    if (random.nextInt(3) > 0 && p < family.size()) person = family.get(p++);
                    FlyingPersonLeafSprite leaf1 = new FlyingPersonLeafSprite(leaf, getWidth(), getHeight(), person, activity);
                    leaf1.addBitmap(1, leaf);
                    leaf1.addBitmap(2, leaf);
                    float r = (random.nextInt(1) + 6) / 10f;

                    leaf1.setWidth((int) (leafWidth * r));
                    leaf1.setHeight((int) (leafHeight * r));
                    leaf1.setSelectable(false);
                    leaf1.setX(branch2.getX() + leaf1.getWidth() * smallleaves[f][0]);
                    leaf1.setY(branch2.getY() + leaf1.getHeight() * smallleaves[f][1]);
                    Matrix m = new Matrix();
                    m.setRotate(smallleaves[f][2], leaf1.getX() + leaf1.getWidth() / 2, leaf1.getY() + leaf1.getHeight() / 2);
                    leaf1.setMatrix(m);
                    leaf1.setBaseRotate(smallleaves[f][2]);
                    addSprite(leaf1);
                    peopleSprites.add(leaf1);
                    animator.addTiming(10000, leaf1, 1);
                    animator.addTiming(11500 + random.nextInt(1000), leaf1, 2);
                }

                addSprite(bird);

                cutScenePlaying = true;

                int w = getWidth()/3;
                int h = getWidth()/3;
                Bitmap bcloud = ImageHelper.loadBitmapFromResource(getContext(), R.drawable.cloud1, 0, w, h);
                float cr = (float)(bcloud.getWidth()) /(float)( bcloud.getHeight());
                cloud = new MovingAnimatedBitmapSprite(bcloud, getWidth(), getHeight());
                cloud.setWrap(true);
                cloud.setIgnoreBounds(true);
                cloud.setSpeed(0);
                cloud.setSlope(0);
                cloud.setWidth((int) (getWidth() * 1.6f));
                cloud.setHeight((int) (cloud.getWidth() / cr));
                cloud.setX(-cloud.getWidth() / 1.6f);
                cloud.setY(branch1.getY() - cloud.getHeight()/3);

                cloud.addBitmap(1, bcloud);
                cloud.setStateSpeed(1, new PointF(cloud.getWidth()/225f, 0f));
                cloud.setStateTarget(1, new PointF(-cloud.getWidth() / 4, cloud.getY()));

                cloud.addBitmap(2, bcloud);
                cloud.setStateSpeed(2, new PointF(0f, 0f));

                cloud.addBitmap(3, ImageHelper.loadBitmapFromResource(getContext(), R.drawable.cloud2, 0, w, h));

                cloud.addBitmap(4, ImageHelper.loadBitmapFromResource(getContext(), R.drawable.cloud3, 0, w, h));
                cloud.addBitmap(4, ImageHelper.loadBitmapFromResource(getContext(), R.drawable.cloud4, 0, w, h));
                cloud.addBitmap(4, ImageHelper.loadBitmapFromResource(getContext(), R.drawable.cloud5, 0, w, h));
                cloud.addBitmap(4, ImageHelper.loadBitmapFromResource(getContext(), R.drawable.cloud6, 0, w, h));
                cloud.addBitmap(4, ImageHelper.loadBitmapFromResource(getContext(), R.drawable.cloud7, 0, w, h));
                cloud.addBitmap(4, ImageHelper.loadBitmapFromResource(getContext(), R.drawable.cloud8, 0, w, h));
                cloud.addBitmap(4, ImageHelper.loadBitmapFromResource(getContext(), R.drawable.cloud9, 0, w, h));
                cloud.addBitmap(4, ImageHelper.loadBitmapFromResource(getContext(), R.drawable.cloud10, 0, w, h));
                cloud.addBitmap(4, ImageHelper.loadBitmapFromResource(getContext(), R.drawable.cloud11, 0, w, h));
                cloud.addBitmap(4, ImageHelper.loadBitmapFromResource(getContext(), R.drawable.cloud12, 0, w, h));

                cloud.addBitmap(5, ImageHelper.loadBitmapFromResource(getContext(), R.drawable.cloud11, 0, w, h));
                cloud.addBitmap(5, ImageHelper.loadBitmapFromResource(getContext(), R.drawable.cloud12, 0, w, h));

                cloud.addBitmap(6, ImageHelper.loadBitmapFromResource(getContext(), R.drawable.cloud13, 0, w, h));
                cloud.addBitmap(6, ImageHelper.loadBitmapFromResource(getContext(), R.drawable.cloud14, 0, w, h));
                cloud.addBitmap(6, ImageHelper.loadBitmapFromResource(getContext(), R.drawable.cloud15, 0, w, h));

                cloud.addBitmap(7, ImageHelper.loadBitmapFromResource(getContext(), R.drawable.cloud15, 0, w, h));
                cloud.addBitmap(7, ImageHelper.loadBitmapFromResource(getContext(), R.drawable.cloud15, 0, w, h));

                cloud.addBitmap(8, ImageHelper.loadBitmapFromResource(getContext(), R.drawable.cloud16, 0, w, h));
                cloud.addBitmap(8, ImageHelper.loadBitmapFromResource(getContext(), R.drawable.cloud16, 0, w, h));

                cloud.addBitmap(9, ImageHelper.loadBitmapFromResource(getContext(), R.drawable.cloud15, 0, w, h));
                cloud.addBitmap(9, ImageHelper.loadBitmapFromResource(getContext(), R.drawable.cloud15, 0, w, h));
                cloud.setStateSpeed(9, new PointF(cloud.getWidth()/80f, 0f));
                cloud.setStateTarget(9, new PointF(getWidth()-1, cloud.getY()));

                addSprite(cloud);

                animator.addTiming(1000, cloud, 1);
                animator.addTiming(5000, cloud, 2);
                animator.addTiming(7500, cloud, 3);
                animator.addAudioTiming(7500, R.raw.grumble, getContext());
                animator.addTiming(10000, cloud, 4);
                animator.addAudioTiming(10000, R.raw.blowing, getContext());
                animator.addTiming(10900, cloud, 5);
                animator.addTiming(14000, cloud, 6);
                animator.addTiming(14300, cloud, 7);
                animator.addTiming(15500, cloud, 8);
                animator.addAudioTiming(15000, R.raw.humph, getContext());
                animator.addTiming(15800, cloud, 9);
                animator.addTiming(22000, cloud, 9);

                TouchEventGameSprite skipButton = new TouchEventGameSprite(
                        BitmapFactory.decodeResource(getResources(), android.R.drawable.ic_media_next),
                        FlyingActivity.TOPIC_SKIP_CUTSCENE,
                        dm
                        );
                float sr = ((float)skipButton.getWidth()) / (float) skipButton.getHeight();
                skipButton.setWidth(getWidth() / 4);
                skipButton.setHeight((int) ((getWidth() / 4) / sr));
                skipButton.setX(getWidth() - skipButton.getWidth());
                skipButton.setY(getHeight() - skipButton.getHeight());
                skipButton.setIgnoreAlpha(true);
                addSprite(skipButton);

                animator.start();
            }
        }
    }

    public void createSprites() {
        synchronized (sprites) {
            sprites.clear();
            peopleSprites = new ArrayList<>();
            nestSprites = new ArrayList<>();
            missedSprites = new ArrayList<>();
            inNest.clear();
            onBoard.clear();
            missed = 0;

            gameOver = false;
            waitDelay = 200;
            tx = 0;
            ty = getHeight() + tiles.get(0).getHeight();

            int width = getWidth();
            while(ty >= 0) {
                tx = 0;
                while(tx <= width) {
                    int r = random.nextInt(tiles.size());
                    Bitmap tile = tiles.get(r);
                    backgroundTiles.add(tile);
                    tx += tile.getWidth() - 2*dm.density;
                }
                ty -= (tiles.get(0).getHeight() - 2*dm.density);
            }

            ty = 0;

            nestHeight = (int) (40 * dm.density);
            nestWidth = (int) (35 *dm.density);

            MovingAnimatedBitmapSprite titleSprite = new MovingAnimatedBitmapSprite(BitmapFactory.decodeResource(getResources(), R.drawable.rr_title), getWidth(), getHeight());
            float tr = (float)(titleSprite.getWidth()) / (float) titleSprite.getHeight();
            titleSprite.setWidth((int) (getWidth() * 0.8f));
            titleSprite.setHeight((int) (titleSprite.getWidth() / tr));
            titleSprite.setX((getWidth() - titleSprite.getWidth())/2);
            titleSprite.setY((getHeight() / 2) - titleSprite.getHeight());
            titleSprite.setSpeed(0);
            titleSprite.setSlope(getHeight() * 3f / titleSprite.getHeight());
            titleSprite.setWrap(false);
            titleSprite.setIgnoreBounds(false);
            addSprite(titleSprite);

            Bitmap birdBm = BitmapFactory.decodeResource(context.getResources(), R.drawable.flying_bird1);
            bird = new AnimatedBitmapSprite(birdBm);
            bird.setX(this.getWidth() / 2 - birdBm.getWidth() / 2);
            bird.setY(this.getHeight() - (birdBm.getHeight() * 2));
            bird.addBitmap(0, BitmapFactory.decodeResource(context.getResources(), R.drawable.flying_bird2));
            bird.addBitmap(0, BitmapFactory.decodeResource(context.getResources(), R.drawable.flying_bird3));
            bird.setBounce(true);
            bird.setState(0);
            addSprite(bird);

            leaves = new ArrayList<>(2);
            leaves.add(ImageHelper.loadBitmapFromResource(getContext(), R.drawable.leaf1, 0, (int) (bird.getWidth()*0.8), (int) (bird.getWidth()*0.8)));
            leaves.add(ImageHelper.loadBitmapFromResource(getContext(), R.drawable.leaf2, 0, (int) (bird.getWidth()*0.8), (int) (bird.getWidth()*0.8)));
            leaves.add(ImageHelper.loadBitmapFromResource(getContext(), R.drawable.leafb1, 0, (int) (bird.getWidth()*0.8), (int) (bird.getWidth()*0.8)));
            leaves.add(ImageHelper.loadBitmapFromResource(getContext(), R.drawable.leafb2, 0, (int) (bird.getWidth()*0.8), (int) (bird.getWidth()*0.8)));

            activity.speak(getResources().getString(R.string.relative_rescue));
            spritesCreated = true;
        }
    }

    public void addTileRow() {
        synchronized (sprites) {
            tx = 0;
            int width = getWidth();
            while(tx < width) {
                int r = random.nextInt(tiles.size());
                Bitmap tile = tiles.get(r);
                backgroundTiles.addFirst(tile);
                backgroundTiles.removeLast();
                tx += tile.getWidth();
            }
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
            int width = (int) (bird.getWidth() * 0.9);

            int rl = random.nextInt(2);
            FlyingPersonLeafSprite personSprite = new FlyingPersonLeafSprite(leaves.get(rl),
                    getWidth(), getHeight() - (nestHeight + width), person, activity);
            personSprite.addBitmap(3, leaves.get(rl+2));
            personSprite.setSelectable(true);
            personSprite.setY(0);
            int x = 20 + random.nextInt(getWidth() - width - 20);
            personSprite.setX(x);
            personSprite.setWrap(false);
            personSprite.setSpeed(0);
            personSprite.setSlope(5f + inNest.size() + random.nextFloat() * 4.5f);
            addSprite(personSprite);
            peopleSprites.add(personSprite);
        }
    }

    public void addRandomCloud() {
        cloud.setWrap(false);
        cloud.setIgnoreBounds(true);
        cloud.setSpeed(0);
        cloud.setSlope(0);
        cloud.setY(bird.getY());
        cloud.setState(0);
        cloud.setRemoveMe(false);

        windPower = 5 + random.nextInt(3);
        float cr = (float)(cloud.getWidth()) /(float)( cloud.getHeight());
        cloud.setWidth((int) ((getWidth() / 2.5f) + ((getWidth()/4f) * Math.abs(windPower)/7f)));
        cloud.setHeight((int) (cloud.getWidth() / cr));

        if (random.nextFloat() > 0.5) {
            windPower = windPower * -1;
            cloud.setFlipHoriz(true);
            cloud.setX(0);
            cloud.buildMatrix();
            cloud.setX(-getWidth() + cloud.getWidth()/3f);
            cloud.setStateTarget(1, new PointF(-getWidth() + cloud.getWidth()*0.8f, cloud.getY()));
        } else {
            cloud.setFlipHoriz(false);
            cloud.setX(-cloud.getWidth());
            cloud.setStateTarget(1, new PointF(-cloud.getWidth()/3.5f, cloud.getY()));
        }
        cloud.setStateSpeed(1, new PointF(cloud.getWidth()/32f, 0f));

        cloud.setStateSpeed(2, new PointF(0f, 0f));

        animator = new SpriteAnimator();
        animator.addTiming(500, cloud, 1);
        animator.addTiming(1500, cloud, 2);
        animator.addTiming(2000, cloud, 3);
        animator.addTiming(3000, cloud, 4);
        animator.addAudioTiming(3500, R.raw.blowing, getContext());
        animator.addTiming(3900, cloud, 5);
        int blowtime = 1000 + random.nextInt(2000);
        animator.addTiming(4000 + blowtime, cloud, 6);
        animator.addTiming(5300 + blowtime, cloud, 7);
        animator.addTiming(6000 + blowtime, cloud, -1);
        animator.addTiming(6500 + blowtime, cloud, -1);

        Log.i("FlyingSurfaceView", "added clound "+windPower);
        addSprite(cloud, bird);
        animator.start();
    }

    public void reorderNest() {
        if (nestSprites.size() > 0) {
            int dx = Math.min(nestWidth, getWidth() / nestSprites.size());
            int x = 0;
            for (FlyingPersonLeafSprite s : nestSprites) {
                s.setX(x);
                x += dx;
            }
        }
    }

    public void skipCutScene() {
        cutSceneComplete = true;
    }

    public void showGameOver() {
        gameOver = true;

        synchronized (sprites) {
            for (FlyingPersonLeafSprite s : peopleSprites) {
                sprites.remove(s);
            }
            peopleSprites.clear();

            AnimatedBitmapSprite gameOver = new AnimatedBitmapSprite(BitmapFactory.decodeResource(getResources(), R.drawable.rr_gameover));
            float tr = (float)(gameOver.getWidth()) / (float) gameOver.getHeight();
            gameOver.setWidth((int) (getWidth() * 0.9f));
            gameOver.setHeight((int) (gameOver.getWidth() / tr));
            gameOver.setX((getWidth() - gameOver.getWidth())/2);
            gameOver.setY((getHeight()/2) - gameOver.getHeight());
            addSprite(gameOver);

            playAgain = new TouchEventGameSprite(BitmapFactory.decodeResource(getResources(), R.drawable.rr_play), TOPIC_PLAY_AGAIN, dm);
            playAgain.setX(getWidth()/2);
            playAgain.setY(gameOver.getY()+gameOver.getHeight() - playAgain.getHeight()/5);
            pax = playAgain.getX();
            pay = playAgain.getY();
            addSprite(playAgain);

            TextSprite message = new TextSprite();
            String text = getResources().getString(R.string.you_rescued, inNest.size());
            message.setText(text);
            message.setWidth((int) (gameOver.getWidth()*0.8f));
            message.setHeight(gameOver.getHeight()/4);
            message.setX(gameOver.getX() + gameOver.getWidth()*0.1f);
            message.setY(gameOver.getY() + gameOver.getHeight()/1.4f);
            message.setFitWidth(true);
            message.setCentered(true);
            addSprite(message);
            activity.speak(text);
        }
    }

    @Override
    public void doStep() {
        super.doStep();

        if (cutScenePlaying && !cutSceneComplete) {
            animator.doStep();
            if (animator.isFinished()) {
                cutSceneComplete = true;
                cutScenePlaying = false;
            }
        }
        else if (spritesCreated) {
            wind = 0;
            if (!animator.isFinished()) {
                animator.doStep();
                if (animator.getCurrentPosition() >3 && animator.getCurrentPosition() < 7) {
                    wind = windPower;
                }
            }
            if (Math.abs(roll) > 3 || wind > 0) {
                float x = bird.getX();
                x -= roll / 2;
                x += wind*dm.density;
                if (x + bird.getWidth() > getWidth()) x = getWidth() - bird.getWidth();
                if (x < 0) x = 0;
                bird.setX(x);
            }

            if (Math.abs(pitch - 70) > 3) {
                float y = bird.getY();
                float dy = (pitch + 65);
                if (dy > 0) dy = dy * 2;
                y += dy;
                if (y + bird.getHeight() + nestHeight > getHeight())
                    y = getHeight() - (bird.getHeight() + nestHeight);
                if (y < getHeight() * 0.66f) y = getHeight() * 0.66f;
                bird.setY(y);
            }

            if (waitDelay > 0) {
                waitDelay--;
            } else if (!gameOver) {
                int nestSize = nestSprites.size();
                Iterator<FlyingPersonLeafSprite> i = peopleSprites.iterator();
                while (i.hasNext()) {
                    FlyingPersonLeafSprite s = i.next();
                    if (s.isRemoveMe()) {
                        missed++;

                        s.setSlope(0);
                        s.setSpeed(0);
                        float r = (float) s.getWidth() / (float) s.getHeight();
                        s.setWidth((int) (nestWidth * r));
                        s.setHeight(nestWidth);
                        s.setX(missedSprites.size() * nestWidth);
                        s.setY(getHeight() - nestHeight * 2);
                        s.setState(3);
                        missedSprites.add(s);
                        s.setRemoveMe(false);
                        LittlePerson person = s.getPerson();
                        onBoard.remove(person);
                        addSprite(s);
                        i.remove();

                        activity.playBuzzSound();
                        Vibrator v = (Vibrator) getContext().getSystemService(Context.VIBRATOR_SERVICE);
                        long[] pattern = {0L, 200L, 50L, 300L};
                        v.vibrate(pattern, -1);

                    } else {
                        if (s.inSprite(bird.getX() + bird.getWidth() * 0.33f, bird.getY())
                                || s.inSprite(bird.getX() + bird.getWidth() *0.66f, bird.getY())) {
                            s.setSlope(0);
                            s.setSpeed(0);
                            float r = (float) s.getWidth() / (float) s.getHeight();
                            s.setWidth((int) (nestWidth * r));
                            s.setHeight(nestWidth);
                            s.setX(inNest.size() * nestWidth);
                            s.setY(getHeight() - nestHeight);
                            nestSprites.add(s);
                            LittlePerson person = s.getPerson();
                            inNest.add(person);
                            onBoard.remove(person);
                            activity.sayGivenNameForPerson(person);
                            i.remove();
                            //activity.playCompleteSound();
                        }
                    }
                }
                if (nestSize != nestSprites.size()) {
                    reorderNest();
                }

                if (missed >= 3) {
                    showGameOver();
                }

                if (delay > 0) {
                    delay--;
                } else {
                    delay = maxDelay / 2 + random.nextInt(maxDelay) - (int) (inNest.size() * 0.5);
                    addRandomPersonSprite();
                }

                if (cloudDelay > 0) {
                    cloudDelay--;
                } else {
                    cloudDelay = maxCloudDelay / 2 + random.nextInt(maxCloudDelay);
                    if (animator.isFinished()) addRandomCloud();
                }
            } else {
                if (playAgain!=null) {
                    playAgain.setX(pax - roll/2);
                    playAgain.setY(pay + pitch*1.5f);
                }
            }

            ty += 2 + inNest.size() / 2.5;
            if (ty >= tiles.get(0).getHeight()) {
                ty = 0;
                addTileRow();
            }
        }
    }

    @Override
    public void doDraw(Canvas canvas) {
        if (!cutScenePlaying && !cutSceneComplete) {
            createCutScene();
        }
        else if (cutSceneComplete && !spritesCreated) {
            createSprites();
        }

        synchronized (sprites) {
            canvas.drawColor(0, android.graphics.PorterDuff.Mode.CLEAR);

            if (spritesCreated) {
                int x = 0;
                int width = getWidth();
                int y = ty - tiles.get(0).getHeight();
                for (Bitmap tile : backgroundTiles) {
                    int dy = tile.getHeight() - tiles.get(0).getHeight();
                    canvas.drawBitmap(tile, x, y - dy, null);
                    x += tile.getWidth() - 2 * dm.density;
                    if (x >= width) {
                        x = 0;
                        y += (tiles.get(0).getHeight() - 2 * dm.density);
                    }
                }
            }

            for (Sprite s : sprites) {
                if (s.getX() + s.getWidth() >= 0 && s.getX() <= getWidth() && s.getY() + s.getHeight() >= 0 && s.getY() <= getHeight()) {
                    s.doDraw(canvas);
                }
            }
        }

/*
        canvas.drawText(String.format("pitch: %.2f", pitch), 0, 40, textPaint);
        canvas.drawText(String.format("roll: %.2f", roll), 0, 80, textPaint);
        canvas.drawText(String.format("wind: %d", wind), 0, 120, textPaint);
*/
    }

    @Override
    public void onEvent(String topic, Object o) {
        if (topic.equals(TOPIC_PLAY_AGAIN)) {
            createSprites();
        }
    }

    @Override
    protected void touch_start(float x, float y) {
        super.touch_start(x, y);
    }

    @Override
    protected void touch_up(float x, float y) {
        super.touch_up(x, y);
    }
}
