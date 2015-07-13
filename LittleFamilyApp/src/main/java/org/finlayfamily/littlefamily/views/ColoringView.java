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
import android.util.Log;
import org.finlayfamily.littlefamily.sprites.Sprite;

/**
 * Created by jfinlay on 1/22/2015.
 */
public class ColoringView extends SpritedSurfaceView implements ColoringImageFilterTask.Listener, WaterColorImageView.ColorChangeListener {
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
        mPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.DST_ATOP));
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
            if ( w < h ) {
				h = (int) (w / ratio);
			} else {
				w = (int)(h * ratio);
			}
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
    public void doDraw(Canvas canvas)
    {
        int w = this.getWidth();
        int h = this.getHeight();
		canvas.drawColor(0, android.graphics.PorterDuff.Mode.CLEAR);

        if (originalBitmap!=null) {
            float ratio = (float) (originalBitmap.getWidth()) / originalBitmap.getHeight();
            if ( w < h ) {
				h = (int) (w / ratio);
			} else {
				w = (int)(h * ratio);
			}
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
		
		synchronized (sprites) {
            for (Sprite s : sprites) {
                if (s.getX() + s.getWidth() >= 0 && s.getX() <= getWidth() && s.getY() + s.getHeight() >= 0 && s.getY() <= getHeight()) {
                    s.doDraw(canvas);
                }
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
            if (w==0) w = 600;
            if (h==0) h = 600;
            float ratio = (float) (originalBitmap.getWidth()) / originalBitmap.getHeight();
            if ( w < h ) {
				h = (int) (w / ratio);
			} else {
				w = (int)(h * ratio);
			}
            mBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
            mCanvas = new Canvas(mBitmap);
            mPaint.setStrokeWidth(w < h ? w * 0.12f : h * 0.12f);
            noPaint.setStrokeWidth(w < h ? w * 0.12f : h * 0.12f);
            Paint background = new Paint();
            background.setColor(Color.WHITE);
            mCanvas.drawRect(0, 0, w, h, background);

            ColoringImageFilterTask task = new ColoringImageFilterTask(this);
            task.execute(originalBitmap);
			
			synchronized(sprites) {
				sprites.clear();
			}
        }
    }

    public void setColor(int color) {
        mPaint.setColor(color);
        noColor = false;
        if (color==0) noColor = true;
    }

    protected void touch_start(float x, float y) {
        super.touch_start(x,y);
		if (!loaded) return;
        mPath.reset();
        mPath.moveTo(x, y);
    }
    public void doMove(float oldX, float oldY, float x, float y) {
        super.doMove(oldX, oldY, x,y);
		if (!loaded) return;
        mPath.quadTo(mX, mY, (x + mX)/2, (y + mY)/2);
            
        // commit the path to our offscreen
        mCanvas.drawPath(mPath,  noPaint);
        if (!noColor) mCanvas.drawPath(mPath,  mPaint);

        circlePath.reset();
        circlePath.addCircle(mX, mY, 25, Path.Direction.CW);
    }

    protected void touch_up(float tx, float ty) {
        if (!loaded) return;
        mPath.lineTo(mX, mY);
        circlePath.reset();
        // commit the path to our offscreen
        mCanvas.drawPath(mPath,  noPaint);
        if (!noColor) mCanvas.drawPath(mPath,  mPaint);
        // kill this so we don't double draw
        mPath.reset();

        // check if scratch is complete
        int xd = (int) mPaint.getStrokeWidth()/4;
        int yd = (int) mPaint.getStrokeWidth()/4;
        int count = 0;
        int total = 0;
        for(int y=yd; y<mBitmap.getHeight(); y+=yd) {
            for(int x=xd; x<mBitmap.getWidth(); x+=xd) {
                total++;
                int pixel = mBitmap.getPixel(x,y);
                if (Color.alpha(pixel) < 255) count++;
				//Log.d(this.getClass().getSimpleName(), "alpha="+Color.alpha(pixel));
            }
        }
		Log.d(this.getClass().getSimpleName(), "count="+count+" total="+total);
        if (count >= (float)total * 0.97f) {
            complete = true;
			Rect r = new Rect();
			r.set(starBitmap.getWidth()/2, starBitmap.getHeight()/2,
				  getWidth()-starBitmap.getWidth()/2, mBitmap.getHeight()-starBitmap.getHeight()/2);
			int sc = 10+random.nextInt(10);
			addStars(r, false, sc);
            for(ColoringCompleteListener l : listeners) {
                l.onColoringComplete();
            }
        }
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
