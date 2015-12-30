package com.yellowforktech.littlefamilytree.sprites;

import android.graphics.Bitmap;
import android.view.HapticFeedbackConstants;

import com.yellowforktech.littlefamilytree.events.EventQueue;

/**
 * Created by jfinlay on 5/22/2015.
 */
public class TouchEventGameSprite extends AnimatedBitmapSprite {
    protected boolean moved;
    protected String eventTopic;

    public TouchEventGameSprite(Bitmap bitmap, String eventTopic) {
        super(bitmap);
        this.eventTopic = eventTopic;
        this.selectable = true;
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
        if (Math.abs(newX - oldX) > 8 || Math.abs(newY - oldY) > 8 ) {
            moved = true;
        }
        return false;
    }

    @Override
    public void onRelease(float x, float y) {
        super.onRelease(x, y);

        if (!moved) {
            if (surfaceView!=null) {
                surfaceView.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP, HapticFeedbackConstants.FLAG_IGNORE_VIEW_SETTING);
            }
            EventQueue.getInstance().publish(eventTopic, this);
        }
        moved = false;
    }
}
