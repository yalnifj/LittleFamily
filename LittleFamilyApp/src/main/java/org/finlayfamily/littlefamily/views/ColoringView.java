package org.finlayfamily.littlefamily.views;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.widget.ImageView;

import com.jabistudio.androidjhlabs.filter.EdgeFilter;
import com.jabistudio.androidjhlabs.filter.GrayscaleFilter;
import com.jabistudio.androidjhlabs.filter.InvertFilter;
import com.jabistudio.androidjhlabs.filter.LaplaceFilter;
import com.jabistudio.androidjhlabs.filter.SmartBlurFilter;
import com.jabistudio.androidjhlabs.filter.util.AndroidUtils;

import org.finlayfamily.littlefamily.R;
import org.finlayfamily.littlefamily.activities.tasks.ColoringImageFilterTask;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by jfinlay on 1/22/2015.
 */
public class ColoringView extends ImageView implements ColoringImageFilterTask.Listener{
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
    private boolean loaded;
    private boolean complete;
    private Drawable spinner;

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
        mPaint.setAlpha(100);
        mPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.DST_IN));
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeJoin(Paint.Join.ROUND);
        mPaint.setStrokeCap(Paint.Cap.ROUND);
        mPaint.setStrokeWidth(10);
        paint2 = new Paint(Paint.DITHER_FLAG);

        spinner = context.getResources().getDrawable(R.drawable.abc_spinner_mtrl_am_alpha);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);

        mBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        mCanvas = new Canvas(mBitmap);
        mPaint.setStrokeWidth(w<h?w*0.15f:h*0.15f);
        Paint background = new Paint();
        background.setColor(Color.WHITE);
        mCanvas.drawRect(0,0,w,h,background);

        if (originalBitmap!=null)
            originalBitmap = Bitmap.createBitmap(originalBitmap, 0, 0, w, h);

        if (outlineBitmap!=null)
            outlineBitmap = Bitmap.createBitmap(outlineBitmap, 0, 0, w, h);
    }

    @Override
    protected void onDraw(Canvas canvas)
    {
        //super.onDraw(canvas);
        if (!loaded) {
            spinner.draw(canvas);
        } else {
            canvas.drawBitmap(originalBitmap, 0, 0, paint2);
            if (!complete) {
                canvas.drawBitmap(mBitmap, 0, 0, mBitmapPaint);
                if (outlineBitmap != null) {
                    canvas.drawBitmap(outlineBitmap, 0, 0, paint2);
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
            mBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
            mCanvas = new Canvas(mBitmap);
            mPaint.setStrokeWidth(w < h ? w * 0.15f : h * 0.15f);
            Paint background = new Paint();
            background.setColor(Color.WHITE);
            mCanvas.drawRect(0, 0, w, h, background);

            ColoringImageFilterTask task = new ColoringImageFilterTask(this);
            task.execute(originalBitmap);
        }
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
            mCanvas.drawPath(mPath,  mPaint);

            circlePath.reset();
            circlePath.addCircle(mX, mY, 35, Path.Direction.CW);
        }
    }

    private void touch_up() {
        if (!loaded) return;
        mPath.lineTo(mX, mY);
        circlePath.reset();
        // commit the path to our offscreen
        mCanvas.drawPath(mPath,  mPaint);
        // kill this so we don't double draw
        mPath.reset();

        // check if scratch is complete
        int xd = (int) mPaint.getStrokeWidth()/2;
        int yd = (int) mPaint.getStrokeWidth()/2;
        int count = 0;
        int total = 0;
        for(int y=yd; y<this.getHeight(); y+=yd) {
            for(int x=xd; x<this.getWidth(); x+=xd) {
                total++;
                int pixel = mBitmap.getPixel(x,y);
                if (Color.alpha(pixel) < 200) count++;
            }
        }
        if (count > total * 0.90) {
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
        this.invalidate();
    }

    private List<ColoringCompleteListener> listeners = new ArrayList<>();
    public void registerListener(ColoringCompleteListener l) {
        listeners.add(l);
    }

    public interface ColoringCompleteListener {
        public void onColoringComplete();
    }
}
