package org.finlayfamily.littlefamily.activities;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.View;

import org.finlayfamily.littlefamily.R;
import org.finlayfamily.littlefamily.data.LittlePerson;
import org.finlayfamily.littlefamily.events.EventListener;
import org.finlayfamily.littlefamily.events.EventQueue;
import org.finlayfamily.littlefamily.sprites.AnimatedBitmapSprite;
import org.finlayfamily.littlefamily.sprites.ClippedAnimatedBitmapSprite;
import org.finlayfamily.littlefamily.sprites.MatchGameSprite;
import org.finlayfamily.littlefamily.sprites.MovingAnimatedBitmapSprite;
import org.finlayfamily.littlefamily.sprites.TouchStateAnimatedBitmapSprite;
import org.finlayfamily.littlefamily.views.SpritedClippedSurfaceView;

import java.util.ArrayList;
import java.util.List;

public class HomeActivity extends LittleFamilyActivity implements EventListener {
    public static final String TOPIC_START_MATCH    = "startMatch";
    public static final String TOPIC_START_SCRATCH  = "startScratch";
    public static final String TOPIC_START_COLORING = "startColoring";
    public static final String TOPIC_START_DRESSUP  = "startDressUp";
    public static final String TOPIC_START_PUZZLE   = "startPuzzle";

    private LittlePerson selectedPerson;
    private ArrayList<LittlePerson> people;

    private SpritedClippedSurfaceView homeView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        homeView = (SpritedClippedSurfaceView) findViewById(R.id.homeView);

        Intent intent = getIntent();
        people = (ArrayList<LittlePerson>) intent.getSerializableExtra(ChooseFamilyMember.FAMILY);
        selectedPerson = (LittlePerson) intent.getSerializableExtra(ChooseFamilyMember.SELECTED_PERSON);
        if (selectedPerson==null && people!=null && people.size()>0) {
            selectedPerson = people.get(0);
        }

