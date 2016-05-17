package com.yellowforktech.littlefamilytree.views;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
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
import com.yellowforktech.littlefamilytree.sprites.FlyingPersonLeafSprite;
import com.yellowforktech.littlefamilytree.sprites.Sprite;
import com.yellowforktech.littlefamilytree.util.ImageHelper;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.Set;

public class FlyingSurfaceView extends SpritedSurfaceView implements SensorEventListener
{
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
    private int nestHeight;

    protected AnimatedBitmapSprite bird;
    private List<FlyingPersonLeafSprite> peopleSprites;
    private List<FlyingPersonLeafSprite> nestSprites;

    private List<LittlePerson> family;
    private Set<LittlePerson> inNest;
    private Set<LittlePerson> onBoard;

    private int maxDelay = 70;
    private int delay;
    private Random random;
    private int nestWidth;

    private List<Bitmap> tiles;
    private LinkedList<Bitmap> backgroundTiles;
    private List<Bitmap> leaves;
    private int tx = 0;
    private int ty = 0;

    private int missed = 0;

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

            Bitmap bbranch2 = ImageHelper.loadBitmapFromResource(getContext(), R.drawable.branch2, 0, (int) (this.getWidth() / 2.5), (int) (this.getWidth()/2.5));
            Sprite branch2 = new AnimatedBitmapSprite(bbranch2);
            branch2.setX(this.getWidth() - branch2.getWidth());
            branch2.setY(this.getHeight()/2 - branch2.getHeight()/2);
            addSprite(branch2);

            float ratio = (float)(branch2.getWidth()) / (float)(this.getWidth());

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
            Sprite bird = new AnimatedBitmapSprite(bbird);
            bird.setWidth(branch1.getWidth()*2);
            bird.setHeight((int) (bird.getWidth() / br));
            bird.setX(branch2.getX() + bird.getWidth()/2);
            bird.setY(branch2.getY() + bird.getHeight()/4f);

            Bitmap leaf = BitmapFactory.decodeResource(getResources(), R.drawable.leaf_stem);

