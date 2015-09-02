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

import com.yellowforktech.littlefamilytree.R;
import com.yellowforktech.littlefamilytree.sprites.AnimatedBitmapSprite;

public class FlyingSurfaceView extends SpritedSurfaceView implements SensorEventListener
{
    private SensorManager mSensorManager;
    protected Sensor rotation;
    protected int mLastAccuracy;
    protected float xRad, yRad, zRad;
    protected Paint textPaint;

    private boolean spritesCreated = false;

    protected AnimatedBitmapSprite bird;


	public FlyingSurfaceView(Context context) {
        super(context);
        mSensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
        rotation = mSensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR);

        textPaint = new Paint();
        textPaint.setColor(Color.BLACK);
        textPaint.setTextSize(25);
    }

    public FlyingSurfaceView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mSensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
        rotation = mSensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR);

        textPaint = new Paint();
        textPaint.setColor(Color.BLACK);
        textPaint.setTextSize(25);
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

    @Override
    public void doStep() {
        super.doStep();

        if (Math.abs(yRad)>0.05) {
            float x = bird.getX();
            x += yRad*50;
            if (x + bird.getWidth()> getWidth()) x = getWidth()-bird.getWidth();
            if (x < 0) x = 0;
            bird.setX(x);
        }

        if (Math.abs(xRad)>0.05) {
            float y = bird.getY();
            y += xRad*60;
            if (y + bird.getHeight()> getHeight()) y = getHeight()-bird.getHeight();
            if (y < getHeight()*2/3) y = getHeight()*2/3;
            bird.setY(y);
        }
    }

    public void createSprites() {
        synchronized (sprites) {
            sprites.clear();

            Bitmap birdBm = BitmapFactory.decodeResource(context.getResources(), R.drawable.flying_bird1);
            bird = new AnimatedBitmapSprite(birdBm);
            bird.setX(this.getWidth() / 2 - birdBm.getWidth()/2);
            bird.setY(this.getHeight() - (birdBm.getHeight() * 2));
            bird.addBitmap(0, BitmapFactory.decodeResource(context.getResources(), R.drawable.flying_bird2));
            bird.addBitmap(0, BitmapFactory.decodeResource(context.getResources(), R.drawable.flying_bird3));
            bird.setBounce(true);
            bird.setState(0);
            addSprite(bird);

            spritesCreated = true;
        }
    }

    @Override
    public void doDraw(Canvas canvas) {
        if (!spritesCreated) {
            createSprites();
        }
        super.doDraw(canvas);

        canvas.drawText(String.format("xRad: %.2f", xRad), 0, 40, textPaint);
        canvas.drawText(String.format("yRad: %.2f", yRad), 0, 80, textPaint);
        canvas.drawText(String.format("zRad: %.2f", zRad), 0, 120, textPaint);
    }
}
