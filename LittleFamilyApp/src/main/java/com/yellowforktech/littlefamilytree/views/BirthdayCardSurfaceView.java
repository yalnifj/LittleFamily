package com.yellowforktech.littlefamilytree.views;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;

import com.yellowforktech.littlefamilytree.activities.LittleFamilyActivity;
import com.yellowforktech.littlefamilytree.events.EventListener;

/**
 * Created by jfinlay on 3/21/2016.
 */
public class BirthdayCardSurfaceView extends SpritedSurfaceView implements EventListener {

    private LittleFamilyActivity activity;

    public BirthdayCardSurfaceView(Context context) {
        super(context);
        setup();
    }

    public BirthdayCardSurfaceView(Context context, AttributeSet attrs) {
        super(context, attrs);
        setup();
    }

    private void setup() {

    }

    public LittleFamilyActivity getActivity() {
        return activity;
    }

    public void setActivity(LittleFamilyActivity activity) {
        this.activity = activity;
    }

    @Override
    public void onEvent(String topic, Object o) {

    }

    @Override
    public void doStep() {
        super.doStep();
    }

    @Override
    public void doDraw(Canvas canvas) {
        super.doDraw(canvas);
    }
}
