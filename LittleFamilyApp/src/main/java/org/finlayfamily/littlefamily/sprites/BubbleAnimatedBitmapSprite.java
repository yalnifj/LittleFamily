package org.finlayfamily.littlefamily.sprites;

import android.graphics.Bitmap;

import java.util.List;
import java.util.Map;

/**
 * Created by kids on 5/21/15.
 */
public class BubbleAnimatedBitmapSprite extends BouncingAnimatedBitmapSprite {

    public BubbleAnimatedBitmapSprite(Bitmap bitmap, int maxWidth, int maxHeight) {
        super(bitmap, maxWidth, maxHeight);
        setSelectable(true);
    }

    public BubbleAnimatedBitmapSprite(Map<Integer, List<Bitmap>> bitmaps, int maxWidth, int maxHeight) {
        super(bitmaps, maxWidth, maxHeight);
        setSelectable(true);
    }

    private int oldFrame = 0;
    @Override
    public void doStep() {
        super.doStep();

        if (state==1 && frame==0 && oldFrame!=0) {
            frame = 5;
            setRemoveMe(true);
        }
        oldFrame = frame;
    }

    @Override
    public void onRelease(float x, float y) {
        super.onRelease(x, y);
        state = 1;
    }
}
