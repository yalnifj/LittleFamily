package com.yellowforktech.littlefamilytree.views;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

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

    public void fireColorChanged(int color) {
        for(ColorChangeListener l : listeners) {
            l.onColorChange(color);
        }
    }

    private void setupColors() {
        colors = new ArrayList<>();
        colorRects = new ArrayList<>();

        int w = getWidth();
        float r = (float) w / watercolors.getWidth();

        Rect dr = new Rect();
        dr.set((int)(40*r), (int)(18*r), (int)(95*r), (int)(75*r));
        colorRects.add(dr);
        colors.add(Color.parseColor("#44aa0000"));

        Rect rr = new Rect();
        rr.set((int)(115*r), (int)(17*r), (int)(170*r), (int)(74*r));
        colorRects.add(rr);
        colors.add(Color.parseColor("#44ff0000"));

        Rect o = new Rect();
        o.set((int)(185*r), (int)(16*r), (int)(240*r), (int)(73*r));
        colorRects.add(o);
        colors.add(Color.parseColor("#44ff6600"));

        Rect g = new Rect();
        g.set((int) (255 * r), (int) (15 * r), (int) (310 * r), (int) (72 * r));
        colorRects.add(g);
        colors.add(Color.parseColor("#44d4aa00"));

        Rect y = new Rect();
        y.set((int) (330 * r), (int) (14 * r), (int) (385 * r), (int) (71 * r));
        colorRects.add(y);
        colors.add(Color.parseColor("#44ffff00"));

        Rect gr = new Rect();
        gr.set((int) (400 * r), (int) (13 * r), (int) (455 * r), (int) (70 * r));
        colorRects.add(gr);
        colors.add(Color.parseColor("#4400b100"));

        Rect dg = new Rect();
        dg.set((int) (50 * r), (int) (88 * r), (int) (105 * r), (int) (145 * r));
        colorRects.add(dg);
        colors.add(Color.parseColor("#4400b100"));

        Rect b = new Rect();
        b.set((int) (120 * r), (int) (87 * r), (int) (175 * r), (int) (144 * r));
        colorRects.add(b);
        colors.add(Color.parseColor("#440000ff"));

        Rect db = new Rect();
        db.set((int) (195 * r), (int) (86 * r), (int) (250 * r), (int) (143 * r));
        colorRects.add(db);
        colors.add(Color.parseColor("#4400006f"));

        Rect p = new Rect();
        p.set((int) (265 * r), (int) (85 * r), (int) (320 * r), (int) (142 * r));
        colorRects.add(p);
        colors.add(Color.parseColor("#446400aa"));

        Rect br = new Rect();
        br.set((int) (335 * r), (int) (84 * r), (int) (390 * r), (int) (141 * r));
        colorRects.add(br);
        colors.add(Color.parseColor("#44803300"));

        Rect wh = new Rect();
        wh.set((int) (410 * r), (int) (83 * r), (int) (465 * r), (int) (140 * r));
        colorRects.add(wh);
        colors.add(0);

        activeColor = colors.size()-1;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (colors==null) {
            setupColors();
        }

        float r = (float) getWidth() / watercolors.getWidth();

        Rect dst = new Rect();
        dst.set(0, 0, (int)(watercolors.getWidth()*r), (int)(watercolors.getHeight()*r));
        canvas.drawBitmap(watercolors, null, dst, null);

        int pw = (int) (paintbrush.getWidth());
        int ph = (int) (paintbrush.getHeight());
        Rect pb = new Rect();
        Rect colorRect = colorRects.get(activeColor);
        pb.set(colorRect.left*2, colorRect.top*2, colorRect.right*2, colorRect.bottom*2);
        canvas.drawBitmap(paintbrush, null, pb, null);

    }

    private void touch_up(float x, float y) {
        for(int i=0; i<colorRects.size(); i++) {
            Rect r = colorRects.get(i);
            if (r.contains((int)x/2, (int)y/2)) {
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
