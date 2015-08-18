package com.yellowforktech.littlefamilytree.sprites;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.media.MediaPlayer;
import android.util.Log;

import com.yellowforktech.littlefamilytree.R;
import com.yellowforktech.littlefamilytree.activities.LittleFamilyActivity;
import com.yellowforktech.littlefamilytree.data.LittlePerson;
import com.yellowforktech.littlefamilytree.util.ImageHelper;
import com.yellowforktech.littlefamilytree.views.BubbleSpriteSurfaceView;

import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * Created by kids on 5/21/15.
 */
public class BubbleAnimatedBitmapSprite extends BouncingAnimatedBitmapSprite {

	private Bitmap photo;
	private LittlePerson person;
    private LittleFamilyActivity activity;
    private BubbleSpriteSurfaceView view;
    private int mx;
    private int my;
    private int oldFrame = 0;
    private int stepCount = 0;
    private int sWidth;
    private int sHeight;
    private int photoWidth;
    protected MediaPlayer mediaPlayer;
	
    public BubbleAnimatedBitmapSprite(Bitmap bitmap, int maxWidth, int maxHeight, LittleFamilyActivity activity, BubbleSpriteSurfaceView view, int photoWidth) {
        super(bitmap, maxWidth, maxHeight);
        setSelectable(true);
        this.activity = activity;
        setIgnoreAlpha(true);
        setStepsPerFrame(2);
        this.view = view;
        this.photoWidth = photoWidth;
    }

    public BubbleAnimatedBitmapSprite(Map<Integer, List<Bitmap>> bitmaps, int maxWidth, int maxHeight, LittleFamilyActivity activity, BubbleSpriteSurfaceView view, int photoWidth) {
        super(bitmaps, maxWidth, maxHeight);
        setSelectable(true);
        this.activity = activity;
        setIgnoreAlpha(true);
        setStepsPerFrame(2);
        this.view = view;
        this.photoWidth = photoWidth;
    }

    public int getMx() {
        return mx;
    }

    public void setMx(int mx) {
        this.mx = mx;
    }

    public int getMy() {
        return my;
    }

    public void setMy(int my) {
        this.my = my;
    }

    public BubbleSpriteSurfaceView getView() {
        return view;
    }

    public void setView(BubbleSpriteSurfaceView view) {
        this.view = view;
    }

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
				if (person.getGivenName()!=null) {
                    activity.speak(person.getGivenName());
                }
			}
        }
        if (state==2) {
            speed = (mx - (x + getWidth()/2))/10;
            slope = (my - (y + getHeight()/2))/10;
            if (speed > 15) speed=15;
            if (speed < -15) speed=-15;
            if (slope > 15) slope = 15;
            if (slope < -15) slope = -15;
            if (x + getWidth()/2 > mx - 3 && x + getWidth()/2 < mx +3 &&
                    y + getHeight()/2 > my - 3 && y + getHeight()/2 < my +3) {
                state = 3;
                slope = 0;
                speed = 0;
                view.nextBubble();
            }
        }
        if (stepCount > 0) {
            Random rand = new Random();
            x += 5 - rand.nextInt(10);
            y += 5 - rand.nextInt(10);
            width += 5 - rand.nextInt(10);
            height += 5 - rand.nextInt(10);
            stepCount--;
            if (stepCount==0) {
                width = sWidth;
                height = sHeight;
            }
        } else {
            sWidth = width;
            sHeight = height;
        }
        oldFrame = frame;
    }
	
	@Override
    public void doDraw(Canvas canvas) {
		if (photo!=null){
			Rect rect = new Rect();
            int px = (int)(x+width*0.2);
            int py = (int)(y+height*0.2);
			rect.set(px, py, px+photo.getWidth(), py+photo.getHeight());
			canvas.drawBitmap(photo, null, rect, null);
		}
		if (state<2) {
            super.doDraw(canvas);
		}
	}

    @Override
    public void onRelease(float x, float y) {
        super.onRelease(x, y);
        if (state==0) {
            if (person==null || person==view.getNextPerson()) {
                state = 1;

                try {
                    mediaPlayer = MediaPlayer.create(activity, R.raw.pop);
                    mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                        @Override
                        public void onCompletion(MediaPlayer mp) {
                            mp.release();
                        }
                    });
                    mediaPlayer.start();
                } catch (Exception e) {
                    // just let things go on
                    Log.e(getClass().getSimpleName(), "Error playing sound", e);
                }
            } else if (person!=null) {
                stepCount = 10;
				try {
                    mediaPlayer = MediaPlayer.create(activity, R.raw.nopop);
                    mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
							@Override
							public void onCompletion(MediaPlayer mp) {
								mp.release();
                                view.sayFindText();
							}
						});
                    mediaPlayer.start();
                } catch (Exception e) {
                    // just let things go on
                    Log.e(getClass().getSimpleName(), "Error playing sound", e);
                    view.sayFindText();
                }
            }
        }
    }
	
	
}
