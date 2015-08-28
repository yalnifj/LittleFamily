package com.yellowforktech.littlefamilytree.views;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.AttributeSet;

public class RotateSensorSpritedSurfaceView extends SpritedSurfaceView implements SensorEventListener
{
    private SensorManager mSensorManager;
    protected Sensor rotation;
    protected int mLastAccuracy;
    protected float xRad, yRad, zRad;
    protected Paint textPaint;


	public RotateSensorSpritedSurfaceView(Context context) {
        super(context);
        mSensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
        rotation = mSensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR);

        textPaint = new Paint();
        textPaint.setColor(Color.BLACK);
        textPaint.setTextSize(25);
    }

    public RotateSensorSpritedSurfaceView(Context context, AttributeSet attrs) {
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
    public void doDraw(Canvas canvas) {
        super.doDraw(canvas);

        canvas.drawColor(Color.WHITE);

        canvas.drawText(String.format("xRad: %.2f", xRad), 0, 40, textPaint);
        canvas.drawText(String.format("yRad: %.2f", yRad), 0, 80, textPaint);
        canvas.drawText(String.format("zRad: %.2f", zRad), 0, 120, textPaint);
    }
}
