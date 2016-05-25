package com.yellowforktech.littlefamilytree.sprites;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;

/**
 * Created by Parents on 2/15/2016.
 */
public class TextSprite extends Sprite {
    private String text;
    private Paint backgroundPaint;
    private Paint textPaint;
    private boolean fitWidth;
    private boolean fitted = false;
    private boolean centered = false;
    private int centerOffset = 0;

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
        this.fitted = false;
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

    public boolean isFitWidth() {
        return fitWidth;
    }

    public void setFitWidth(boolean fitWidth) {
        this.fitWidth = fitWidth;
    }

    public boolean isCentered() {
        return centered;
    }

    public void setCentered(boolean centered) {
        this.centered = centered;
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
            if (fitWidth && !fitted) {
                Rect bounds = new Rect();
                textPaint.getTextBounds(text, 0, text.length(), bounds);
                while(bounds.width() > getWidth() && textPaint.getTextSize() > 5) {
                    textPaint.setTextSize(textPaint.getTextSize() - 1);
                    textPaint.getTextBounds(text, 0, text.length(), bounds);
                }
                fitted = true;
                if (centered) {
                    centerOffset = (getWidth() - bounds.width())/2;
                }
            }
            if (centered) {
                canvas.drawText(text, x + centerOffset, y, textPaint);
            } else {
                canvas.drawText(text, x, y, textPaint);
            }
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
