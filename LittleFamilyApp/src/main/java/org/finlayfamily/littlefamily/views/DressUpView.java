package org.finlayfamily.littlefamily.views;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Path;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;

import org.finlayfamily.littlefamily.data.DollClothing;
import org.finlayfamily.littlefamily.games.DollConfig;

import java.io.IOException;
import java.io.InputStream;

/**
 * Created by jfinlay on 4/17/2015.
 */
public class DressUpView extends View {
    private DollConfig dollConfig;
    private Bitmap doll;
    private DollClothing[] clothing;
    private Context context;

    public DressUpView(Context context) {
        super(context);
        this.context = context;
    }
    public DressUpView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
    }

    public DollConfig getDollConfig() {
        return dollConfig;
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        DollClothing[] clothes = dollConfig.getClothing(context);
        if (clothes!=null) {
            clothing = clothes;
            float factor = (float)getWidth() / (float)doll.getWidth();
            int x = 0;
            int y = (int)(doll.getHeight()*factor);
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
                    x = (int)(x+bm.getWidth()*factor);
                } catch (IOException e) {
                    Log.e("DressUpView", "Error drawing image", e);
                }
            }
        }
    }

    public void setDollConfig(DollConfig dollConfig) {
        this.dollConfig = dollConfig;

        String dollFilename = dollConfig.getDoll();
        try {
            InputStream is = context.getAssets().open(dollFilename);
            doll = BitmapFactory.decodeStream(is);

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
                float factor = (float)getWidth() / (float)doll.getWidth();
                Rect r1 = new Rect();
                r1.set(0, 0, doll.getWidth(), doll.getHeight());
                Rect r2 = new Rect();
                r2.set(0, 0, (int)(doll.getWidth()*factor), (int)(doll.getHeight()*factor));
                canvas.drawBitmap(doll, r1, r2, null);

                if (clothing!=null) {
                    for(int c=0; c<clothing.length; c++) {
                        DollClothing dc = clothing[c];
                        Rect cr1 = new Rect();
                        cr1.set(0, 0, dc.getBitmap().getWidth(), dc.getBitmap().getHeight());
                        Rect cr2 = new Rect();
                        cr2.set(dc.getX(), dc.getY(), (int)(dc.getX()+cr1.right*factor), (int)(dc.getY()+cr1.bottom*factor));
                        canvas.drawBitmap(dc.getBitmap(), cr1, cr2, null);
                    }
                }
            }
        }
    }

    private DollClothing selected = null;
    private static final float TOUCH_TOLERANCE = 4;

    private void touch_start(float x, float y) {
        if (doll!=null) {
            float factor = (float)getWidth() / (float)doll.getWidth();
            for (int c = 0; c < clothing.length; c++) {
                DollClothing dc = clothing[c];
                Rect rect = new Rect();
                rect.set(dc.getX(), dc.getY(), (int)(dc.getX()+dc.getBitmap().getWidth()*factor), (int)(dc.getY()+dc.getBitmap().getHeight()*factor));
                if (rect.contains((int)x, (int)y)) {
                    selected = dc;
                    break;
                }
            }
        }
    }
    private void touch_move(float x, float y) {
        if (selected!=null) {
            float dx = Math.abs(x - selected.getX());
            float dy = Math.abs(y - selected.getY());
            if (dx >= TOUCH_TOLERANCE || dy >= TOUCH_TOLERANCE) {
                selected.setX((int)(selected.getX()+(x - selected.getX())));
                selected.setY((int)(selected.getY()+(y - selected.getY())));
            }
        }
    }

    private void touch_up() {
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
}
