package com.yellowforktech.littlefamilytree.sprites;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;

/**
 * Created by Parents on 2/15/2016.
 */
public class TextSprite extends Sprite {
    private String text;
    private Paint backgroundPaint;
    private Paint textPaint;

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public Paint getTextPaint() {
        return textPaint;
    }

    public void setTextPaint(Paint textPaint) {
        this.textPaint = textPaint;
    }

    public Paint getBackgroundPaint() {
        return backgroundPaint;
    }

    public void setBackgroundPaint(Paint backgroundPaint) {
        this.backgroundPaint = backgroundPaint;
    }

    @Override
    public void doStep() {

    }

    @Override
    public void doDraw(Canvas canvas) {
        if (backgroundPaint != null) {
            canvas.drawRect(x, y, x+width, y+height, backgroundPaint);
        }

        if (text != null) {
            if (textPaint == null) {
                textPaint = new Paint();
                textPaint.setTextSize(height * 0.75f);
                textPaint.setColor(Color.BLACK);
            }
            canvas.drawText(text, x, y, textPaint);
        }
    }

    @Override
    public void onSelect(float x, float y) {

    }

    @Override
    public boolean onMove(float oldX, float oldY, float newX, float newY) {
        return false;
    }

    @Override
    public void onRelease(float x, float y) {

    }

    @Override
    public void onDestroy() {

    }
}