            int leafWidth = (int) (bird.getWidth() * 0.8f);
            int leafHeight = (int) (bird.getWidth() * 0.8f);
            if (family != null && family.size() > 0) {

                peopleSprites = new ArrayList<>();

                float[][] leaves = new float[7][];
                leaves[0] = new float[] {0.1f, -0.4f, 45f};
                leaves[1] = new float[] {-0.75f, -0.4f, -55f};
                leaves[2] = new float[] {-0.75f, -1.0f, -30f};
                leaves[3] = new float[] {-0.3f, -1.0f, 15f};
                leaves[4] = new float[] {-0.8f, 0.3f, -75f};
                leaves[5] = new float[] {0.2f, 0.1f, 60f};
                leaves[6] = new float[] {0.25f, 0.7f, 90f};

                int p = 0;
                for (int f=0; f< leaves.length; f++) {
                    LittlePerson person = null;
                    if (random.nextInt(6) > 0 && f < family.size()) person = family.get(p++);
                    FlyingPersonLeafSprite leaf1 = new FlyingPersonLeafSprite(leaf, getWidth(), getHeight(), person, activity);
                    float r = (random.nextInt(1) + 10) / 10f;

                    leaf1.setWidth((int) (leafWidth * r));
                    leaf1.setHeight((int) (leafHeight * r));
                    leaf1.setSelectable(false);
                    leaf1.setX(branch1.getX() + leaf1.getWidth() * leaves[f][0]);
                    leaf1.setY(branch1.getY() + leaf1.getHeight() * leaves[f][1]);
                    Matrix m = new Matrix();
                    m.setRotate(leaves[f][2], leaf1.getX() + leaf1.getWidth() / 2, leaf1.getY() + leaf1.getHeight() / 2);
                    leaf1.setMatrix(m);
                    addSprite(leaf1);
                    peopleSprites.add(leaf1);
                }

                float[][] smallleaves = new float[4][];
                smallleaves[0] = new float[] {0.75f, -0.6f, 45f};
                smallleaves[1] = new float[] {-0.05f, -0.6f, -55f};
                smallleaves[2] = new float[] {-0.5f, 0.0f, -15f};
                smallleaves[3] = new float[] {-0.95f, 0.7f, -115f};

                for (int f=0; f<smallleaves.length; f++) {
                    LittlePerson person = null;
                    if (random.nextInt(3) > 0 && p < family.size()) person = family.get(p++);
                    FlyingPersonLeafSprite leaf1 = new FlyingPersonLeafSprite(leaf, getWidth(), getHeight(), person, activity);
                    float r = (random.nextInt(1) + 6) / 10f;

                    leaf1.setWidth((int) (leafWidth * r));
                    leaf1.setHeight((int) (leafHeight * r));
                    leaf1.setSelectable(false);
                    leaf1.setX(branch2.getX() + leaf1.getWidth() * smallleaves[f][0]);
                    leaf1.setY(branch2.getY() + leaf1.getHeight() * smallleaves[f][1]);
                    Matrix m = new Matrix();
                    m.setRotate(smallleaves[f][2], leaf1.getX() + leaf1.getWidth() / 2, leaf1.getY() + leaf1.getHeight() / 2);
                    leaf1.setMatrix(m);
                    addSprite(leaf1);
                    peopleSprites.add(leaf1);
                }

                addSprite(bird);

                cutScenePlaying = true;
            }
        }
    }

    public void createSprites() {
        synchronized (sprites) {
            sprites.clear();
            peopleSprites = new ArrayList<>();
            nestSprites = new ArrayList<>();

            tx = 0;
            ty = getHeight() + tiles.get(0).getHeight();

            int width = getWidth();
            while(ty > 0) {
                tx = 0;
                while(tx < width) {
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

            FlyingPersonLeafSprite personSprite = new FlyingPersonLeafSprite(leaves.get(random.nextInt(leaves.size())),
                    getWidth(), getHeight() - (nestHeight + width), person, activity);
            personSprite.setSelectable(true);
            personSprite.setY(0);
            int x = 20 + random.nextInt(getWidth() - width - 20);
            personSprite.setX(x);
            personSprite.setWrap(false);
            personSprite.setSpeed(0);
            personSprite.setSlope(5f + inNest.size() + random.nextFloat() * 5f);
            addSprite(personSprite);
            peopleSprites.add(personSprite);
        }
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

    @Override
    public void doStep() {
        super.doStep();

        if (cutScenePlaying && !cutSceneComplete) {

        }
        else if (spritesCreated) {
            if (Math.abs(roll) > 3) {
                float x = bird.getX();
                x -= roll / 2;
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

            int nestSize = nestSprites.size();
            Iterator<FlyingPersonLeafSprite> i = peopleSprites.iterator();
            while (i.hasNext()) {
                FlyingPersonLeafSprite s = i.next();
                if (s.isRemoveMe()) {
                    missed++;
                    i.remove();
                }
                else {
                    if (s.inSprite(bird.getX() + bird.getWidth() / 2, bird.getY())) {
                        s.setSlope(0);
                        s.setSpeed(0);
                        float r = (float)s.getWidth()/(float)s.getHeight();
                        s.setWidth((int) (nestWidth*r));
                        s.setHeight(nestWidth);
                        s.setX(inNest.size() * nestWidth);
                        s.setY(getHeight() - nestHeight);
                        nestSprites.add(s);
                        LittlePerson person = s.getPerson();
                        inNest.add(person);
                        onBoard.remove(person);
                        activity.sayGivenNameForPerson(person);
                        i.remove();
                    }
                }
            }
            if (nestSize!=nestSprites.size()) {
                reorderNest();
            }

            ty+=2 + inNest.size()/2;
            if (ty > tiles.get(0).getHeight()) {
                ty = 0;
                addTileRow();
            }

            if (delay > 0) {
                delay--;
            } else {
                delay = maxDelay/2 + random.nextInt(maxDelay) - (int)(inNest.size()*1.5);
                addRandomPersonSprite();
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
                    if (x > width) {
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
        canvas.drawText(String.format("zRad: %.2f", zRad), 0, 120, textPaint);
        */
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
