package com.yellowforktech.littlefamilytree.views;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.DisplayMetrics;

import com.yellowforktech.littlefamilytree.activities.LittleFamilyActivity;
import com.yellowforktech.littlefamilytree.data.LittlePerson;
import com.yellowforktech.littlefamilytree.events.EventListener;
import com.yellowforktech.littlefamilytree.events.EventQueue;
import com.yellowforktech.littlefamilytree.sprites.AnimatedBitmapSprite;
import com.yellowforktech.littlefamilytree.sprites.BubbleAnimatedBitmapSprite;
import com.yellowforktech.littlefamilytree.sprites.Sprite;
import com.yellowforktech.littlefamilytree.sprites.TouchStateAnimatedBitmapSprite;

import org.gedcomx.types.GenderType;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

/**
 * Created by kids on 6/17/15.
 */
public class BubbleSpriteSurfaceView extends SpritedSurfaceView implements EventListener {
    public static final String TOPIC_SOAP_ADDED = "soapAdded";
    public static final String TOPIC_WATER_ADDED = "waterAdded";

    private List<LittlePerson> parents;
    private List<LittlePerson> children;
    private List<LittlePerson> random;
    private Bitmap bubbleBm;
    private Bitmap spotBm;
    private Bitmap spotHBm;
    private Bitmap spotDownBm;
    private Bitmap spotDownHBm;
    private List<Bitmap> popping;
    private boolean spritesCreated = false;
    private boolean hasSoap;
    private boolean addBubbles;
    private int bubbleCount = 0;
    private int bubbleAddWait = 5;
    private int bubbleStep = 0;
    private int popped = 0;
    private AnimatedBitmapSprite fatherSpot;
    private AnimatedBitmapSprite motherSpot;
    private AnimatedBitmapSprite childSpot;
    private Paint spotPaint;
    private DisplayMetrics dm;
	private float bubbleScale =1.0f;

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

    @Override
    public void pause() {
        super.pause();
        EventQueue.getInstance().unSubscribe(TOPIC_SOAP_ADDED, this);
        EventQueue.getInstance().unSubscribe(TOPIC_WATER_ADDED, this);
    }

    @Override
    public void resume() {
        super.resume();
        EventQueue.getInstance().subscribe(TOPIC_SOAP_ADDED, this);
        EventQueue.getInstance().subscribe(TOPIC_WATER_ADDED, this);
    }

    public void setup() {
        listeners = new ArrayList<>(1);
        bubbleBm = BitmapFactory.decodeResource(context.getResources(), com.yellowforktech.littlefamilytree.R.drawable.bubble);
        backgroundBitmap = BitmapFactory.decodeResource(context.getResources(), com.yellowforktech.littlefamilytree.R.drawable.bubble_background);
        popping = new ArrayList<>(5);
        popping.add(BitmapFactory.decodeResource(context.getResources(), com.yellowforktech.littlefamilytree.R.drawable.bubble_pop1));
        popping.add(BitmapFactory.decodeResource(context.getResources(), com.yellowforktech.littlefamilytree.R.drawable.bubble_pop2));
        popping.add(BitmapFactory.decodeResource(context.getResources(), com.yellowforktech.littlefamilytree.R.drawable.bubble_pop3));
        popping.add(BitmapFactory.decodeResource(context.getResources(), com.yellowforktech.littlefamilytree.R.drawable.bubble_pop4));
        popping.add(BitmapFactory.decodeResource(context.getResources(), com.yellowforktech.littlefamilytree.R.drawable.bubble_pop5));
        hasSoap = false;
        addBubbles = false;
        multiSelect = false;
        spotPaint = new Paint();
        spotPaint.setColor(Color.parseColor("#ffd12d2d"));
    }

    public List<LittlePerson> getParents() {
        return parents;
    }

    public void setParentsAndChildren(List<LittlePerson> parents, List<LittlePerson> children) {
        this.parents = parents;
        this.children = children;
        this.random = new ArrayList<>(3);
        popped = 0;
        if (parents.size()>0) random.add(parents.get(0));
        if (parents.size()>1) random.add(parents.get(1));
        if (children.size()>0) random.add(children.get(0));
        Collections.shuffle(random);

        synchronized (sprites) {
            this.sprites.clear();
        }
        spritesCreated = false;
    }

