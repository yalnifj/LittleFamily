package com.yellowforktech.littlefamilytree.views;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.view.View;

/**
 * Created by jfinlay on 10/26/2015.
 */
public class BrushSizeView extends View {
    private float minSize = 5f;
    private float maxSize = 30f;
    private float brushSize = 20f;

    private Paint brushPaint;
    private Path brushPath = new Path();

    public BrushSizeView(Context context) {
        super(context);
    }

    public BrushSizeView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public BrushSizeView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public float getMaxSize() {
        return maxSize;
    }

    public void setMaxSize(float maxSize) {
        if (maxSize < 0 ) maxSize = 0;
        if (maxSize < minSize) {
            this.maxSize = minSize;
            this.minSize = maxSize;
        } else {
            this.maxSize = maxSize;
        }

        if (this.brushSize > this.maxSize) setBrushSize(this.maxSize);
    }

    public float getMinSize() {
        return minSize;
    }

    public void setMinSize(float minSize) {
        if (minSize < 0) minSize=0;
        if (minSize > maxSize) {
            this.minSize = maxSize;
            maxSize = minSize;
        } else {
            this.minSize = minSize;
        }

        if (brushSize < this.minSize) setBrushSize(this.minSize);
    }

    public float getBrushSize() {
        return brushSize;
    }

    public Paint getBrushPaint() {
        return brushPaint;
    }

    public void setBrushPaint(Paint brushPaint) {
        this.brushPaint = brushPaint;
        invalidate();
    }

    public void setBrushSize(float brushSize) {
        if (brushSize < minSize) brushSize = minSize;
        if (brushSize > maxSize) brushSize = maxSize;
        this.brushSize = brushSize;

        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (brushPaint==null) {
            brushPaint = new Paint();
            brushPaint.setStyle(Paint.Style.FILL_AND_STROKE);
            brushPaint.setColor(Color.BLUE);
        }

        brushPath.reset();
        brushPath.addCircle(getWidth()/2, getHeight()/2, brushSize, Path.Direction.CW);
        canvas.drawPath(brushPath, brushPaint);
    }
}
