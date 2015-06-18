package org.finlayfamily.littlefamily.views;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.util.AttributeSet;

import org.finlayfamily.littlefamily.R;
import org.finlayfamily.littlefamily.data.LittlePerson;
import org.finlayfamily.littlefamily.sprites.BouncingAnimatedBitmapSprite;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Created by kids on 6/17/15.
 */
public class BubbleSpriteSurfaceView extends SpritedSurfaceView {

    private List<LittlePerson> parents;
    private List<LittlePerson> children;
    private Bitmap bubbleBm;
    private boolean spritesCreated = false;

    private List<BubbleCompleteListener> listeners;

    public BubbleSpriteSurfaceView(Context context) {
        super(context);
        listeners = new ArrayList<>(1);
        bubbleBm = BitmapFactory.decodeResource(context.getResources(), R.drawable.bubble);
        backgroundBitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.bubble_background);
    }

    public BubbleSpriteSurfaceView(Context context, AttributeSet attrs) {
        super(context, attrs);
        listeners = new ArrayList<>(1);
        bubbleBm = BitmapFactory.decodeResource(context.getResources(), R.drawable.bubble);
        backgroundBitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.bubble_background);
    }

    public List<LittlePerson> getParents() {
        return parents;
    }

    public void setParentsAndChildren(List<LittlePerson> parents, List<LittlePerson> children) {
        this.parents = parents;
        this.children = children;

        synchronized (sprites) {
            this.sprites.clear();
        }
        spritesCreated = false;
    }

    public List<LittlePerson> getChildren() {
        return children;
    }

    public void registerListener(BubbleCompleteListener l) {
        listeners.add(l);
    }

    public interface BubbleCompleteListener {
        public void onBubbleComplete();
    }

    @Override
    public void doStep() {
        super.doStep();
        //-- add random bubbles
    }

    @Override
    public void doDraw(Canvas canvas) {
        if (!spritesCreated) {
            Random rand = new Random();
            int count = rand.nextInt(8) + 4;
            for (int b = 0; b < count; b++) {
                BouncingAnimatedBitmapSprite bubble = new BouncingAnimatedBitmapSprite(bubbleBm, getWidth(), getHeight());
                int width = (int) (bubbleBm.getWidth() * (0.5 + rand.nextFloat()));
                bubble.setWidth(width);
                bubble.setHeight(width);
                int slope = 5 - rand.nextInt(10);
                bubble.setSlope(slope);
                float speed = 5.0f - rand.nextFloat()*10.0f;
                bubble.setSpeed(speed);
                bubble.setY(rand.nextInt(getHeight()-bubble.getHeight()));
                bubble.setX(rand.nextInt(getWidth()-bubble.getWidth()));
                addSprite(bubble);
            }
            spritesCreated = true;
        }

        super.doDraw(canvas);
    }
}
