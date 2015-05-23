package org.finlayfamily.littlefamily.sprites;

import android.graphics.Bitmap;

import org.finlayfamily.littlefamily.activities.HomeActivity;
import org.finlayfamily.littlefamily.events.EventQueue;

/**
 * Created by jfinlay on 5/22/2015.
 */
public class MatchGameSprite extends AnimatedBitmapSprite {
    public MatchGameSprite(Bitmap bitmap) {
        super(bitmap);
    }

    protected boolean moved;
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
            EventQueue.getInstance().publish(HomeActivity.TOPIC_START_MATCH, null);
        }
        moved = false;
    }
}
