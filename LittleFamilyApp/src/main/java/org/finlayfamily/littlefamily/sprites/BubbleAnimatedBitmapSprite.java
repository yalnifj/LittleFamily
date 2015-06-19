package org.finlayfamily.littlefamily.sprites;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Rect;
import java.util.List;
import java.util.Map;
import org.finlayfamily.littlefamily.data.LittlePerson;
import org.finlayfamily.littlefamily.util.ImageHelper;

/**
 * Created by kids on 5/21/15.
 */
public class BubbleAnimatedBitmapSprite extends BouncingAnimatedBitmapSprite {

	private Bitmap photo;
	private LittlePerson person;
	
    public BubbleAnimatedBitmapSprite(Bitmap bitmap, int maxWidth, int maxHeight) {
        super(bitmap, maxWidth, maxHeight);
        setSelectable(true);
    }

    public BubbleAnimatedBitmapSprite(Map<Integer, List<Bitmap>> bitmaps, int maxWidth, int maxHeight) {
        super(bitmaps, maxWidth, maxHeight);
        setSelectable(true);
    }

    private int oldFrame = 0;

	public void setPerson(LittlePerson person)
	{
		this.person = person;
		photo = null;
        if (person.getPhotoPath() != null) {
            photo = ImageHelper.loadBitmapFromFile(person.getPhotoPath(), ImageHelper.getOrientation(person.getPhotoPath()), (int) (width * 0.7), (int) (height * 0.7), false);
        }
        if (photo==null){
            photo = ImageHelper.loadBitmapFromResource(activity, person.getDefaultPhotoResource(), 0, (int)(width*0.7), (int) (height*0.7));
        }
	}

	public LittlePerson getPerson()
	{
		return person;
	}
	
    @Override
    public void doStep() {
        super.doStep();

        if (state==1 && frame==0 && oldFrame!=0) {
            frame = 5;
			if (person==null) {
            	setRemoveMe(true);
			} else {
				state = 2;
				slope = 0;
				speed = 0;
			}
        }
        oldFrame = frame;
    }
	
	@Override
    public void doDraw(Canvas canvas) {
		if (photo!=null){
			Rect rect = new Rect();
			rect.set((int)(x+width*0.3), (int)(y+height*0.3), photo.getWidth(), photo.getHeight());
			canvas.drawBitmap(photo, null, rect, null);
		}
		if (state<2) {
			super.doDraw(canvas);
		}
	}

    @Override
    public void onRelease(float x, float y) {
        super.onRelease(x, y);
        state = 1;
    }
	
	
}
