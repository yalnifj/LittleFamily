package org.finlayfamily.littlefamily.sprites;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;

import org.finlayfamily.littlefamily.activities.LittleFamilyActivity;
import org.finlayfamily.littlefamily.data.LittlePerson;
import org.finlayfamily.littlefamily.events.EventQueue;
import org.finlayfamily.littlefamily.util.ImageHelper;

/**
 * Created by Parents on 5/29/2015.
 */
public class TreePersonAnimatedSprite extends Sprite {
    protected LittlePerson person;
    protected LittleFamilyActivity activity;
    protected Bitmap leafBitmap;
    protected Bitmap photo;
    protected boolean moved;
    protected Paint textPaint;

    public static final int STATE_CLOSED = 0;
    public static final int STATE_ANIMATING_OPEN = 1;
    public static final int STATE_OPEN = 2;
    public static final int STATE_ANIMATING_CLOSED = 3;

    public TreePersonAnimatedSprite(LittlePerson person, LittleFamilyActivity activity, Bitmap leafBitmap) {
        this.person = person;
        this.activity = activity;
        this.leafBitmap = leafBitmap;
        this.selectable = true;
        this.setWidth(leafBitmap.getWidth());
        this.setHeight(leafBitmap.getHeight());

        photo = null;
        if (person.getPhotoPath() != null) {
            photo = ImageHelper.loadBitmapFromFile(person.getPhotoPath(), ImageHelper.getOrientation(person.getPhotoPath()), (int) (width * 0.8), (int) (height * 0.8), false);
        } else {
            photo = ImageHelper.loadBitmapFromResource(activity, person.getDefaultPhotoResource(), 0, (int)(width*0.8), (int) (height*0.8));
        }

        textPaint = new Paint();
        textPaint.setColor(Color.WHITE);
    }

    @Override
    public void doStep() {

    }

    @Override
    public void doDraw(Canvas canvas) {
        Rect dst = new Rect();
        dst.set((int)getX(), (int)getY(), (int)(getX()+getWidth()), (int) (getY()+getHeight()));
        canvas.drawBitmap(leafBitmap, null, dst, null);
        Rect photoRect = new Rect();
        photoRect.set((int) (getX() +width*0.1f), (int)(getY() + height * 0.1f),
                (int) (getX() +width*0.9f), (int)(getY() + height * 0.9f));
        canvas.drawBitmap(photo, width * 0.1f, height * 0.1f, null);

        canvas.drawText(person.getGivenName(), 0, height*0.9f, textPaint);
    }

    @Override
    public void onSelect(float x, float y) {

    }

    @Override
    public boolean onMove(float oldX, float oldY, float newX, float newY) {
        moved = true;
        return false;
    }

    @Override
    public void onRelease(float x, float y) {
        if (!moved) {
            state = STATE_ANIMATING_OPEN;
        }
        moved = false;
    }

    @Override
    public void onDestroy() {
        leafBitmap = null;
        photo.recycle();
        photo = null;
    }
}
