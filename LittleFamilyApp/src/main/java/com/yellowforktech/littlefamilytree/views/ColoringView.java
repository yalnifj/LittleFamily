package com.yellowforktech.littlefamilytree.views;

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
import android.util.Log;

import com.yellowforktech.littlefamilytree.R;
import com.yellowforktech.littlefamilytree.activities.tasks.ColoringImageFilterTask;
import com.yellowforktech.littlefamilytree.sprites.Sprite;
import com.yellowforktech.littlefamilytree.util.ImageHelper;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by jfinlay on 1/22/2015.
 */
public class ColoringView extends SpritedSurfaceView implements ColoringImageFilterTask.Listener, WaterColorImageView.ColorChangeListener {
    public int width;
    public  int height;
	private Bitmap canvasBitmap;
	private Canvas shareCanvas;
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
    private boolean showOriginal = false;
    private boolean noColor = true;
    private Paint background;

    private float brushSize = 25;

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
        mPaint.setColor(Color.parseColor("#99ffffff"));
        mPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.DST_ATOP));
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeJoin(Paint.Join.ROUND);
        mPaint.setStrokeCap(Paint.Cap.ROUND);
        mPaint.setStrokeWidth(10);

        noPaint = new Paint();
        noPaint.setAntiAlias(true);
        noPaint.setDither(true);
        noPaint.setAlpha(100);
        noPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.DST_IN));
        noPaint.setStyle(Paint.Style.STROKE);
        noPaint.setStrokeJoin(Paint.Join.ROUND);
        noPaint.setStrokeCap(Paint.Cap.ROUND);
        noPaint.setStrokeWidth(10);

        paint2 = new Paint(Paint.DITHER_FLAG);

        background = new Paint();
        background.setColor(Color.WHITE);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);

        if (mBitmap!=null) {
            synchronized (mBitmap) {
                mBitmap = null;
            }
        }
    }

    /**
     * Get a bitmap copy of the screen for sharing
     * @return
     */
	public Bitmap getSharingBitmap() {
        Bitmap copy = null;
        synchronized (canvasBitmap) {
            int w = this.getWidth();
            int h = this.getHeight();
            float ratio = (float) (originalBitmap.getWidth()) / originalBitmap.getHeight();
            if (ratio > 1) {
                h = (int) (w / ratio);
            } else {
                w = (int) (h * ratio);
            }
            //-- create the cropping rectangle
            Rect src = new Rect();
            src.set(0,0,w,h);

            //-- create a copy that is the correct size
            copy = Bitmap.createBitmap(w, h, canvasBitmap.getConfig());
            Canvas copyCanvas = new Canvas(copy);

            //-- draw the cropped bitmap onto the copy
            copyCanvas.drawBitmap(canvasBitmap, src, src, null);

            Bitmap branding = ImageHelper.loadBitmapFromResource(context, R.drawable.little_family_logo,0, (int) (w*0.4f), (int) (h*0.4f));
            //-- add the branding mark
            Rect dst = new Rect();
            dst.set(0, h - branding.getHeight(), branding.getWidth(), h);
            copyCanvas.drawBitmap(branding, null, dst, null);
        }
		return copy;
	}

    private void createDrawingBitmap(int w, int h) {
        mBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        synchronized (mBitmap) {
            mCanvas = new Canvas(mBitmap);
            setBrushSize(brushSize);
            Paint background = new Paint();
            background.setColor(Color.WHITE);
            mCanvas.drawRect(0, 0, w, h, background);
        }
    }
	
	private void createSharingBitmap() {
		int w = getWidth();
		int h = getHeight();
        canvasBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        synchronized (canvasBitmap) {
            shareCanvas = new Canvas(canvasBitmap);
        }
    }

    @Override
    public void doDraw(Canvas canvas)
    {
        int w = this.getWidth();
        int h = this.getHeight();
		if (shareCanvas==null) {
			createSharingBitmap();
		}
        synchronized (canvasBitmap) {
            shareCanvas.drawColor(0, android.graphics.PorterDuff.Mode.CLEAR);

            if (originalBitmap != null && !originalBitmap.isRecycled()) {
                synchronized (originalBitmap) {
                    float ratio = (float) (originalBitmap.getWidth()) / originalBitmap.getHeight();
                    if (ratio > 1) {
                        h = (int) (w / ratio);
                    } else {
                        w = (int) (h * ratio);
                    }
                    if (mBitmap == null) {
                        createDrawingBitmap(w, h);
                    }
                    synchronized (mBitmap) {
                        Rect dst = new Rect();
                        dst.set(0, 0, w, h);
                        shareCanvas.drawRect(0, 0, w, h, background);
                        shareCanvas.drawBitmap(originalBitmap, null, dst, paint2);
                        if (!showOriginal) {
                            shareCanvas.drawBitmap(mBitmap, null, dst, mBitmapPaint);
                            if (outlineBitmap != null) {
                                shareCanvas.drawBitmap(outlineBitmap, null, dst, paint2);
                            }

                            shareCanvas.drawPath(circlePath, circlePaint);
                        }
                    }
                }
            }
            canvas.drawColor(0, android.graphics.PorterDuff.Mode.CLEAR);
            canvas.drawBitmap(canvasBitmap, 0, 0, null);
        }
		
		synchronized (sprites) {
            for (Sprite s : sprites) {
                if (s.getX() + s.getWidth() >= 0 && s.getX() <= getWidth() && s.getY() + s.getHeight() >= 0 && s.getY() <= getHeight()) {
                    s.doDraw(canvas);
                }
            }
        }
    }

    public void setImageBitmap(Bitmap bm)
    {
        originalBitmap = bm;

        loaded = false;
        complete = false;

        if (bm!=null) {
            synchronized (originalBitmap) {
                if (mBitmap != null) {
                    synchronized (mBitmap) {
                        mBitmap = null;
                    }
                }

                ColoringImageFilterTask task = new ColoringImageFilterTask(this);
                task.execute(originalBitmap);
            }

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

    public float getBrushSize() {
        return brushSize;
    }

    public void setBrushSize(float brushSize) {
        this.brushSize = brushSize;
        mPaint.setStrokeWidth(brushSize);
        noPaint.setStrokeWidth(brushSize);
    }

    protected void touch_start(float x, float y) {
        super.touch_start(x,y);
		if (!loaded || mBitmap==null) return;
        mPath.reset();
        mPath.moveTo(x, y);
    }
    public void doMove(float oldX, float oldY, float x, float y) {
        super.doMove(oldX, oldY, x, y);
		if (!loaded || mBitmap==null) return;
        mPath.quadTo(mX, mY, (x + mX)/2, (y + mY)/2);

        synchronized (mBitmap) {
            // commit the path to our offscreen
            mCanvas.drawPath(mPath, noPaint);
            if (!noColor) mCanvas.drawPath(mPath, mPaint);

            circlePath.reset();
            circlePath.addCircle(mX, mY, brushSize*0.7f, Path.Direction.CW);
        }
    }

    protected void touch_up(float tx, float ty) {
        if (!loaded || mBitmap==null) return;
        mPath.lineTo(mX, mY);
        circlePath.reset();
        // commit the path to our offscreen
        mCanvas.drawPath(mPath, noPaint);
        if (!noColor) mCanvas.drawPath(mPath,  mPaint);
        // kill this so we don't double draw
        mPath.reset();

        if (!complete) {
            // check if scratch is complete
            int xd = getWidth() / 20;
            int yd = getHeight() / 30;
            int count = 0;
            int total = 0;
            for (int y = yd; y < mBitmap.getHeight(); y += yd) {
                for (int x = xd; x < mBitmap.getWidth(); x += xd) {
                    total++;
                    int pixel = mBitmap.getPixel(x, y);
                    if (Color.alpha(pixel) < 255) count++;
                    //Log.d(this.getClass().getSimpleName(), "alpha="+Color.alpha(pixel));
                }
            }
            Log.d(this.getClass().getSimpleName(), "count=" + count + " total=" + total);
            if (count >= (float) total * 0.98f) {
                complete = true;
                Rect r = new Rect();
                r.set(starBitmap.getWidth() / 2, starBitmap.getHeight() / 2,
                        getWidth() - starBitmap.getWidth() / 2, mBitmap.getHeight() - starBitmap.getHeight() / 2);
                int sc = 10 + random.nextInt(10);
                addStars(r, false, sc);
                for (ColoringCompleteListener l : listeners) {
                    l.onColoringComplete();
                }
            }
        }
    }

    @Override
    public void onComplete(Bitmap result) {
        this.outlineBitmap = result;
        loaded = true;
        complete = false;
        for(ColoringCompleteListener l : listeners) {
            l.onColoringReady();
        }
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
