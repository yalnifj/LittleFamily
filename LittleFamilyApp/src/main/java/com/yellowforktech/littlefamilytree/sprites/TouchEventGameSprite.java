package com.yellowforktech.littlefamilytree.sprites;

import android.graphics.Bitmap;
import android.util.DisplayMetrics;
import android.view.HapticFeedbackConstants;

import com.yellowforktech.littlefamilytree.events.EventQueue;

/**
 * Created by jfinlay on 5/22/2015.
 */
public class TouchEventGameSprite extends AnimatedBitmapSprite {
    protected boolean moved;
    protected String eventTopic;
    protected DisplayMetrics dm;

    public TouchEventGameSprite(Bitmap bitmap, String eventTopic, DisplayMetrics dm) {
        super(bitmap);
        this.eventTopic = eventTopic;
        this.selectable = true;
        this.dm = dm;
    }

    public String getEventTopic() {
        return eventTopic;
    }

    public void setEventTopic(String eventTopic) {
        this.eventTopic = eventTopic;
    }

    @Override
    public boolean onMove(float oldX, float oldY, float newX, float newY) {
        super.onMove(oldX, oldY, newX, newY);
        if (Math.abs(newX - oldX) > 8*dm.density || Math.abs(newY - oldY) > 8*dm.density ) {
            moved = true;
        }
        return false;
    }

    @Override
    public void onRelease(float x, float y) {
        super.onRelease(x, y);

        long pressTime = (System.currentTimeMillis() - onSelectStartTime);
        boolean inside = inSprite(x, y);
        if (!moved || (inside && pressTime < 500)) {
            if (surfaceView!=null) {
                surfaceView.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP, HapticFeedbackConstants.FLAG_IGNORE_VIEW_SETTING);
            }
            EventQueue.getInstance().publish(eventTopic, this);
        }
        moved = false;
    }
}
