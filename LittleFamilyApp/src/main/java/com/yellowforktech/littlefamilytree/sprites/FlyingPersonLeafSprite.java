package com.yellowforktech.littlefamilytree.sprites;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;

import com.yellowforktech.littlefamilytree.data.LittlePerson;
import com.yellowforktech.littlefamilytree.util.ImageHelper;

import java.util.List;
import java.util.Map;

/**
 * Created by jfinlay on 5/10/2016.
 */
public class FlyingPersonLeafSprite extends MovingAnimatedBitmapSprite {

    private LittlePerson person;
    private Bitmap photo;
    private Context context;

    public FlyingPersonLeafSprite(Bitmap bitmap, int maxWidth, int maxHeight, LittlePerson person, Context context) {
        super(bitmap, maxWidth, maxHeight);
        this.person = person;
        this.context = context;
        setup();
    }

    public FlyingPersonLeafSprite(Map<Integer, List<Bitmap>> bitmaps, int maxWidth, int maxHeight, LittlePerson person, Context context) {
        super(bitmaps, maxWidth, maxHeight);
        this.person = person;
        this.context = context;
        setup();
    }

    public void setup() {
        if (person.getPhotoPath() != null) {
            photo = ImageHelper.loadBitmapFromFile(person.getPhotoPath(), ImageHelper.getOrientation(person.getPhotoPath()), (int) (width/1.6), (int) (width/1.6), false);
        }
        if (photo == null) {
            photo = ImageHelper.loadBitmapFromResource(context, person.getDefaultPhotoResource(), 0, (int) (width/1.6), (int)(width/1.6));
        }
    }

    public LittlePerson getPerson() {
        return person;
    }

    public void setPerson(LittlePerson person) {
        this.person = person;
        setup();
    }

    @Override
    public void doDraw(Canvas canvas) {
        super.doDraw(canvas);

        canvas.drawBitmap(photo, x + (width/2 - photo.getWidth()/2), y + (height/2 - photo.getHeight()/2), null);
    }
}
