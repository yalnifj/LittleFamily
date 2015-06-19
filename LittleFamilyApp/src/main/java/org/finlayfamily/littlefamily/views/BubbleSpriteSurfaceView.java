package org.finlayfamily.littlefamily.views;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.util.AttributeSet;

import org.finlayfamily.littlefamily.R;
import org.finlayfamily.littlefamily.activities.LittleFamilyActivity;
import org.finlayfamily.littlefamily.data.LittlePerson;
import org.finlayfamily.littlefamily.events.EventListener;
import org.finlayfamily.littlefamily.events.EventQueue;
import org.finlayfamily.littlefamily.sprites.AnimatedBitmapSprite;
import org.finlayfamily.littlefamily.sprites.BubbleAnimatedBitmapSprite;
import org.finlayfamily.littlefamily.sprites.TouchStateAnimatedBitmapSprite;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

/**
 * Created by kids on 6/17/15.
 */
public class BubbleSpriteSurfaceView extends SpritedSurfaceView implements EventListener{
    public static final String TOPIC_SOAP_ADDED = "soapAdded";
    public static final String TOPIC_WATER_ADDED = "waterAdded";

    private List<LittlePerson> parents;
    private List<LittlePerson> children;
    private Bitmap bubbleBm;
    private List<Bitmap> popping;
    private boolean spritesCreated = false;
    private boolean hasSoap;
    private boolean addBubbles;
    private int bubbleCount = 0;
    private int bubbleAddWait = 3;
    private int bubbleStep = 0;

    private LittleFamilyActivity activity;
    private List<BubbleCompleteListener> listeners;

    public BubbleSpriteSurfaceView(Context context) {
        super(context);
        setup();
    }

    public BubbleSpriteSurfaceView(Context context, AttributeSet attrs) {
        super(context, attrs);
        setup();
    }

    public void setup() {
        listeners = new ArrayList<>(1);
        bubbleBm = BitmapFactory.decodeResource(context.getResources(), R.drawable.bubble);
        backgroundBitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.bubble_background);
        popping = new ArrayList<>(5);
        popping.add(BitmapFactory.decodeResource(context.getResources(), R.drawable.bubble_pop1));
        popping.add(BitmapFactory.decodeResource(context.getResources(), R.drawable.bubble_pop2));
        popping.add(BitmapFactory.decodeResource(context.getResources(), R.drawable.bubble_pop3));
        popping.add(BitmapFactory.decodeResource(context.getResources(), R.drawable.bubble_pop4));
        popping.add(BitmapFactory.decodeResource(context.getResources(), R.drawable.bubble_pop5));
        EventQueue.getInstance().subscribe(TOPIC_SOAP_ADDED, this);
        EventQueue.getInstance().subscribe(TOPIC_WATER_ADDED, this);
        hasSoap = false;
        addBubbles = false;
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

    @Override
    public void onEvent(String topic, Object o) {
        switch (topic) {
            case TOPIC_SOAP_ADDED:
                hasSoap = true;
                break;
            case TOPIC_WATER_ADDED:
                if (hasSoap) {
                    addBubbles = true;
                }
                hasSoap = false;
                break;
        }
    }

    public interface BubbleCompleteListener {
        public void onBubbleComplete();
    }

    @Override
    public void doStep() {
        super.doStep();
        //-- add more random bubbles
        if (addBubbles) {
            Random rand = new Random();
            bubbleCount = rand.nextInt(5) + 4;
            addBubbles = false;
            bubbleStep = 0;
        }
        if (bubbleCount>0) {
            if (bubbleStep>=bubbleAddWait) {
                bubbleCount--;
                Random rand = new Random();
                BubbleAnimatedBitmapSprite bubble = new BubbleAnimatedBitmapSprite(bubbleBm, getWidth(), getHeight(), activity);
                bubble.getBitmaps().put(1, popping);
                int width = (int) (bubbleBm.getWidth() * (0.5 + rand.nextFloat()));
                bubble.setWidth(width);
                bubble.setHeight(width);
                int slope = 5 - rand.nextInt(10);
                bubble.setSlope(slope);
                float speed = 5.0f - rand.nextFloat() * 10.0f;
                bubble.setSpeed(speed);
                bubble.setY(getHeight() * 0.75f);
                bubble.setX(getWidth() * 0.4f + rand.nextInt(50));
                addSprite(bubble);
                bubbleStep = 0;
            } else {
                bubbleStep++;
            }
        }
    }

