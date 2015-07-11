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
import android.media.MediaPlayer;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.ImageView;

import org.finlayfamily.littlefamily.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by jfinlay on 1/22/2015.
 */
public class ScratchView extends ImageView {
    public int width;
    public  int height;
    private Bitmap imageBitmap;
    private Bitmap  mBitmap;
    private Canvas  mCanvas;
    private Path    mPath;
    private Paint   mBitmapPaint;
    Context context;
    private Paint circlePaint;
    private Path circlePath;
    private Paint mPaint;
    private boolean complete = false;
    private MediaPlayer mediaPlayer;

    public ScratchView(Context context, AttributeSet attrs) {
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
        mPaint.setAlpha(220);
        mPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.DST_IN));
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeJoin(Paint.Join.ROUND);
        mPaint.setStrokeCap(Paint.Cap.ROUND);
        mPaint.setStrokeWidth(12);

        mediaPlayer = MediaPlayer.create(context, R.raw.erasing);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);

        if (imageBitmap!=null) {
            float ratio = (float) (imageBitmap.getWidth()) / imageBitmap.getHeight();
            h = (int) (w / ratio);
        }

        mBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        mCanvas = new Canvas(mBitmap);
        mPaint.setStrokeWidth(w<h?w*0.15f:h*0.15f);
        Paint background = new Paint();
        background.setColor(Color.GRAY);
        mCanvas.drawRect(0,0,w,h,background);
    }

    @Override
    protected void onDraw(Canvas canvas)
    {
        //super.onDraw(canvas);
        int w = this.getWidth();
        int h = this.getHeight();

        if (imageBitmap!=null) {
            float ratio = (float) (imageBitmap.getWidth()) / imageBitmap.getHeight();
            h = (int) (w / ratio);
            Rect dst = new Rect();
            dst.set(0, 0, w, h);
            canvas.drawBitmap(imageBitmap, null, dst, null);
        }
        if (!complete) {
            canvas.drawBitmap(mBitmap, 0, 0, mBitmapPaint);
            canvas.drawPath(circlePath, circlePaint);
        }
    }

    @Override
    public void setImageBitmap(Bitmap bm)
    {
        super.setImageBitmap(bm);
        this.imageBitmap = bm;
        int w = this.getWidth();
        int h = this.getHeight();
        if (w==0) w=600;

        float ratio = (float) (imageBitmap.getWidth()) / imageBitmap.getHeight();
        h = (int) (w / ratio);

        mBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        mCanvas = new Canvas(mBitmap);
        mPaint.setStrokeWidth(w<h?w*0.15f:h*0.15f);
        Paint background = new Paint();
        background.setColor(Color.GRAY);
        mCanvas.drawRect(0,0,w,h,background);
        complete = false;
    }

    private float mX, mY;
    private static final float TOUCH_TOLERANCE = 4;

    private void touch_start(float x, float y) {
        mPath.reset();
        mPath.moveTo(x, y);
        mX = x;
        mY = y;
        try {
            mediaPlayer.start();
            mediaPlayer.setLooping(true);
        } catch (Exception e) {
            // just let things go on
        }
    }
    private void touch_move(float x, float y) {
        float dx = Math.abs(x - mX);
        float dy = Math.abs(y - mY);
        if (dx >= TOUCH_TOLERANCE || dy >= TOUCH_TOLERANCE) {
            mPath.quadTo(mX, mY, (x + mX)/2, (y + mY)/2);
            mX = x;
            mY = y;

            // commit the path to our offscreen
            mCanvas.drawPath(mPath,  mPaint);

            circlePath.reset();
            circlePath.addCircle(mX, mY, 40, Path.Direction.CW);
        }
    }

    private void touch_up() {
        mPath.lineTo(mX, mY);
        circlePath.reset();
        // commit the path to our offscreen
        mCanvas.drawPath(mPath,  mPaint);
        // kill this so we don't double draw
        mPath.reset();

        try {
            mediaPlayer.stop();
        } catch (Exception e) {
            // just let things go on
        }

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
        if (count > total * 0.97) {
            complete = true;
            for(ScratchCompleteListener l : listeners) {
                l.onScratchComplete();
            }
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
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

    private List<ScratchCompleteListener> listeners = new ArrayList<>();
    public void registerListener(ScratchCompleteListener l) {
        listeners.add(l);
    }
    public interface ScratchCompleteListener {
        public void onScratchComplete();
    }
}