    public List<LittlePerson> getChildren() {
        return children;
    }

    public LittlePerson getNextPerson() {
        return random.get(popped);
    }

    public void nextBubble() {
        synchronized (sprites) {
            int firstbubble = -1;
            //-- move popped bubbles behind unpopped bubbles
            for(int i=0; i<sprites.size()-1; i++) {
                Sprite s1 = sprites.get(i);
                if (s1 instanceof BubbleAnimatedBitmapSprite) {
                    if (firstbubble<0 && s1.getState()==0) firstbubble = i;

                    if (firstbubble>=0 && s1.getState()==3 && i>firstbubble) {
                        sprites.set(i, sprites.get(firstbubble));
                        sprites.set(firstbubble, s1);
                        firstbubble++;
                    }
                }
            }
        }
        popped++;
        if (popped>=random.size()) {
            for(BubbleCompleteListener l : listeners) {
                l.onBubbleComplete();
            }
        } else {
            if (parents.size() > 0 && parents.get(0) == getNextPerson()) {
                fatherSpot.setState(1);
            } else {
                fatherSpot.setState(0);
            }
            if (parents.size() > 1 && parents.get(1) == getNextPerson()) {
                motherSpot.setState(1);
            } else {
                motherSpot.setState(0);
            }
            if (children.size() > 0 && children.get(0) == getNextPerson()) {
                childSpot.setState(1);
            } else {
                childSpot.setState(0);
            }
            sayFindText();
        }
    }

    public void sayFindText() {
        if (parents.size() > 0 && parents.get(0) == getNextPerson()) {
            if (getNextPerson().getGender()== GenderType.Female) activity.speak(activity.getResources().getString(com.yellowforktech.littlefamilytree.R.string.who_is_mother));
            else activity.speak(activity.getResources().getString(com.yellowforktech.littlefamilytree.R.string.who_is_father));
        }
        if (parents.size() > 1 && parents.get(1) == getNextPerson()) {
            if (getNextPerson().getGender()== GenderType.Female) activity.speak(activity.getResources().getString(com.yellowforktech.littlefamilytree.R.string.who_is_mother));
            else activity.speak(activity.getResources().getString(com.yellowforktech.littlefamilytree.R.string.who_is_father));
        }
        if (children.size() > 0 && children.get(0) == getNextPerson()) {
            activity.speak(activity.getResources().getString(com.yellowforktech.littlefamilytree.R.string.who_is_child));
        }
    }

    public LittleFamilyActivity getActivity() {
        return activity;
    }