    public void createSprites() {

        Bitmap sinkBm = BitmapFactory.decodeResource(getResources(), R.drawable.sink);
        AnimatedBitmapSprite sink = new AnimatedBitmapSprite(sinkBm);
        int swidth = getWidth()-40;
        float r = (float) sinkBm.getWidth() / sinkBm.getHeight();
        int sheight  = (int)(swidth / r);
        sink.setWidth(swidth);
        sink.setHeight(sheight);
        sink.setX(20);
        sink.setY(getHeight()-sheight);
        addSprite(sink);

        Bitmap faucet1bm = BitmapFactory.decodeResource(getResources(), R.drawable.faucet1);
        TouchStateAnimatedBitmapSprite faucet = new TouchStateAnimatedBitmapSprite(faucet1bm, activity);
        float fr = (float) faucet1bm.getWidth() / (float) faucet1bm.getHeight();
        int fwidth = 230;
        int fheight  = (int)(fwidth / fr);
        faucet.setWidth(fwidth);
        faucet.setHeight(fheight);
        faucet.setX(sink.getX() + sink.getWidth() / 2);
        faucet.setY(sink.getY() - fheight + 50);
        List<Bitmap> turning = new ArrayList<>(2);
        turning.add(BitmapFactory.decodeResource(getResources(), R.drawable.faucet2));
        turning.add(faucet1bm);
        faucet.getBitmaps().put(1, turning);
        faucet.setStateTransition(1, TouchStateAnimatedBitmapSprite.TRANSITION_LOOP3);
        List<Bitmap> water = new ArrayList<>(2);
        water.add(BitmapFactory.decodeResource(getResources(), R.drawable.faucet3));
        water.add(BitmapFactory.decodeResource(getResources(), R.drawable.faucet4));
        faucet.getBitmaps().put(2, water);
        faucet.setStateTransition(2, TouchStateAnimatedBitmapSprite.TRANSITION_LOOP1);
        List<Bitmap> water2 = new ArrayList<>(2);
        water2.add(BitmapFactory.decodeResource(getResources(), R.drawable.faucet5));
        water2.add(BitmapFactory.decodeResource(getResources(), R.drawable.faucet6));
        faucet.getBitmaps().put(3, water2);
        faucet.setStateTransition(3, TouchStateAnimatedBitmapSprite.TRANSITION_LOOP3);
        List<Bitmap> rwater = new ArrayList<>(water);
        Collections.reverse(rwater);
        faucet.getBitmaps().put(4, rwater);
        faucet.setStateTransition(4, TouchStateAnimatedBitmapSprite.TRANSITION_LOOP1);
        faucet.getBitmaps().put(5, turning);
        faucet.setStateTransition(4, TouchStateAnimatedBitmapSprite.TRANSITION_LOOP3);
        faucet.setStateTransitionEvent(0, TOPIC_WATER_ADDED);
        Rect touchRect = new Rect();
        touchRect.set(80, 80, faucet1bm.getWidth()-5, (int) (faucet1bm.getHeight()*0.8f));
        faucet.setTouchRectangles(0, touchRect);
        addSprite(faucet);

        Bitmap soap1bm = BitmapFactory.decodeResource(getResources(), R.drawable.soap1);
        TouchStateAnimatedBitmapSprite soap = new TouchStateAnimatedBitmapSprite(soap1bm, activity);
        float pr = (float)soap1bm.getWidth() / soap1bm.getHeight();
        int pwidth = 230;
        int pheight  = (int)(pwidth / pr);
        soap.setWidth(pwidth);
        soap.setHeight(pheight);
        soap.setX(sink.getX() + sink.getWidth() + 20 - pwidth);
        soap.setY(sink.getY() - pheight * 0.75f);
        List<Bitmap> squirting = new ArrayList<>(4);
        squirting.add(BitmapFactory.decodeResource(getResources(), R.drawable.soap2));
        squirting.add(BitmapFactory.decodeResource(getResources(), R.drawable.soap3));
        squirting.add(BitmapFactory.decodeResource(getResources(), R.drawable.soap4));
        squirting.add(BitmapFactory.decodeResource(getResources(), R.drawable.soap5));
        soap.getBitmaps().put(1, squirting);
        soap.setStateTransition(1, TouchStateAnimatedBitmapSprite.TRANSITION_LOOP1);
        List<Bitmap> squirting2 = new ArrayList<>(2);
        squirting2.add(BitmapFactory.decodeResource(getResources(), R.drawable.soap6));
        squirting2.add(BitmapFactory.decodeResource(getResources(), R.drawable.soap7));
        soap.getBitmaps().put(2, squirting2);
        soap.setStateTransition(2, TouchStateAnimatedBitmapSprite.TRANSITION_LOOP1);
        List<Bitmap> rsquirting2 = new ArrayList<>(squirting2);
        Collections.reverse(rsquirting2);
        soap.getBitmaps().put(3, rsquirting2);
        soap.setStateTransition(3, TouchStateAnimatedBitmapSprite.TRANSITION_LOOP1);
        List<Bitmap> rsquirting = new ArrayList<>(squirting);
        Collections.reverse(rsquirting);
        soap.getBitmaps().put(4, rsquirting);
        soap.setStateTransition(4, TouchStateAnimatedBitmapSprite.TRANSITION_LOOP1);
        soap.setStateTransitionEvent(4, TOPIC_SOAP_ADDED);
        touchRect = new Rect();
        touchRect.set((int) (soap1bm.getWidth()*0.5f), 0, soap1bm.getWidth(), soap1bm.getHeight());
        soap.setTouchRectangles(0, touchRect);
        addSprite(soap);

        Random rand = new Random();
        int count = rand.nextInt(8) + 4;
        for (int b = 0; b < count; b++) {
            BubbleAnimatedBitmapSprite bubble = new BubbleAnimatedBitmapSprite(bubbleBm, getWidth(), getHeight(), activity);
            bubble.getBitmaps().put(1, popping);
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
		
		if (parents!=null) {
			for (LittlePerson parent : parents) {
				BubbleAnimatedBitmapSprite bubble = new BubbleAnimatedBitmapSprite(bubbleBm, getWidth(), getHeight(), activity);
				bubble.getBitmaps().put(1, popping);
				int slope = 5 - rand.nextInt(10);
				bubble.setSlope(slope);
				float speed = 5.0f - rand.nextFloat()*10.0f;
				bubble.setSpeed(speed);
				bubble.setY(rand.nextInt(getHeight()-bubble.getHeight()));
				bubble.setX(rand.nextInt(getWidth()-bubble.getWidth()));
				bubble.setPerson(parent);
				addSprite(bubble);
			}
		}
		
		if (children!=null) {
			for (LittlePerson child : children) {
				BubbleAnimatedBitmapSprite bubble = new BubbleAnimatedBitmapSprite(bubbleBm, getWidth(), getHeight(), activity);
				bubble.getBitmaps().put(1, popping);
				int slope = 5 - rand.nextInt(10);
				bubble.setSlope(slope);
				float speed = 5.0f - rand.nextFloat()*10.0f;
				bubble.setSpeed(speed);
				bubble.setY(rand.nextInt(getHeight()-bubble.getHeight()));
				bubble.setX(rand.nextInt(getWidth()-bubble.getWidth()));
				bubble.setPerson(child);
				addSprite(bubble);
			}
		}
        spritesCreated = true;
    }

    @Override
    public void doDraw(Canvas canvas) {
        if (!spritesCreated) {
            createSprites();
        }

        super.doDraw(canvas);
    }
}
