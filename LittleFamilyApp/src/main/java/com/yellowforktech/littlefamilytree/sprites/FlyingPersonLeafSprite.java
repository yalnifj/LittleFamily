package com.yellowforktech.littlefamilytree.sprites;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Rect;

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
            photo = ImageHelper.loadBitmapFromFile(person.getPhotoPath(), ImageHelper.getOrientation(person.getPhotoPath()), (int) (width * 0.5), (int) (width * 0.5), false);
        }
        if (photo == null) {
            photo = ImageHelper.loadBitmapFromResource(context, person.getDefaultPhotoResource(), 0, (int) (width * 0.5), (int)(width * 0.5));
        }
    }

    public LittlePerson getPerson() {
        return person;
    }

    public void setPerson(LittlePerson person) {
        this.person = person;
        setup();
    }

    public void setMatrix(Matrix matrix) {
        this.matrix = matrix;
    }

    @Override
    public void buildMatrix() {

    }

    @Override
    public void doStep() {
        super.doStep();
    }

    @Override
    public void doDraw(Canvas canvas) {
        super.doDraw(canvas);

        int left = (int)(x + (width/2 - width/4));
        int top = (int)(y + (height/2 - width/4));
        Rect r = new Rect(left, top, left + (int)(width * 0.5), top + (int)(width * 0.5));
        if (matrix != null) {
            canvas.save();
            canvas.setMatrix(matrix);
        }
        canvas.drawBitmap(photo, null, r, null);
        if (matrix != null) {
            canvas.restore();
        }
    }
}
