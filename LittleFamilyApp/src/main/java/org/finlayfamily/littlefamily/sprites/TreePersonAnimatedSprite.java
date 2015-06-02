package org.finlayfamily.littlefamily.sprites;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;

import org.finlayfamily.littlefamily.activities.LittleFamilyActivity;
import org.finlayfamily.littlefamily.data.LittlePerson;
import org.finlayfamily.littlefamily.data.TreeNode;
import org.finlayfamily.littlefamily.util.ImageHelper;

/**
 * Created by Parents on 5/29/2015.
 */
public class TreePersonAnimatedSprite extends Sprite {
    protected LittlePerson person;
    protected TreeNode node;
    protected LittleFamilyActivity activity;
    protected Bitmap leafBitmap;
    protected Bitmap photo;
    protected boolean moved;
    protected Paint textPaint;

    public static final int STATE_CLOSED = 0;
    public static final int STATE_ANIMATING_OPEN = 1;
    public static final int STATE_OPEN = 2;
    public static final int STATE_ANIMATING_CLOSED = 3;

    public TreePersonAnimatedSprite(TreeNode personNode, LittleFamilyActivity activity, Bitmap leafBitmap) {
        this.person = personNode.getPerson();
        this.node = personNode;
        this.activity = activity;
        this.leafBitmap = leafBitmap;
        this.selectable = true;
        this.setWidth(leafBitmap.getWidth());
        this.setHeight(leafBitmap.getHeight());

        photo = null;
        if (person.getPhotoPath() != null) {
            photo = ImageHelper.loadBitmapFromFile(person.getPhotoPath(), ImageHelper.getOrientation(person.getPhotoPath()), (int) (width * 0.7), (int) (height * 0.7), false);
        } else {
            photo = ImageHelper.loadBitmapFromResource(activity, person.getDefaultPhotoResource(), 0, (int)(width*0.7), (int) (height*0.7));
        }

        textPaint = new Paint();
        textPaint.setColor(Color.WHITE);
        textPaint.setTextSize(32);
        textPaint.setTextAlign(Paint.Align.CENTER);
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
        float ratio = ((float)photo.getWidth())/photo.getHeight();
        int pw = (int) (width*0.7f);
        int ph = (int) (width*0.7f);
        if (photo.getWidth() > photo.getHeight()) {
            ph = (int) (pw / ratio);
        } else {
            pw = (int) (ph * ratio);
        }
        int px = (int) (getX() + (width/2  - pw/2));
        int py = (int) (getY() + (height/2 - ph/2));
        photoRect.set(px, py, px + pw, py + ph);
        canvas.drawBitmap(photo, null, photoRect, null);

        canvas.drawText(person.getGivenName(), getX() + width/2, getY()+height, textPaint);
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
