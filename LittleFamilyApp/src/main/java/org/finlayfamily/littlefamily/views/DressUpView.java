package org.finlayfamily.littlefamily.views;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import org.finlayfamily.littlefamily.data.DollClothing;
import org.finlayfamily.littlefamily.games.DollConfig;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by jfinlay on 4/17/2015.
 */
public class DressUpView extends View {
    private DollConfig dollConfig;
    private Bitmap doll;
    private DollClothing[] clothing;
    private Context context;
    private List<DressedListener> listeners;

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


    private boolean factored = false;
    public void setDollConfig(DollConfig dollConfig) {
        this.dollConfig = dollConfig;

        String dollFilename = dollConfig.getDoll();
        try {
            InputStream is = context.getAssets().open(dollFilename);
            doll = BitmapFactory.decodeStream(is);
            DollClothing[] clothes = dollConfig.getClothing(context);
            if (clothes!=null) {
                factored = false;
                clothing = clothes;
                int x = 0;
                int y = doll.getHeight();
                for (int c = 0; c < clothes.length; c++) {
                    DollClothing dc = clothes[c];
                    dc.setPlaced(false);
                    InputStream cis = null;
                    try {
                        cis = context.getAssets().open(dc.getFilename());
                        Bitmap bm = BitmapFactory.decodeStream(cis);
                        dc.setBitmap(bm);
                        dc.setX(x);
                        dc.setY(y);
                        x = (int)(x+bm.getWidth());
                    } catch (IOException e) {
                        Log.e("DressUpView", "Error drawing image", e);
                    }
                }
            }
        } catch (Exception e) {
            Log.e("DressUpView", "Error drawing image", e);
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        boolean portrait = getWidth()<getHeight();
        if (doll!=null) {
            if (portrait) {
                factor = (float)getWidth() / (float)doll.getWidth();
                if (doll.getHeight()*factor > this.getHeight() * 0.66f) {
                    factor = (this.getHeight() * 0.66f) / doll.getHeight();
                }
                Rect r1 = new Rect();
                r1.set(0, 0, doll.getWidth(), doll.getHeight());
                Rect r2 = new Rect();
                int w = (int)(doll.getWidth()*factor);
                offset = 0;
                if (w < this.getWidth()) {
                    offset = (this.getWidth() - w)/2;
                }
                r2.set(offset, 0, w+offset, (int)(doll.getHeight()*factor));
                canvas.drawBitmap(doll, r1, r2, null);

                if (clothing!=null) {
                    for(int c=0; c<clothing.length; c++) {
                        DollClothing dc = clothing[c];
                        if (!factored) {
                            dc.setX((int)(dc.getX()*factor));
                            dc.setY((int)(dc.getY()*factor));
                            if (dc.getX()+dc.getBitmap().getWidth()*factor > getWidth()) {
                                dc.setX(dc.getX() - getWidth());
                                if(dc.getX() < 0 ) {
                                    dc.setX(0);
                                }
                                dc.setY(dc.getY() + (int)(dc.getBitmap().getHeight()*factor));
                            }
                            if (dc.getY()+dc.getBitmap().getHeight()*factor > getHeight()) {
                                dc.setY(getHeight() - (int)(dc.getBitmap().getHeight()*factor));
                            }
                        }
                        Rect cr1 = new Rect();
                        cr1.set(0, 0, dc.getBitmap().getWidth(), dc.getBitmap().getHeight());
                        Rect cr2 = new Rect();
                        cr2.set(dc.getX(), dc.getY(), (int)(dc.getX()+cr1.right*factor), (int)(dc.getY()+cr1.bottom*factor));
                        canvas.drawBitmap(dc.getBitmap(), cr1, cr2, null);
                    }
//                    if (selected!=null) {
//                        Paint paint = new Paint();
//                        paint.setColor(Color.RED);
//                        canvas.drawCircle((selected.getSnapX()*factor) + offset, selected.getSnapY()*factor, getWidth()/12, paint);
//                    }
                    factored = true;
                }
            }
        }
    }

    private DollClothing selected = null;
    private static final float TOUCH_TOLERANCE = 4;
    private float mx;
    private float my;
    private float factor = 1;
    private int offset = 0;

    private void touch_start(float x, float y) {
        if (doll!=null) {
            for (int c = 0; c < clothing.length; c++) {
                DollClothing dc = clothing[c];
                Rect rect = new Rect();
                rect.set(dc.getX(), dc.getY(), (int)(dc.getX()+dc.getBitmap().getWidth()*factor), (int)(dc.getY()+dc.getBitmap().getHeight()*factor));
                if (rect.contains((int)x, (int)y)) {
                    selected = dc;
                    mx = x;
                    my = y;
                    selected.setPlaced(false);
                    break;
                }
            }
        }
    }
    private void touch_move(float x, float y) {
        if (selected!=null) {
            float dx = Math.abs(x - mx);
            float dy = Math.abs(y - my);
            if (dx >= TOUCH_TOLERANCE || dy >= TOUCH_TOLERANCE) {
                selected.setX((int)(selected.getX()+(x - mx)));
                selected.setY((int)(selected.getY()+(y - my)));
                if (selected.getX()+selected.getBitmap().getWidth()*factor > getWidth()) {
                    selected.setX((int)(getWidth() - selected.getBitmap().getWidth()*factor));
                }
                if (selected.getY()+selected.getBitmap().getHeight()*factor > getHeight()) {
                    selected.setY((int) (getHeight() - selected.getBitmap().getHeight() * factor));
                }
                if (selected.getX() < 0 ) selected.setX(0);
                if (selected.getY() < 0 ) selected.setY(0);
                mx=x;
                my=y;
            }
        }
    }

    private void touch_up() {
        int dist = getWidth()/12;
        if (selected!=null && Math.abs(selected.getX() - ((selected.getSnapX()*factor)+offset)) <= dist
                && Math.abs(selected.getY() - (selected.getSnapY()*factor)) <= dist) {
            selected.setX((int) (selected.getSnapX()*factor)+offset);
            selected.setY((int) (selected.getSnapY()*factor));
            selected.setPlaced(true);

            boolean complete = true;
            for (int c = 0; c < clothing.length; c++) {
                DollClothing dc = clothing[c];
                if (!dc.isPlaced()) {
                    complete = false;
                    break;
                }
            }
            if (complete) {
                for(DressedListener l : listeners) {
                    l.onDressed();
                }
            }
        }
        selected = null;
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

    public void addListener(DressedListener l) {
        listeners.add(l);
    }

    public interface DressedListener {
        public void onDressed();
    }
}
