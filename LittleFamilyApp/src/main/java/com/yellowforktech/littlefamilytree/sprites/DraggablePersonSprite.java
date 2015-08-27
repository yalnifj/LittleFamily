package com.yellowforktech.littlefamilytree.sprites;

import android.graphics.Bitmap;

import com.yellowforktech.littlefamilytree.data.LittlePerson;

/**
 * Created by jfinlay on 8/26/2015.
 */
public class DraggablePersonSprite extends TouchEventGameSprite {
    protected LittlePerson person;
    protected float sx;
    protected float sy;
    protected int maxWidth;
    protected int maxHeight;

    public DraggablePersonSprite(Bitmap bitmap, LittlePerson person, int maxWidth, int maxHeight, String eventTopic) {
        super(bitmap, eventTopic);
        this.person = person;
        selectable = true;
        this.maxHeight = maxHeight;
        this.maxWidth = maxWidth;
    }

    @Override
    public boolean onMove(float oldX, float oldY, float newX, float newY) {
        float xdiff = newX-oldX;
        if (!moved && Math.abs(xdiff) < 10) {
            return false;
        }

        x += xdiff;
        if (x<0) x = 0;
        if (x + width > maxWidth) x = maxWidth - width;

        y += (newY - oldY);
        if (y<0) y = 0;
        if (y +height > maxHeight) y = maxHeight - height;

        moved = true;
        return true;
    }

    @Override
    public void onSelect(float x, float y) {
        sx = this.x;
        sy = this.y;
        super.onSelect(x, y);
    }

    @Override
    public void onRelease(float x, float y) {
        if (moved) {
            this.x = sx; //-- TODO animate
            this.y = sy;
        }
        super.onRelease(x, y);
    }
}
