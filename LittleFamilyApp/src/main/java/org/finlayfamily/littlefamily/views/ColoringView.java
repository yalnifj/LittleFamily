package org.finlayfamily.littlefamily.views;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.ImageView;

import org.finlayfamily.littlefamily.activities.tasks.ColoringImageFilterTask;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by jfinlay on 1/22/2015.
 */
public class ColoringView extends ImageView implements ColoringImageFilterTask.Listener, WaterColorImageView.ColorChangeListener {
    public int width;
    public  int height;
    private Bitmap originalBitmap;
    private Bitmap outlineBitmap;
    private Bitmap  mBitmap;
    private Canvas  mCanvas;
    private Path    mPath;
    private Paint   mBitmapPaint;
    private Paint   paint2;
    private Context context;
    private Paint circlePaint;
    private Path circlePath;
    private Paint       mPaint;
    private Paint       noPaint;
    private boolean loaded;
    private boolean complete;
    private boolean noColor = true;

    public ColoringView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context = context;

        mPath = new Path();
        mBitmapPaint = new Paint(Paint.DITHER_FLAG);
        circlePaint = new Paint();
        circlePath = new Path();
        circlePaint.setAntiAlias(true);
        circlePaint.setColor(Color.BLUE);
        circlePaint.setStyle(Paint.Style.STROKE);
        circlePaint.setStrokeJoin(Paint.Join.MITER);
        circlePaint.setStrokeWidth(4f);

        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mPaint.setDither(true);
        //mPaint.setColor(Color.RED);
        //mPaint.setAlpha(100);
        mPaint.setColor(Color.parseColor("#99ffffff"));
        mPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_ATOP));
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeJoin(Paint.Join.ROUND);
        mPaint.setStrokeCap(Paint.Cap.ROUND);
        mPaint.setStrokeWidth(10);

        noPaint = new Paint();
        noPaint.setAntiAlias(true);
        noPaint.setDither(true);
        //noPaint.setColor(Color.RED);
        noPaint.setAlpha(100);
        //noPaint.setColor(Color.parseColor("#99ffffff"));
        noPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.DST_IN));
        noPaint.setStyle(Paint.Style.STROKE);
        noPaint.setStrokeJoin(Paint.Join.ROUND);
        noPaint.setStrokeCap(Paint.Cap.ROUND);
        noPaint.setStrokeWidth(10);

        paint2 = new Paint(Paint.DITHER_FLAG);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);

        if (originalBitmap!=null) {
            float ratio = (float) (originalBitmap.getWidth()) / originalBitmap.getHeight();
            h = (int) (w / ratio);
        }

        mBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        mCanvas = new Canvas(mBitmap);
        mPaint.setStrokeWidth(w<h?w*0.12f:h*0.12f);
        noPaint.setStrokeWidth(w<h?w*0.12f:h*0.12f);
        Paint background = new Paint();
        background.setColor(Color.WHITE);
        mCanvas.drawRect(0,0,w,h,background);
    }

    @Override
    protected void onDraw(Canvas canvas)
    {
        int w = this.getWidth();
        int h = this.getHeight();

        if (originalBitmap!=null) {
            float ratio = (float) (originalBitmap.getWidth()) / originalBitmap.getHeight();
            h = (int) (w / ratio);
            Rect dst = new Rect();
            dst.set(0, 0, w, h);
            canvas.drawBitmap(originalBitmap, null, dst, paint2);
            if (!complete) {
                canvas.drawBitmap(mBitmap, 0, 0, mBitmapPaint);
                if (outlineBitmap != null) {
                    canvas.drawBitmap(outlineBitmap, null, dst, paint2);
                }

                canvas.drawPath(circlePath, circlePaint);
            }
        }
    }

    @Override
    public void setImageBitmap(Bitmap bm)
    {
        originalBitmap = bm;

        loaded = false;
        complete = false;

        if (bm!=null) {
            int w = this.getWidth();
            int h = this.getHeight();
            if (w==0) w = 700;
            if (h==0) h = 700;
            float ratio = (float) (originalBitmap.getWidth()) / originalBitmap.getHeight();
            h = (int) (w / ratio);
            mBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
            mCanvas = new Canvas(mBitmap);
            mPaint.setStrokeWidth(w < h ? w * 0.12f : h * 0.12f);
            noPaint.setStrokeWidth(w < h ? w * 0.12f : h * 0.12f);
            Paint background = new Paint();
            background.setColor(Color.WHITE);
            mCanvas.drawRect(0, 0, w, h, background);

            ColoringImageFilterTask task = new ColoringImageFilterTask(this);
            task.execute(originalBitmap);
        }
    }

    public void setColor(int color) {
        mPaint.setColor(color);
        noColor = false;
        if (color==0) noColor = true;
    }

    private float mX, mY;
    private static final float TOUCH_TOLERANCE = 4;

    private void touch_start(float x, float y) {
        if (!loaded) return;
        mPath.reset();
        mPath.moveTo(x, y);
        mX = x;
        mY = y;
    }
    private void touch_move(float x, float y) {
        if (!loaded) return;
        float dx = Math.abs(x - mX);
        float dy = Math.abs(y - mY);
        if (dx >= TOUCH_TOLERANCE || dy >= TOUCH_TOLERANCE) {
            mPath.quadTo(mX, mY, (x + mX)/2, (y + mY)/2);
            mX = x;
            mY = y;

            // commit the path to our offscreen
            mCanvas.drawPath(mPath,  noPaint);
            if (!noColor) mCanvas.drawPath(mPath,  mPaint);

            circlePath.reset();
            circlePath.addCircle(mX, mY, 33, Path.Direction.CW);
        }
    }

    private void touch_up() {
        if (!loaded) return;
        mPath.lineTo(mX, mY);
        circlePath.reset();
        // commit the path to our offscreen
        mCanvas.drawPath(mPath,  noPaint);
        if (!noColor) mCanvas.drawPath(mPath,  mPaint);
        // kill this so we don't double draw
        mPath.reset();

        // check if scratch is complete
        int xd = (int) mPaint.getStrokeWidth()/5;
        int yd = (int) mPaint.getStrokeWidth()/5;
        int count = 0;
        int total = 0;
        for(int y=yd; y<mBitmap.getHeight(); y+=yd) {
            for(int x=xd; x<mBitmap.getWidth(); x+=xd) {
                total++;
                int pixel = mBitmap.getPixel(x,y);
                if (Color.alpha(pixel) < 200) count++;
            }
        }
        if (count >= total) {
            complete = true;
            for(ColoringCompleteListener l : listeners) {
                l.onColoringComplete();
            }
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (!loaded) return false;
        float x = event.getX();
        float y = event.getY();

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                touch_start(x, y);
                invalidate();
                break;
            case MotionEvent.ACTION_MOVE:
                touch_move(x, y);
                invalidate();
                break;
            case MotionEvent.ACTION_UP:
                touch_up();
                invalidate();
                break;
        }
        return true;
    }

    @Override
    public void onComplete(Bitmap result) {
        this.outlineBitmap = result;
        loaded = true;
        for(ColoringCompleteListener l : listeners) {
            l.onColoringReady();
        }
        this.invalidate();
    }

    private List<ColoringCompleteListener> listeners = new ArrayList<>();
    public void registerListener(ColoringCompleteListener l) {
        listeners.add(l);
    }

    @Override
    public void onColorChange(int color) {
        setColor(color);
    }

    public interface ColoringCompleteListener {
        public void onColoringComplete();
        public void onColoringReady();
    }
}
