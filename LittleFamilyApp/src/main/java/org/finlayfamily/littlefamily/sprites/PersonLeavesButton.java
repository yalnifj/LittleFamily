package org.finlayfamily.littlefamily.sprites;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Rect;

import org.finlayfamily.littlefamily.data.LittlePerson;
import org.finlayfamily.littlefamily.events.EventQueue;
import org.finlayfamily.littlefamily.util.ImageHelper;

import java.util.List;

/**
 * Created by jfinlay on 7/16/2015.
 */
public class PersonLeavesButton extends Sprite {
    private List<LittlePerson> people;
    private List<Bitmap> bitmaps;
    private Bitmap photo;
    private int delay0 = 40;
    private int delay2 = 150;
    private int delay = 0;
    private int person = 0;
    private boolean moved = false;
    private String eventTopic;
    private int frame = 0;
    protected int stepsPerFrame;
    protected int steps;
    protected Context context;

    public PersonLeavesButton(String eventTopic, List<LittlePerson> people, List<Bitmap> bitmaps, Context context) {
        this.eventTopic = eventTopic;
        this.people = people;
        this.bitmaps = bitmaps;
        this.context = context;
        steps=0;
        stepsPerFrame = 4;
        width = bitmaps.get(0).getWidth();
        height = bitmaps.get(0).getHeight();
    }

    @Override
    public void doStep() {
        if (state==0) {
            delay++;
            frame=0;
            if (delay > delay0) {
                delay = 0;
                state=1;
                steps = 0;
            }
        }
        if (state==1) {
            steps++;
            if (steps >= stepsPerFrame) {
                steps = 0;
                frame++;
                if (frame >= bitmaps.size()) {
                    state = 2;
                }
            }
        }
        if (state==2) {
            frame = bitmaps.size()-1;
            delay++;
            if (delay > delay2) {
                delay = 0;
                state=3;
                steps = 0;
            }
        }
        if (state==3) {
            steps++;
            if (steps >= stepsPerFrame) {
                steps = 0;
                frame--;
                if (frame <0) {
                    state = 0;
                    frame = 0;
                    delay = 0;
                    person++;
                    photo = null;
                    if (person >= people.size()) {
                        person = 0;
                    }
                }
            }
        }
    }

    @Override
    public void doDraw(Canvas canvas) {
        if (photo==null) {
            if (people.get(person).getPhotoPath() != null) {
                photo = ImageHelper.loadBitmapFromFile(people.get(person).getPhotoPath(), ImageHelper.getOrientation(people.get(person).getPhotoPath()), (int) (width * 0.7), (int) (height * 0.7), false);
            }else {
                photo = ImageHelper.loadBitmapFromResource(context, people.get(person).getDefaultPhotoResource(), 0, (int)(width*0.7), (int) (height*0.7));
            }
        }

        if (matrix != null) {
            canvas.save();
            canvas.setMatrix(matrix);
        }
        if (photo!=null) {
            canvas.drawBitmap(photo, x + (getWidth() - photo.getWidth())/2, y + (getHeight() - photo.getHeight())/2, null);
        }
        if (frame >= 0 && frame < bitmaps.size()) {
            Rect rect = new Rect();
            rect.set((int)(x), (int)(y), (int)((x + width)), (int)((y + height)));
            Bitmap bitmap = bitmaps.get(frame);
            canvas.drawBitmap(bitmap, null, rect, null);
        }
        if (matrix != null) {
            canvas.restore();
        }
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
            EventQueue.getInstance().publish(eventTopic, this);
        }
        moved = false;
    }

    @Override
    public void onDestroy() {
        if (photo!=null) {
            photo.recycle();
            photo = null;
        }
        if (bitmaps!=null) {
            for (Bitmap b : bitmaps) {
                b.recycle();
            }
            bitmaps.clear();
        }
    }

}
