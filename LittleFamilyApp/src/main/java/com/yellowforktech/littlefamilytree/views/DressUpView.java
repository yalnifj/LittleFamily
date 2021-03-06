package com.yellowforktech.littlefamilytree.views;

import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.Log;

import com.yellowforktech.littlefamilytree.data.DollClothing;
import com.yellowforktech.littlefamilytree.filters.GPUImageAlphaMaskFilter;
import com.yellowforktech.littlefamilytree.filters.GPUImageAlphaSobelEdgeDetection;
import com.yellowforktech.littlefamilytree.games.DollConfig;
import com.yellowforktech.littlefamilytree.sprites.Sprite;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import jp.co.cyberagent.android.gpuimage.GPUImage;
import jp.co.cyberagent.android.gpuimage.GPUImageFilterGroup;

/**
 * Created by jfinlay on 4/17/2015.
 */
public class DressUpView extends SpritedSurfaceView {
    private DollConfig dollConfig;
    private Bitmap doll;
    private DollClothing[] clothing;
    private Context context;
    private List<DressedListener> listeners;
    private Paint textPaint;
    private DollClothing selected = null;
    private float factor = 1;
    private int offset = 0;
    private boolean factored = false;

    public DressUpView(Context context) {
        super(context);
        this.context = context;
        listeners = new ArrayList<>();
    }
    public DressUpView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
        listeners = new ArrayList<>();
    }

    public DollConfig getDollConfig() {
        return dollConfig;
    }

    public void setDollConfig(DollConfig dollConfig) {
        this.dollConfig = dollConfig;
        GPUImageFilterGroup filterGroup = new GPUImageFilterGroup();
        filterGroup.addFilter(new GPUImageAlphaMaskFilter());
        filterGroup.addFilter(new GPUImageAlphaSobelEdgeDetection());
        filterGroup.addFilter(new GPUImageAlphaMaskFilter(0.2f, new float[]{1f, 0f, 0f}));

        synchronized (dollConfig) {

            String dollFilename = dollConfig.getDoll();
            try {
                InputStream is = context.getAssets().open(dollFilename);
                doll = BitmapFactory.decodeStream(is);
                DollClothing[] clothes = dollConfig.getClothing(context);
                if (clothes != null) {
                    factored = false;
                    int x = 0;
                    int y = doll.getHeight();
                    boolean portrait = (Configuration.ORIENTATION_PORTRAIT==context.getResources().getConfiguration().orientation);
                    if (!portrait) {
                        x = (int) (doll.getWidth()*1.5);
                        y = 10;
                    }
                    for (int c = 0; c < clothes.length; c++) {
                        DollClothing dc = clothes[c];
                        dc.setPlaced(false);
                        InputStream cis = null;
                        try {
                            cis = context.getAssets().open(dc.getFilename());
                            Bitmap bm = BitmapFactory.decodeStream(cis);
                            try {
                                GPUImage outlineImage = new GPUImage(context);
                                outlineImage.setFilter(filterGroup);
                                Bitmap outlineBitmap = outlineImage.getBitmapWithFilterApplied(bm);
                                dc.setBitmap(bm);
                                dc.setOutline(outlineBitmap);
                            } catch(Exception e) {
                                Log.e("DressUpView", "Error using OpenGL", e);
                                dc.setOutline(bm);
                            }
                            dc.setX(x);
                            dc.setY(y);
                            if (portrait) x = (int) (x + bm.getWidth());
                            else y = y + bm.getHeight();
                        } catch (IOException e) {
                            Log.e("DressUpView", "Error drawing image", e);
                        }
                    }
                    if (clothing==null) {
                        clothing = clothes;
                    } else {
                        synchronized (clothing) {
                            clothing = clothes;
                        }
                    }
                }

                textPaint = new Paint();
                textPaint.setColor(Color.BLACK);
                textPaint.setTextAlign(Paint.Align.CENTER);
            } catch (Exception e) {
                Log.e("DressUpView", "Error drawing image", e);
            }
            this.invalidate();
        }
    }

    @Override
    public void doDraw(Canvas canvas) {
        canvas.drawColor(0, android.graphics.PorterDuff.Mode.CLEAR);
        boolean portrait = getWidth()<getHeight();
        synchronized (dollConfig) {
            if (doll != null) {
                if (portrait) {
                    factor = (float) getWidth() / (float) doll.getWidth();
                    if (doll.getHeight() * factor > this.getHeight() * 0.66f) {
                        factor = (this.getHeight() * 0.66f) / doll.getHeight();
                    }
                    Rect r2 = new Rect();
                    int w = (int) (doll.getWidth() * factor);
                    offset = 0;
                    if (w < this.getWidth()) {
                        offset = (this.getWidth() - w) / 2;
                    }
                    r2.set(offset, 0, w + offset, (int) (doll.getHeight() * factor));
                    canvas.drawBitmap(doll, null, r2, null);

                    if (textPaint!=null) {
                        textPaint.setTextSize(getWidth() * 0.10f);
                        canvas.drawText(dollConfig.getOriginalPlace(), getWidth() / 2, r2.bottom + textPaint.getTextSize(), textPaint);

                    }

                    if (clothing != null) {
                        synchronized (clothing) {
                            for (int c = 0; c < clothing.length; c++) {
                                DollClothing dc = clothing[c];
                                if (dc.getBitmap() != null) {
                                    if (!factored) {
                                        dc.setX((int) (dc.getX() * factor));
                                        dc.setY((int) (dc.getY() * factor));
                                        if (dc.getX() + dc.getBitmap().getWidth() * factor > getWidth()) {
                                            dc.setX(dc.getX() - getWidth());
                                            if (dc.getX() < 0) {
                                                dc.setX(0);
                                            }
                                            dc.setY(dc.getY() + (int) (dc.getBitmap().getHeight() * factor));
                                        }
                                        if (dc.getY() + dc.getBitmap().getHeight() * factor > getHeight()) {
                                            dc.setY(getHeight() - (int) (dc.getBitmap().getHeight() * factor));
                                        }
                                    }
                                    //-- draw outline of selected
                                    if (dc == selected) {
                                        Rect cr3 = new Rect();
                                        int left = (int) (dc.getSnapX() * factor) + offset;
                                        int top = (int) (dc.getSnapY() * factor);
                                        cr3.set(left, top,
                                                (int) (left + dc.getOutline().getWidth() * factor),
                                                (int) (top + dc.getOutline().getHeight() * factor)
                                        );
                                        canvas.drawBitmap(dc.getOutline(), null, cr3, null);
                                    }
                                    Rect cr2 = new Rect();
                                    cr2.set(dc.getX(), dc.getY(), (int) (dc.getX() + dc.getBitmap().getWidth() * factor), (int) (dc.getY() + dc.getBitmap().getHeight() * factor));
                                    canvas.drawBitmap(dc.getBitmap(), null, cr2, null);
                                }
                            }
                            factored = true;
                        }
                    }
                } else {
                    factor = (this.getHeight() * 0.80f) / doll.getHeight();
                    Rect r2 = new Rect();
                    int w = (int) (doll.getWidth() * factor);
                    offset = 0;
                    if (w < this.getWidth()) {
                        offset = ((this.getWidth() / 2) - w) / 2;
                    }
                    r2.set(offset, 0, w + offset, (int) (doll.getHeight() * factor));
                    canvas.drawBitmap(doll, null, r2, null);

                    if (textPaint!=null) {
                        textPaint.setTextSize(getHeight() * 0.10f);
                        canvas.drawText(dollConfig.getOriginalPlace(), getWidth() / 2, r2.bottom + textPaint.getTextSize(), textPaint);
                    }

                    if (clothing != null) {
                        synchronized (clothing) {
                            for (int c = 0; c < clothing.length; c++) {
                                DollClothing dc = clothing[c];
                                if (dc.getBitmap() != null) {
                                    if (!factored) {
                                        dc.setX((int) (dc.getX() * factor));
                                        dc.setY((int) (dc.getY() * factor));
                                        if (dc.getY() + dc.getBitmap().getHeight() * factor > getHeight()) {
                                            dc.setY(dc.getY() - getHeight());
                                            if (dc.getY() < 0) {
                                                dc.setY(0);
                                            }
                                            dc.setX(dc.getX() + (int) (dc.getBitmap().getWidth() * factor));
                                        }
                                        if (dc.getX() + dc.getBitmap().getWidth() * factor > getWidth()) {
                                            dc.setX(getWidth() - (int) (dc.getBitmap().getWidth() * factor));
                                        }
                                    }
                                    //-- draw outline of selected
                                    if (dc == selected) {
                                        Rect cr3 = new Rect();
                                        int left = (int) (dc.getSnapX() * factor) + offset;
                                        int top = (int) (dc.getSnapY() * factor);
                                        cr3.set(left, top,
                                                (int) (left + dc.getOutline().getWidth() * factor),
                                                (int) (top + dc.getOutline().getHeight() * factor)
                                        );
                                        canvas.drawBitmap(dc.getOutline(), null, cr3, null);
                                    }
                                    Rect cr2 = new Rect();
                                    cr2.set(dc.getX(), dc.getY(), (int) (dc.getX() + dc.getBitmap().getWidth() * factor), (int) (dc.getY() + dc.getBitmap().getHeight() * factor));
                                    canvas.drawBitmap(dc.getBitmap(), null, cr2, null);
                                }
                            }
                            factored = true;
                        }
                    }
                }
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

    protected void touch_start(float x, float y) {
        super.touch_start(x, y);
        if (doll!=null && clothing != null) {
            synchronized (clothing) {
                for (int c = clothing.length - 1; c >= 0; c--) {
                    DollClothing dc = clothing[c];
                    if (dc != null && dc.getBitmap() != null) {
                        Rect rect = new Rect();
                        rect.set(dc.getX(), dc.getY(), (int) (dc.getX() + dc.getBitmap().getWidth() * factor), (int) (dc.getY() + dc.getBitmap().getHeight() * factor));
                        if (rect.contains((int) x, (int) y)) {
                            selected = dc;
                            selected.setPlaced(false);
                            break;
                        }
                    }
                }
            }
        }
    }

    public void doMove(float oldX, float oldY, float newX, float newY) {
        super.doMove(oldX, oldY, newX, newY);
        if (selected!=null) {
            selected.setX((int) (selected.getX() + (newX - oldX)));
            selected.setY((int) (selected.getY() + (newY - oldY)));
            if (selected.getX() + selected.getBitmap().getWidth() * factor > getWidth()) {
                selected.setX((int) (getWidth() - selected.getBitmap().getWidth() * factor));
            }
            if (selected.getY() + selected.getBitmap().getHeight() * factor > getHeight()) {
                selected.setY((int) (getHeight() - selected.getBitmap().getHeight() * factor));
            }
            if (selected.getX() < 0) selected.setX(0);
            if (selected.getY() < 0) selected.setY(0);
        }
    }

    protected void touch_up(float x, float y) {
        super.touch_up(x, y);
        int dist = getWidth()/12;
        if (selected!=null && Math.abs(selected.getX() - ((selected.getSnapX()*factor)+offset)) <= dist
                && Math.abs(selected.getY() - (selected.getSnapY()*factor)) <= dist) {
            selected.setX((int) (selected.getSnapX()*factor)+offset);
            selected.setY((int) (selected.getSnapY()*factor));
            selected.setPlaced(true);

            boolean complete = true;
            if (clothing!=null) {
                synchronized (clothing) {
                    for (int c = 0; c < clothing.length; c++) {
                        DollClothing dc = clothing[c];
                        if (!dc.isPlaced()) {
                            complete = false;
                            break;
                        }
                    }
                }
            }
            if (complete) {
                Rect r2 = new Rect();
                int w = (int)(doll.getWidth()*factor);
                offset = 0;
                if (w < this.getWidth()) {
                    offset = (this.getWidth() - w)/2;
                }
                r2.set(offset/2, 0, w+offset/2, (int)(doll.getHeight()*factor));
                addStars(r2, true, 20);
                for(DressedListener l : listeners) {
                    l.onDressed();
                }
            }
        }
        selected = null;
    }

    public void addListener(DressedListener l) {
        listeners.add(l);
    }

    public interface DressedListener {
        public void onDressed();
    }
}
