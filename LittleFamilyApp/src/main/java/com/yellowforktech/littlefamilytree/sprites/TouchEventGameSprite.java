package com.yellowforktech.littlefamilytree.sprites;

import android.graphics.Bitmap;
import android.view.HapticFeedbackConstants;

import com.yellowforktech.littlefamilytree.events.EventQueue;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by jfinlay on 5/22/2015.
 */
public class TouchEventGameSprite extends AnimatedBitmapSprite {
    protected boolean moved;
    protected String eventTopic;
    protected Map<String, Object> data;

    public TouchEventGameSprite(Bitmap bitmap, String eventTopic) {
        super(bitmap);
        this.eventTopic = eventTopic;
        this.data = new HashMap<>(0);
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
        moved = true;
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

    public void setData(String key, Object value) {
        data.put(key, value);
    }

    public Object getData(String key) {
        return data.get(key);
    }
}
