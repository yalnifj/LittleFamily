package com.yellowforktech.littlefamilytree.views;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.util.DisplayMetrics;

import com.yellowforktech.littlefamilytree.R;
import com.yellowforktech.littlefamilytree.activities.LittleFamilyActivity;
import com.yellowforktech.littlefamilytree.events.EventListener;
import com.yellowforktech.littlefamilytree.sprites.DraggablePersonSprite;
import com.yellowforktech.littlefamilytree.util.ImageHelper;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by jfinlay on 3/21/2016.
 */
public class BirthdayCardSurfaceView extends SpritedSurfaceView implements EventListener {

    private LittleFamilyActivity activity;

    private List<DraggablePersonSprite> peopleSprites;

    private float xOffset;
    private float yOffset;

    private boolean portrait = true;
    private DisplayMetrics dm;
    private boolean spritesCreated = false;

    private Bitmap vanityTop;
    private Bitmap vanityBottom;

    public BirthdayCardSurfaceView(Context context) {
        super(context);
        setup();
    }

    public BirthdayCardSurfaceView(Context context, AttributeSet attrs) {
        super(context, attrs);
        setup();
    }

    private void setup() {
        peopleSprites = new ArrayList<>();
    }

    public LittleFamilyActivity getActivity() {
        return activity;
    }

    public void setActivity(LittleFamilyActivity activity) {
        this.activity = activity;
        dm = new DisplayMetrics();
        activity.getWindowManager().getDefaultDisplay().getMetrics(dm);
    }

    @Override
    public void onEvent(String topic, Object o) {

    }

    @Override
    public void doStep() {
        super.doStep();
    }

    public void createSprites() {
        synchronized (sprites) {
            sprites.clear();
            peopleSprites.clear();

            if (this.getWidth() > this.getHeight()) {
                portrait = false;
            }

            float width = Math.min(this.getWidth(), this.getHeight());

            float vanityWidth = width;
            float vanityHeight = getHeight() / 2f;
            if (!portrait) {
                vanityHeight = width;
                vanityWidth = this.getWidth() / 2f;
            }

            vanityTop = ImageHelper.loadBitmapFromResource(context, R.drawable.vanity_top, 0, (int) (vanityWidth), (int)vanityHeight);
            vanityBottom = ImageHelper.loadBitmapFromResource(context, R.drawable.vanity_bottom, 0, (int) (vanityWidth), (int)vanityHeight);

            xOffset = (this.getWidth() - (vanityBottom.getWidth() + vanityTop.getWidth())) / 2;
            if (xOffset < 0) {
                xOffset = 0;
            }
            yOffset = 10;

            spritesCreated = true;
        }
    }

    @Override
    public void doDraw(Canvas canvas) {
        canvas.drawColor(0, android.graphics.PorterDuff.Mode.CLEAR);
        if (!spritesCreated) {
            createSprites();
        }
        synchronized (sprites) {
            if (portrait) {
                canvas.drawBitmap(vanityTop, xOffset, yOffset, null);
                canvas.drawBitmap(vanityBottom, xOffset, yOffset + vanityTop.getHeight(), null);
            } else {
                canvas.drawBitmap(vanityBottom, xOffset, yOffset, null);
                canvas.drawBitmap(vanityTop, xOffset + vanityBottom.getWidth(), yOffset, null);
            }
        }
    }
}