    public void setActivity(LittleFamilyActivity activity) {
        this.activity = activity;
        dm = new DisplayMetrics();
        activity.getWindowManager().getDefaultDisplay().getMetrics(dm);
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
                BubbleAnimatedBitmapSprite bubble = new BubbleAnimatedBitmapSprite(bubbleBm, getWidth(), getHeight(), activity, this, (int) (fatherSpot.getWidth()*0.8));
                bubble.getBitmaps().put(1, popping);
                int width = (int) (bubbleBm.getWidth() * bubbleScale * (0.5 + rand.nextFloat()));
                bubble.setWidth(width);
                bubble.setHeight(width);
                int slope = 5 - rand.nextInt(10);
                bubble.setSlope(slope);
                float speed = 5.0f - rand.nextFloat() * 10.0f;
                bubble.setSpeed(speed);
                bubble.setY(getHeight() * 0.70f);
                bubble.setX(getWidth() * 0.35f + rand.nextInt(60));
                addSprite(bubble);
                bubbleStep = 0;
            } else {
                bubbleStep++;
            }
        }
    }

    public void createSprites() {
		if (getHeight() > 800) {
			bubbleScale = (float) getHeight() / 800;
		}
        spotBm = BitmapFactory.decodeResource(getResources(), com.yellowforktech.littlefamilytree.R.drawable.bubble_spot);
        spotHBm = BitmapFactory.decodeResource(getResources(), com.yellowforktech.littlefamilytree.R.drawable.bubble_spot_h);
        spotDownBm = BitmapFactory.decodeResource(getResources(), com.yellowforktech.littlefamilytree.R.drawable.bubble_spot_down);
        spotDownHBm = BitmapFactory.decodeResource(getResources(), com.yellowforktech.littlefamilytree.R.drawable.bubble_spot_down_h);
        int spotWidth = getWidth()/5;
        if (spotWidth < spotBm.getWidth()) spotWidth = spotBm.getWidth();
        float spotRatio = (float)spotBm.getWidth() / spotBm.getHeight();
        int spotHeight = (int) (spotWidth / spotRatio);
        if (spotHeight < spotBm.getHeight()) spotHeight = spotBm.getHeight();

        fatherSpot = new AnimatedBitmapSprite(spotBm);
        fatherSpot.setWidth(spotWidth);
        fatherSpot.setHeight(spotHeight);
        List<Bitmap> highlighted = new ArrayList<>(1);
        highlighted.add(spotHBm);
        fatherSpot.getBitmaps().put(1, highlighted);
        int fx = (int) ((getWidth()/2)-(spotWidth*1.3f));
        int fy = getHeight()/10;
        fatherSpot.setX(fx);
        fatherSpot.setY(fy);
        addSprite(fatherSpot);

        motherSpot = new AnimatedBitmapSprite(spotBm);
        motherSpot.getBitmaps().put(1, highlighted);
        motherSpot.setWidth(spotWidth);
        motherSpot.setHeight(spotHeight);
        int mx = (int) ((getWidth()/2)+(spotWidth*0.3f));
        int my = getHeight()/10;
        motherSpot.setX(mx);
        motherSpot.setY(my);
        addSprite(motherSpot);

        childSpot = new AnimatedBitmapSprite(spotDownBm);
        childSpot.setWidth(spotWidth);
        childSpot.setHeight(spotHeight);
        List<Bitmap> highlightedDown = new ArrayList<>(1);
        highlightedDown.add(spotDownHBm);
        childSpot.getBitmaps().put(1, highlightedDown);
        int cx = (int) ((getWidth()/2)-(spotWidth/2));
        int cy = spotHeight+getHeight()/10;
        childSpot.setX(cx);
        childSpot.setY(cy);
        addSprite(childSpot);

        Bitmap sinkBm = BitmapFactory.decodeResource(getResources(), com.yellowforktech.littlefamilytree.R.drawable.sink);
        AnimatedBitmapSprite sink = new AnimatedBitmapSprite(sinkBm);
        int swidth = (int) (getWidth()-(20*dm.density));
        float r = (float) sinkBm.getWidth() / sinkBm.getHeight();
        int sheight  = (int)(swidth / r);
        sink.setWidth(swidth);
        sink.setHeight(sheight);
        sink.setX(10*dm.density);
        sink.setY(getHeight()-sheight);
        addSprite(sink);

        Bitmap faucet1bm = BitmapFactory.decodeResource(getResources(), com.yellowforktech.littlefamilytree.R.drawable.faucet1);
        TouchStateAnimatedBitmapSprite faucet = new TouchStateAnimatedBitmapSprite(faucet1bm, activity);
        float fr = (float) faucet1bm.getWidth() / (float) faucet1bm.getHeight();
        int fheight  = (int)(getHeight()/2 - sink.getHeight()*0.75f);
        int fwidth = (int) (fheight * fr);
        float scale = (float) fheight / faucet1bm.getHeight();
		//faucet.setScale((float)fheight/faucet1bm.getHeight());
        faucet.setWidth(fwidth);
        faucet.setHeight(fheight);
        faucet.setX(sink.getX() + sink.getWidth() / 2);
        faucet.setY(getHeight()/2);
        List<Bitmap> turning = new ArrayList<>(2);
        turning.add(BitmapFactory.decodeResource(getResources(), com.yellowforktech.littlefamilytree.R.drawable.faucet2));
        turning.add(faucet1bm);
        faucet.getBitmaps().put(1, turning);
        faucet.setStateTransition(1, TouchStateAnimatedBitmapSprite.TRANSITION_LOOP3);
        List<Bitmap> water = new ArrayList<>(2);
        water.add(BitmapFactory.decodeResource(getResources(), com.yellowforktech.littlefamilytree.R.drawable.faucet3));
        water.add(BitmapFactory.decodeResource(getResources(), com.yellowforktech.littlefamilytree.R.drawable.faucet4));
        faucet.getBitmaps().put(2, water);
        faucet.setStateTransition(2, TouchStateAnimatedBitmapSprite.TRANSITION_LOOP1);
		faucet.getAudio().put(2, com.yellowforktech.littlefamilytree.R.raw.water);
        List<Bitmap> water2 = new ArrayList<>(2);
        water2.add(BitmapFactory.decodeResource(getResources(), com.yellowforktech.littlefamilytree.R.drawable.faucet5));
        water2.add(BitmapFactory.decodeResource(getResources(), com.yellowforktech.littlefamilytree.R.drawable.faucet6));
        faucet.getBitmaps().put(3, water2);
        faucet.setStateTransition(3, TouchStateAnimatedBitmapSprite.TRANSITION_LOOP3);
        List<Bitmap> rwater = new ArrayList<>(water);
        Collections.reverse(rwater);
        faucet.getBitmaps().put(4, rwater);
        faucet.setStateTransition(4, TouchStateAnimatedBitmapSprite.TRANSITION_LOOP1);
        faucet.getBitmaps().put(5, turning);
        faucet.setStateTransition(5, TouchStateAnimatedBitmapSprite.TRANSITION_LOOP3);
        faucet.setStateTransitionEvent(4, TOPIC_WATER_ADDED);
        Rect touchRect = new Rect();
        touchRect.set((int) (42*scale*dm.density), (int) (45*scale*dm.density), faucet.getWidth()-10, (int) (faucet.getHeight()*0.8f));
        faucet.setTouchRectangles(0, touchRect);
        addSprite(faucet);

        Bitmap soap1bm = BitmapFactory.decodeResource(getResources(), com.yellowforktech.littlefamilytree.R.drawable.soap1);
        TouchStateAnimatedBitmapSprite soap = new TouchStateAnimatedBitmapSprite(soap1bm, activity);
        float pr = (float)soap1bm.getWidth() / soap1bm.getHeight();
        int pheight  = (int)(faucet.getHeight()/2);
        int pwidth = (int) (pheight * pr);
        soap.setWidth(pwidth);
        soap.setHeight(pheight);
		//soap.setScale((float)pheight/soap1bm.getHeight());
        soap.setX(faucet.getX() + faucet.getWidth() / 2);
        soap.setY(faucet.getY() + faucet.getHeight() - pheight + 5 * dm.density);
        List<Bitmap> squirting = new ArrayList<>(4);
        squirting.add(BitmapFactory.decodeResource(getResources(), com.yellowforktech.littlefamilytree.R.drawable.soap2));
        squirting.add(BitmapFactory.decodeResource(getResources(), com.yellowforktech.littlefamilytree.R.drawable.soap3));
        squirting.add(BitmapFactory.decodeResource(getResources(), com.yellowforktech.littlefamilytree.R.drawable.soap4));
        squirting.add(BitmapFactory.decodeResource(getResources(), com.yellowforktech.littlefamilytree.R.drawable.soap5));
        soap.getBitmaps().put(1, squirting);
        soap.setStateTransition(1, TouchStateAnimatedBitmapSprite.TRANSITION_LOOP1);
		soap.getAudio().put(1, com.yellowforktech.littlefamilytree.R.raw.squirt);
        List<Bitmap> squirting2 = new ArrayList<>(2);
        squirting2.add(BitmapFactory.decodeResource(getResources(), com.yellowforktech.littlefamilytree.R.drawable.soap6));
        squirting2.add(BitmapFactory.decodeResource(getResources(), com.yellowforktech.littlefamilytree.R.drawable.soap7));
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
        touchRect.set((int) (soap.getWidth()*0.65f), 0, soap.getWidth(), soap.getHeight());
        soap.setTouchRectangles(0, touchRect);
        addSprite(soap);

        Random rand = new Random();
        int count = rand.nextInt(8) + 4;
        for (int b = 0; b < count; b++) {
            BubbleAnimatedBitmapSprite bubble = new BubbleAnimatedBitmapSprite(bubbleBm, getWidth(), getHeight(), activity, this, (int) (spotWidth*0.8));
            bubble.getBitmaps().put(1, popping);
            int width = (int) (bubbleBm.getWidth() * bubbleScale * (0.5 + rand.nextFloat()));
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
		
		if (random!=null) {
            count = 0;
			for (LittlePerson person : random) {
                BubbleAnimatedBitmapSprite bubble = new BubbleAnimatedBitmapSprite(bubbleBm, getWidth(), getHeight(), activity, this, (int) (spotWidth*0.8));
                bubble.getBitmaps().put(1, popping);
                int slope = 5 - rand.nextInt(10);
                bubble.setSlope(slope);
                float speed = 5.0f - rand.nextFloat() * 10.0f;
                bubble.setSpeed(speed);
				bubble.setWidth((int)(bubbleBm.getWidth()* bubbleScale));
				bubble.setHeight((int)(bubbleBm.getHeight()* bubbleScale));
                bubble.setY(rand.nextInt(getHeight() - bubble.getHeight()));
                bubble.setX(rand.nextInt(getWidth() - bubble.getWidth()));
                bubble.setPerson(person);
                if (parents.size() > 0 && parents.get(0) == person) {
                    bubble.setMx((int) (fatherSpot.getX() + (fatherSpot.getWidth()/2)));
                    bubble.setMy((int) (fatherSpot.getY() + (fatherSpot.getWidth()/2)));
                    if (count==0) {
                        fatherSpot.setState(1);
                        if (person.getGender()== GenderType.Female) activity.speak(activity.getResources().getString(com.yellowforktech.littlefamilytree.R.string.who_is_mother));
                        else activity.speak(activity.getResources().getString(com.yellowforktech.littlefamilytree.R.string.who_is_father));
                    }
                }
                if (parents.size() > 1 && parents.get(1) == person) {
                    bubble.setMx((int) (motherSpot.getX() + (motherSpot.getWidth()/2)));
                    bubble.setMy((int) (motherSpot.getY() + (motherSpot.getWidth()/2)));
                    if (count==0) {
                        motherSpot.setState(1);
                        if (person.getGender()== GenderType.Female) activity.speak(activity.getResources().getString(com.yellowforktech.littlefamilytree.R.string.who_is_mother));
                        else activity.speak(activity.getResources().getString(com.yellowforktech.littlefamilytree.R.string.who_is_father));
                    }
                }
                if (children.size() > 0 && children.get(0) == person) {
                    bubble.setMx((int) (childSpot.getX() + (childSpot.getWidth()/2)));
                    bubble.setMy((int) (childSpot.getY() + childSpot.getHeight()/2));
                    if (count==0) {
                        childSpot.setState(1);
                        activity.speak(activity.getResources().getString(com.yellowforktech.littlefamilytree.R.string.who_is_child));
                    }
                }
                addSprite(bubble);
                count++;
            }
		}
        spritesCreated = true;
    }

    @Override
    public void doDraw(Canvas canvas) {
        if (!spritesCreated) {
            createSprites();
        }

        if (backgroundBitmap!=null) {
            Rect rect = new Rect();
            rect.set(0,0,getWidth(),getHeight());
            canvas.drawBitmap(backgroundBitmap, null, rect, basePaint);
        } else {
            basePaint.setColor(Color.WHITE);
            canvas.drawRect(0,0,getWidth(),getHeight(),basePaint);
        }

        canvas.drawRect(fatherSpot.getX()+(35*dm.density), childSpot.getY()-(5*dm.density), motherSpot.getX()+motherSpot.getWidth()-(35*dm.density), childSpot.getY()+(5*dm.density), spotPaint);

        synchronized (sprites) {
            for (Sprite s : sprites) {
                if (s.getX() + s.getWidth() >= 0 && s.getX() <= getWidth() && s.getY() + s.getHeight() >= 0 && s.getY() <= getHeight()) {
                    s.doDraw(canvas);
                }
            }
        }
    }
}