        setupHomeViewSprites();
    }

    @Override
    protected void onStart() {
        super.onStart();
        EventQueue.getInstance().subscribe(TOPIC_START_MATCH, this);
        EventQueue.getInstance().subscribe(TOPIC_START_COLORING, this);
        EventQueue.getInstance().subscribe(TOPIC_START_DRESSUP, this);
        EventQueue.getInstance().subscribe(TOPIC_START_PUZZLE, this);
        EventQueue.getInstance().subscribe(TOPIC_START_SCRATCH, this);
    }

    @Override
    protected void onStop() {
        super.onStop();
        EventQueue.getInstance().unSubscribe(TOPIC_START_MATCH, this);
        EventQueue.getInstance().unSubscribe(TOPIC_START_COLORING, this);
        EventQueue.getInstance().unSubscribe(TOPIC_START_DRESSUP, this);
        EventQueue.getInstance().unSubscribe(TOPIC_START_PUZZLE, this);
        EventQueue.getInstance().unSubscribe(TOPIC_START_SCRATCH, this);
    }

    private ClippedAnimatedBitmapSprite homeBackground;
    private void setupHomeViewSprites() {
        //-- background
        float scale = 1.0f;
        if (homeBackground==null) {
            Bitmap backBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.house_background);
            int maxWidth = (int) (backBitmap.getWidth() * scale);
            int maxHeight = (int) (backBitmap.getHeight() * scale);
            homeView.setMaxHeight(maxHeight);
            homeView.setMaxWidth(maxWidth);

            homeBackground = new ClippedAnimatedBitmapSprite(backBitmap, backBitmap.getWidth(), backBitmap.getHeight());
            homeBackground.setWidth(homeView.getWidth());
            homeBackground.setHeight(homeView.getHeight());
            homeBackground.setScale(scale);
            homeView.setBackgroundSprite(homeBackground);


            Bitmap cloudBm1 = BitmapFactory.decodeResource(getResources(), R.drawable.house_cloud1);
            MovingAnimatedBitmapSprite cloud1 = new MovingAnimatedBitmapSprite(cloudBm1, maxWidth, maxHeight);
            cloud1.setWidth((int) (cloudBm1.getWidth() * scale));
            cloud1.setHeight((int) (cloudBm1.getHeight() * scale));
            cloud1.setSlope(0);
            cloud1.setSpeed(0.5f);
            cloud1.setY(30);
            cloud1.setWrap(true);
            homeView.addSprite(cloud1);

            Bitmap cloudBm2 = BitmapFactory.decodeResource(getResources(), R.drawable.house_cloud2);
            MovingAnimatedBitmapSprite cloud2 = new MovingAnimatedBitmapSprite(cloudBm2, maxWidth, maxHeight);
            cloud2.setWidth((int) (cloudBm2.getWidth() * scale));
            cloud2.setHeight((int) (cloudBm2.getHeight() * scale));
            cloud2.setSlope(0);
            cloud2.setSpeed(0.5f);
            cloud2.setX((int) (backBitmap.getWidth() * 0.75));
            cloud2.setY(50);
            cloud2.setWrap(true);
            homeView.addSprite(cloud2);

            Bitmap treeBm = BitmapFactory.decodeResource(getResources(), R.drawable.house_tree1);
            AnimatedBitmapSprite tree = new AnimatedBitmapSprite(treeBm);
            tree.setX(100);
            tree.setY(500);
            homeView.addSprite(tree);

            Bitmap flowerBm1 = BitmapFactory.decodeResource(getResources(), R.drawable.house_flowers_b1);
            AnimatedBitmapSprite flower1 = new AnimatedBitmapSprite(flowerBm1);
            flower1.setX(180);
            flower1.setY(1200);
            homeView.addSprite(flower1);

            Bitmap flowerBm2 = BitmapFactory.decodeResource(getResources(), R.drawable.house_flowers_a1);
            TouchStateAnimatedBitmapSprite flower2 = new TouchStateAnimatedBitmapSprite(flowerBm2, this);
            flower2.setX(530);
            flower2.setY(1200);
            List<Bitmap> spinning = new ArrayList<>(5);
            spinning.add(BitmapFactory.decodeResource(getResources(), R.drawable.house_flowers_a2));
            spinning.add(BitmapFactory.decodeResource(getResources(), R.drawable.house_flowers_a3));
            spinning.add(BitmapFactory.decodeResource(getResources(), R.drawable.house_flowers_a4));
            spinning.add(BitmapFactory.decodeResource(getResources(), R.drawable.house_flowers_a5));
            spinning.add(BitmapFactory.decodeResource(getResources(), R.drawable.house_flowers_a1));
            flower2.getBitmaps().put(1, spinning);
            flower2.getAudio().put(1, R.raw.spinning);
            flower2.setStateTransition(1, TouchStateAnimatedBitmapSprite.TRANSITION_LOOP3);
            homeView.addSprite(flower2);

            Bitmap roomsBm = BitmapFactory.decodeResource(getResources(), R.drawable.house_rooms);
            AnimatedBitmapSprite rooms = new AnimatedBitmapSprite(roomsBm);
            rooms.setX(900);
            rooms.setY(170);
            homeView.addSprite(rooms);

            Bitmap familyBm = BitmapFactory.decodeResource(getResources(), R.drawable.house_familyroom_furniture);
            AnimatedBitmapSprite familyRoom = new AnimatedBitmapSprite(familyBm);
            familyRoom.setX(980);
            familyRoom.setY(1135);
            homeView.addSprite(familyRoom);

            Bitmap frameBm = BitmapFactory.decodeResource(getResources(), R.drawable.house_familyroom_frame);
            MatchGameSprite frameBtn = new MatchGameSprite(frameBm);
            frameBtn.setX(1225);
            frameBtn.setY(1035);
            frameBtn.setSelectable(true);
            homeView.addSprite(frameBtn);

            Bitmap childBedBm = BitmapFactory.decodeResource(getResources(), R.drawable.house_chilldroom_bed);
            AnimatedBitmapSprite childBed = new AnimatedBitmapSprite(childBedBm);
            childBed.setX(1600);
            childBed.setY(700);
            homeView.addSprite(childBed);

            Bitmap childDeskBm = BitmapFactory.decodeResource(getResources(), R.drawable.house_chilldroom_desk);
            AnimatedBitmapSprite childDesk = new AnimatedBitmapSprite(childDeskBm);
            childDesk.setX(2100);
            childDesk.setY(800);
            homeView.addSprite(childDesk);

            Bitmap childPaintBm = BitmapFactory.decodeResource(getResources(), R.drawable.house_chilldroom_paint);
            AnimatedBitmapSprite childPaint = new AnimatedBitmapSprite(childPaintBm);
            childPaint.setX(1900);
            childPaint.setY(700);
            homeView.addSprite(childPaint);

            Bitmap childTeddyBm = BitmapFactory.decodeResource(getResources(), R.drawable.house_chilldroom_teddy);
            AnimatedBitmapSprite childTeddy = new AnimatedBitmapSprite(childTeddyBm);
            childTeddy.setX(1800);
            childTeddy.setY(900);
            homeView.addSprite(childTeddy);

            Bitmap kitchenBm = BitmapFactory.decodeResource(getResources(), R.drawable.house_kitchen);
            AnimatedBitmapSprite kitchen = new AnimatedBitmapSprite(kitchenBm);
            kitchen.setX(1700);
            kitchen.setY(1000);
            homeView.addSprite(kitchen);
        }
    }

    @Override
    public void onEvent(String topic, Object o) {
        switch(topic) {
            case TOPIC_START_COLORING:
                startColoringGame(homeView);
                break;
            case TOPIC_START_DRESSUP:
                startHeritageDressUpGame(homeView);
                break;
            case TOPIC_START_MATCH:
                startMatchGame(homeView);
                break;
            case TOPIC_START_PUZZLE:
                startPuzzleGame(homeView);
                break;
            case TOPIC_START_SCRATCH:
                startScratchGame((homeView));
                break;
        }
    }

    public void startMatchGame(View view) {
        Intent intent = new Intent( this, MatchGameActivity.class );
        intent.putExtra(ChooseFamilyMember.FAMILY, people);
        intent.putExtra(ChooseFamilyMember.SELECTED_PERSON, selectedPerson);
        startActivity(intent);
    }

    public void startScratchGame(View view) {
        Intent intent = new Intent( this, ScratchGameActivity.class );
        intent.putExtra(ChooseFamilyMember.FAMILY, people);
        intent.putExtra(ChooseFamilyMember.SELECTED_PERSON, selectedPerson);
        startActivity(intent);
    }

    public void startColoringGame(View view) {
        Intent intent = new Intent( this, ColoringGameActivity.class );
        intent.putExtra(ChooseFamilyMember.FAMILY, people);
        intent.putExtra(ChooseFamilyMember.SELECTED_PERSON, selectedPerson);
        startActivity(intent);
    }

    public void startHeritageDressUpGame(View view) {
        Intent intent = new Intent( this, ChooseCultureActivity.class );
        intent.putExtra(ChooseFamilyMember.SELECTED_PERSON, selectedPerson);
        startActivity(intent);
    }

    public void startPuzzleGame(View view) {
        Intent intent = new Intent( this, PuzzleGameActivity.class );
        intent.putExtra(ChooseFamilyMember.FAMILY, people);
        intent.putExtra(ChooseFamilyMember.SELECTED_PERSON, selectedPerson);
        startActivity(intent);
    }
}
