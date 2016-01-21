package com.yellowforktech.littlefamilytree.views;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.MotionEvent;
import android.view.View;

import com.yellowforktech.littlefamilytree.activities.LittleFamilyActivity;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by jfinlay on 7/10/2015.
 */
public class WaterColorImageView extends View {

    private Bitmap paintbrush;
    private Bitmap watercolors;
    private List<Rect> colorRects;
    private int activeColor = 0;
    private List<Integer> colors;
    private List<ColorChangeListener> listeners;
    private LittleFamilyActivity activity;
    private DisplayMetrics dm;

    public WaterColorImageView(Context context) {
        super(context);
        paintbrush = BitmapFactory.decodeResource(context.getResources(), com.yellowforktech.littlefamilytree.R.drawable.paintbrush);
        watercolors = BitmapFactory.decodeResource(context.getResources(), com.yellowforktech.littlefamilytree.R.drawable.colors);
        listeners = new ArrayList<>(1);
    }

    public WaterColorImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
        paintbrush = BitmapFactory.decodeResource(context.getResources(), com.yellowforktech.littlefamilytree.R.drawable.paintbrush);
        watercolors = BitmapFactory.decodeResource(context.getResources(), com.yellowforktech.littlefamilytree.R.drawable.colors);
        listeners = new ArrayList<>(1);
    }

    public void registerListener(ColorChangeListener l) {
        listeners.add(l);
    }

    public void unregisterListener(ColorChangeListener l) {
        listeners.remove(l);
    }

    public void setActivity(LittleFamilyActivity activity) {
        this.activity = activity;
    }

    public void fireColorChanged(int color) {
        for(ColorChangeListener l : listeners) {
            l.onColorChange(color);
        }
    }

    private void setupColors() {
        dm = getContext().getResources().getDisplayMetrics();

        boolean portrait = getHeight() > getWidth();

        if (portrait) {
            Matrix matrix = new Matrix();
            matrix.postRotate(90);
            float ratio = ((float)watercolors.getWidth()) / watercolors.getHeight();
            Bitmap scaledBitmap = Bitmap.createScaledBitmap(watercolors, getHeight(), (int) (getHeight() / ratio), true);
            watercolors = Bitmap.createBitmap(scaledBitmap , 0, 0, scaledBitmap.getWidth(), scaledBitmap.getHeight(), matrix, true);
        }

        float r = getRatio();

        colors = new ArrayList<>();
        colorRects = new ArrayList<>();

        Rect dr = new Rect();
        dr.set((int)(40*dm.density*r), (int)(18*dm.density*r), (int)(95*dm.density*r), (int)(75*dm.density*r));
        colorRects.add(dr);
        colors.add(Color.parseColor("#44aa0000"));

        Rect rr = new Rect();
        rr.set((int)(115*dm.density*r), (int)(17*dm.density*r), (int)(170*dm.density*r), (int)(74*dm.density*r));
        colorRects.add(rr);
        colors.add(Color.parseColor("#44ff0000"));

        Rect o = new Rect();
        o.set((int)(185*dm.density*r), (int)(16*dm.density*r), (int)(240*dm.density*r), (int)(73*dm.density*r));
        colorRects.add(o);
        colors.add(Color.parseColor("#44ff6600"));

        Rect g = new Rect();
        g.set((int) (255*dm.density * r), (int) (15*dm.density * r), (int) (310*dm.density * r), (int) (72*dm.density * r));
        colorRects.add(g);
        colors.add(Color.parseColor("#44d4aa00"));

        Rect y = new Rect();
        y.set((int) (330*dm.density * r), (int) (14*dm.density * r), (int) (385*dm.density * r), (int) (71*dm.density * r));
        colorRects.add(y);
        colors.add(Color.parseColor("#44ffff00"));

        Rect gr = new Rect();
        gr.set((int) (400*dm.density * r), (int) (13*dm.density * r), (int) (455*dm.density * r), (int) (70*dm.density * r));
        colorRects.add(gr);
        colors.add(Color.parseColor("#4400b100"));

        Rect dg = new Rect();
        dg.set((int) (50*dm.density * r), (int) (88*dm.density * r), (int) (105*dm.density * r), (int) (145*dm.density * r));
        colorRects.add(dg);
        colors.add(Color.parseColor("#4400b100"));

        Rect b = new Rect();
        b.set((int) (120*dm.density * r), (int) (87*dm.density * r), (int) (175*dm.density * r), (int) (144*dm.density * r));
        colorRects.add(b);
        colors.add(Color.parseColor("#440000ff"));

        Rect db = new Rect();
        db.set((int) (195*dm.density * r), (int) (86*dm.density * r), (int) (250*dm.density * r), (int) (143*dm.density * r));
        colorRects.add(db);
        colors.add(Color.parseColor("#4400006f"));

        Rect p = new Rect();
        p.set((int) (265*dm.density * r), (int) (85*dm.density * r), (int) (320*dm.density * r), (int) (142*dm.density * r));
        colorRects.add(p);
        colors.add(Color.parseColor("#446400aa"));

        Rect br = new Rect();
        br.set((int) (335*dm.density * r), (int) (84*dm.density * r), (int) (390*dm.density * r), (int) (141*dm.density * r));
        colorRects.add(br);
        colors.add(Color.parseColor("#44803300"));

        Rect wh = new Rect();
        wh.set((int) (410*dm.density * r), (int) (83*dm.density * r), (int) (465*dm.density * r), (int) (140*dm.density * r));
        colorRects.add(wh);
        colors.add(0);

        if (portrait) {
            for (Rect rect : colorRects) {
                int left = (int) (rect.left*Math.cos(90) - rect.top*Math.sin(90));
                int top = (int) (rect.left*Math.sin(90) + rect.top*Math.cos(90));
                int right = (int) (rect.right*Math.cos(90) - rect.bottom*Math.sin(90));
                int bottom = (int) (rect.right*Math.sin(90) + rect.bottom*Math.cos(90));
                rect.set(left, top, right, bottom);
            }
        }

        activeColor = colors.size()-1;
    }

    private float getRatio() {
        int w = getWidth();
        int h = getHeight();
        float rw = (float) w / watercolors.getWidth();
        float rh = (float) h / watercolors.getHeight();

        float r = rw;
        if (watercolors.getHeight()* r > h) {
            r = rh;
        }
        return r;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (colors==null) {
            setupColors();
        }

        float r = getRatio();

        Rect dst = new Rect();
        dst.set(0, 0, (int)(watercolors.getWidth()*r), (int)(watercolors.getHeight()*r));
        canvas.drawBitmap(watercolors, null, dst, null);

        Rect pb = new Rect();
        Rect colorRect = colorRects.get(activeColor);
        pb.set(colorRect.left, colorRect.top, colorRect.right, colorRect.bottom);
        canvas.drawBitmap(paintbrush, null, pb, null);

    }

    private void touch_up(float x, float y) {
        for(int i=0; i<colorRects.size(); i++) {
            Rect r = colorRects.get(i);
            if (r.contains((int)x, (int)y)) {
                activeColor = i;
                fireColorChanged(colors.get(activeColor));
                break;
            }
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float x = event.getX();
        float y = event.getY();

        switch (event.getAction()) {
            case MotionEvent.ACTION_UP:
                touch_up(x, y);
                invalidate();
                break;
        }
        return true;
    }

    public interface ColorChangeListener {
        public void onColorChange(int color);
    }
}
