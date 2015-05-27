package org.finlayfamily.littlefamily.sprites;

import android.graphics.Bitmap;

import org.finlayfamily.littlefamily.events.EventQueue;

/**
 * Created by jfinlay on 5/22/2015.
 */
public class TouchEventGameSprite extends AnimatedBitmapSprite {
    protected boolean moved;
    protected String eventTopic;

    public TouchEventGameSprite(Bitmap bitmap, String eventTopic) {
        super(bitmap);
        this.eventTopic = eventTopic;
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
            EventQueue.getInstance().publish(eventTopic, this);
        }
        moved = false;
    }
}
