package org.finlayfamily.littlefamily.activities;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.os.Bundle;

import org.finlayfamily.littlefamily.R;
import org.finlayfamily.littlefamily.data.LittlePerson;
import org.finlayfamily.littlefamily.sprites.AnimatedBitmapSprite;
import org.finlayfamily.littlefamily.sprites.ClippedRepeatedBackgroundSprite;
import org.finlayfamily.littlefamily.sprites.MovingAnimatedBitmapSprite;
import org.finlayfamily.littlefamily.sprites.TouchEventGameSprite;
import org.finlayfamily.littlefamily.sprites.TouchStateAnimatedBitmapSprite;
import org.finlayfamily.littlefamily.views.SpritedClippedSurfaceView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class HomeActivity extends LittleFamilyActivity {


    private LittlePerson selectedPerson;
    private ArrayList<LittlePerson> people;
    private ClippedRepeatedBackgroundSprite homeBackground;
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
    protected void onDestroy() {
        super.onDestroy();

        homeView.stop();
        homeView.onDestroy();
        if (homeBackground!=null) {
            homeBackground.onDestroy();
        }
        homeBackground = null;
    }

    @Override
    public void onInit(int code) {
        super.onInit(code);
        String message = String.format(getResources().getString(R.string.player_greeting), selectedPerson.getGivenName());
        //message += " "+getResources().getString(R.string.what_game);
        speak(message);
    }

    private void setupHomeViewSprites() {
        //-- background
        if (homeBackground==null) {
            Bitmap backBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.house_background2);
            int maxWidth = 1280*2;
            int maxHeight = 720*2;
            homeView.setMaxHeight(maxHeight);
            homeView.setMaxWidth(maxWidth);

            homeBackground = new ClippedRepeatedBackgroundSprite(backBitmap, maxWidth, maxHeight);
            homeBackground.setWidth(homeView.getWidth());
            homeBackground.setHeight(homeView.getHeight());
            homeBackground.setClipX(800);
            homeBackground.setClipY(200);
            homeView.setBackgroundSprite(homeBackground);


            Bitmap cloudBm1 = BitmapFactory.decodeResource(getResources(), R.drawable.house_cloud1);
            MovingAnimatedBitmapSprite cloud1 = new MovingAnimatedBitmapSprite(cloudBm1, maxWidth, maxHeight);
            cloud1.setWidth((int) (cloudBm1.getWidth()));
            cloud1.setHeight((int) (cloudBm1.getHeight()));
            cloud1.setSlope(0);
            cloud1.setSpeed(0.5f);
            cloud1.setY(30);
            cloud1.setWrap(true);
            homeView.addSprite(cloud1);

            Bitmap cloudBm2 = BitmapFactory.decodeResource(getResources(), R.drawable.house_cloud2);
            MovingAnimatedBitmapSprite cloud2 = new MovingAnimatedBitmapSprite(cloudBm2, maxWidth, maxHeight);
            cloud2.setWidth((int) (cloudBm2.getWidth()));
            cloud2.setHeight((int) (cloudBm2.getHeight()));
            cloud2.setSlope(0);
            cloud2.setSpeed(0.5f);
            cloud2.setX((int) (maxWidth * 0.75));
            cloud2.setY(50);
            cloud2.setWrap(true);
            homeView.addSprite(cloud2);

            Bitmap treeBm = BitmapFactory.decodeResource(getResources(), R.drawable.house_tree1);
            TouchEventGameSprite tree = new TouchEventGameSprite(treeBm, LittleFamilyActivity.TOPIC_START_TREE);
            tree.setX(100);
            tree.setY(500);
            tree.setSelectable(true);
            homeView.addSprite(tree);

            Bitmap flowerBm2 = BitmapFactory.decodeResource(getResources(), R.drawable.house_flowers_a1);
            TouchStateAnimatedBitmapSprite flower1 = new TouchStateAnimatedBitmapSprite(flowerBm2, this);
            flower1.setX(180);
            flower1.setY(1200);
            flower1.setIgnoreAlpha(true);
            List<Bitmap> spinning = new ArrayList<>(5);
            spinning.add(BitmapFactory.decodeResource(getResources(), R.drawable.house_flowers_a2));
            spinning.add(BitmapFactory.decodeResource(getResources(), R.drawable.house_flowers_a3));
            spinning.add(BitmapFactory.decodeResource(getResources(), R.drawable.house_flowers_a4));
            spinning.add(BitmapFactory.decodeResource(getResources(), R.drawable.house_flowers_a5));
            spinning.add(BitmapFactory.decodeResource(getResources(), R.drawable.house_flowers_a1));
            flower1.getBitmaps().put(1, spinning);
            flower1.getAudio().put(1, R.raw.spinning);
            flower1.setStateTransition(1, TouchStateAnimatedBitmapSprite.TRANSITION_LOOP3);
            Matrix flipped = new Matrix();
            flipped.postScale(-1, 1);
            flipped.postTranslate(flowerBm2.getWidth()+360, 0);
            flower1.setMatrix(flipped);
            homeView.addSprite(flower1);

            TouchStateAnimatedBitmapSprite flower2 = new TouchStateAnimatedBitmapSprite(flowerBm2, this);
            flower2.setX(530);
            flower2.setY(1200);
            flower2.setIgnoreAlpha(true);
            List<Bitmap> spinning2 = new ArrayList<>(5);
            spinning2.addAll(spinning); //-- reuse the bitmaps
            flower2.getBitmaps().put(1, spinning2);
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
            TouchEventGameSprite frameBtn = new TouchEventGameSprite(frameBm, TOPIC_START_MATCH);
            frameBtn.setX(1225);
            frameBtn.setY(1035);
            frameBtn.setSelectable(true);
            homeView.addSprite(frameBtn);

            Bitmap lampBm = BitmapFactory.decodeResource(getResources(), R.drawable.house_familyroom_lamp1);
            TouchStateAnimatedBitmapSprite lamp = new TouchStateAnimatedBitmapSprite(lampBm, this);
            lamp.setX(965);
            lamp.setY(1120);
            List<Bitmap> onOff = new ArrayList<>(1);
            onOff.add(BitmapFactory.decodeResource(getResources(), R.drawable.house_familyroom_lamp2));
            lamp.getBitmaps().put(1, onOff);
            lamp.getAudio().put(0, R.raw.pullchainslowon);
            lamp.getAudio().put(1, R.raw.pullchainslowon);
            homeView.addSprite(lamp);

            TouchStateAnimatedBitmapSprite lamp2 = new TouchStateAnimatedBitmapSprite(lampBm, this);
            lamp2.setX(1450);
            lamp2.setY(1120);
            lamp2.getBitmaps().put(1, onOff);
            lamp2.getAudio().put(0, R.raw.pullchainslowon);
            lamp2.getAudio().put(1, R.raw.pullchainslowon);
            homeView.addSprite(lamp2);

            Bitmap childBedBm = BitmapFactory.decodeResource(getResources(), R.drawable.house_chilldroom_bed);
            AnimatedBitmapSprite childBed = new AnimatedBitmapSprite(childBedBm);
            childBed.setX(1655);
            childBed.setY(735);
            homeView.addSprite(childBed);

            Bitmap childDeskBm = BitmapFactory.decodeResource(getResources(), R.drawable.house_chilldroom_desk);
            TouchEventGameSprite childDesk = new TouchEventGameSprite(childDeskBm, TOPIC_START_SCRATCH);
            childDesk.setX(2130);
            childDesk.setY(800);
            childDesk.setSelectable(true);
            homeView.addSprite(childDesk);

            Bitmap childPaintBm = BitmapFactory.decodeResource(getResources(), R.drawable.house_chilldroom_paint);
            TouchEventGameSprite childPaint = new TouchEventGameSprite(childPaintBm, TOPIC_START_COLORING);
            childPaint.setX(2000);
            childPaint.setY(810);
            childPaint.setSelectable(true);
            homeView.addSprite(childPaint);

            Bitmap childTeddyBm = BitmapFactory.decodeResource(getResources(), R.drawable.house_chilldroom_teddy);
            TouchStateAnimatedBitmapSprite childTeddy = new TouchStateAnimatedBitmapSprite(childTeddyBm, this);
            childTeddy.setX(1850);
            childTeddy.setY(895);
            List<Bitmap> falling = new ArrayList<>(5);
            falling.add(BitmapFactory.decodeResource(getResources(), R.drawable.house_chilldroom_teddy2));
            falling.add(BitmapFactory.decodeResource(getResources(), R.drawable.house_chilldroom_teddy3));
            falling.add(BitmapFactory.decodeResource(getResources(), R.drawable.house_chilldroom_teddy4));
            falling.add(BitmapFactory.decodeResource(getResources(), R.drawable.house_chilldroom_teddy5));
            falling.add(BitmapFactory.decodeResource(getResources(), R.drawable.house_chilldroom_teddy6));
            childTeddy.getBitmaps().put(1, falling);
            childTeddy.getAudio().put(1, R.raw.slide_whistle_down01);
            childTeddy.setStateTransition(1, TouchStateAnimatedBitmapSprite.TRANSITION_LOOP1);
            List<Bitmap> fallen = new ArrayList<>(1);
            fallen.add(BitmapFactory.decodeResource(getResources(), R.drawable.house_chilldroom_teddy6));
            childTeddy.getBitmaps().put(2, fallen);
            List<Bitmap> rising = new ArrayList<>(falling);
            Collections.reverse(rising);
            //rising.add(BitmapFactory.decodeResource(getResources(), R.drawable.house_chilldroom_teddy6));
            //rising.add(BitmapFactory.decodeResource(getResources(), R.drawable.house_chilldroom_teddy5));
            //rising.add(BitmapFactory.decodeResource(getResources(), R.drawable.house_chilldroom_teddy4));
            //rising.add(BitmapFactory.decodeResource(getResources(), R.drawable.house_chilldroom_teddy3));
            //rising.add(BitmapFactory.decodeResource(getResources(), R.drawable.house_chilldroom_teddy2));
            childTeddy.getBitmaps().put(3, rising);
            childTeddy.getAudio().put(3, R.raw.slide_whistle_up04);
            childTeddy.setStateTransition(3, TouchStateAnimatedBitmapSprite.TRANSITION_LOOP1);
            homeView.addSprite(childTeddy);

            Bitmap kitchenBm = BitmapFactory.decodeResource(getResources(), R.drawable.house_kitchen);
            AnimatedBitmapSprite kitchen = new AnimatedBitmapSprite(kitchenBm);
            kitchen.setX(1680);
            kitchen.setY(1025);
            homeView.addSprite(kitchen);

            Bitmap toasterBm = BitmapFactory.decodeResource(getResources(), R.drawable.house_toaster1);
            TouchStateAnimatedBitmapSprite toaster = new TouchStateAnimatedBitmapSprite(toasterBm, this);
            toaster.setX(2170);
            toaster.setY(1130);
            List<Bitmap> toastDown = new ArrayList<>(2);
            toastDown.add(BitmapFactory.decodeResource(getResources(), R.drawable.house_toaster2));
            toastDown.add(BitmapFactory.decodeResource(getResources(), R.drawable.house_toaster3));
            toaster.getBitmaps().put(1, toastDown);
            toaster.setStateTransition(1, TouchStateAnimatedBitmapSprite.TRANSITION_LOOP1);
            List<Bitmap> toastIn = new ArrayList<>(1);
            toastIn.add(BitmapFactory.decodeResource(getResources(), R.drawable.house_toaster4));
            toastIn.add(BitmapFactory.decodeResource(getResources(), R.drawable.house_toaster4));
            toaster.getBitmaps().put(2, toastIn);
            toaster.setStateTransition(2, 10);
            List<Bitmap> toastUp = new ArrayList<>(17);
            toastUp.add(BitmapFactory.decodeResource(getResources(), R.drawable.house_toaster5));
            toastUp.add(BitmapFactory.decodeResource(getResources(), R.drawable.house_toaster6));
            toastUp.add(BitmapFactory.decodeResource(getResources(), R.drawable.house_toaster7));
            toastUp.add(BitmapFactory.decodeResource(getResources(), R.drawable.house_toaster8));
            toastUp.add(BitmapFactory.decodeResource(getResources(), R.drawable.house_toaster9));
            toastUp.add(BitmapFactory.decodeResource(getResources(), R.drawable.house_toaster10));
            toastUp.add(BitmapFactory.decodeResource(getResources(), R.drawable.house_toaster11));
            toastUp.add(BitmapFactory.decodeResource(getResources(), R.drawable.house_toaster12));
            toastUp.add(BitmapFactory.decodeResource(getResources(), R.drawable.house_toaster13));
            toastUp.add(BitmapFactory.decodeResource(getResources(), R.drawable.house_toaster14));
            toastUp.add(BitmapFactory.decodeResource(getResources(), R.drawable.house_toaster15));
            toastUp.add(BitmapFactory.decodeResource(getResources(), R.drawable.house_toaster16));
            toastUp.add(BitmapFactory.decodeResource(getResources(), R.drawable.house_toaster17));
            toastUp.add(BitmapFactory.decodeResource(getResources(), R.drawable.house_toaster18));
            toastUp.add(BitmapFactory.decodeResource(getResources(), R.drawable.house_toaster19));
            toastUp.add(BitmapFactory.decodeResource(getResources(), R.drawable.house_toaster20));
            toastUp.add(BitmapFactory.decodeResource(getResources(), R.drawable.house_toaster21));
            toaster.getBitmaps().put(3, toastUp);
            toaster.setStateTransition(3, TouchStateAnimatedBitmapSprite.TRANSITION_LOOP1);
            toaster.getAudio().put(1, R.raw.toaster1);
            toaster.getAudio().put(3, R.raw.toaster2);

            homeView.addSprite(toaster);

            Bitmap bubbles1bm = BitmapFactory.decodeResource(getResources(), R.drawable.bubbles1);
            TouchEventGameSprite bubbles = new TouchEventGameSprite(bubbles1bm, TOPIC_START_BUBBLES);
            bubbles.getBitmaps().get(0).add(BitmapFactory.decodeResource(getResources(), R.drawable.bubbles2));
            bubbles.getBitmaps().get(0).add(BitmapFactory.decodeResource(getResources(), R.drawable.bubbles3));
            bubbles.getBitmaps().get(0).add(BitmapFactory.decodeResource(getResources(), R.drawable.bubbles4));
            bubbles.getBitmaps().get(0).add(BitmapFactory.decodeResource(getResources(), R.drawable.bubbles5));
            bubbles.getBitmaps().get(0).add(BitmapFactory.decodeResource(getResources(), R.drawable.bubbles6));
            bubbles.getBitmaps().get(0).add(BitmapFactory.decodeResource(getResources(), R.drawable.bubbles7));
            bubbles.getBitmaps().get(0).add(BitmapFactory.decodeResource(getResources(), R.drawable.bubbles8));
            bubbles.setStepsPerFrame(5);
            bubbles.setX(1820);
            bubbles.setY(1135);
            bubbles.setSelectable(true);
            bubbles.setIgnoreAlpha(true);
            homeView.addSprite(bubbles);

            Bitmap wardrobeBm = BitmapFactory.decodeResource(getResources(), R.drawable.house_adult_wardrobe);
            TouchEventGameSprite wardrobe = new TouchEventGameSprite(wardrobeBm, TOPIC_START_HERITAGE_CALC);
            wardrobe.setX(1500);
            wardrobe.setY(700);
            wardrobe.setSelectable(true);
            homeView.addSprite(wardrobe);

            Bitmap adultBedBm = BitmapFactory.decodeResource(getResources(), R.drawable.house_adult_bed);
            AnimatedBitmapSprite adultBed = new AnimatedBitmapSprite(adultBedBm);
            adultBed.setX(975);
            adultBed.setY(693);
            homeView.addSprite(adultBed);

            Bitmap adultVanityBm = BitmapFactory.decodeResource(getResources(), R.drawable.house_adult_vanity);
            AnimatedBitmapSprite adultVanity = new AnimatedBitmapSprite(adultVanityBm);
            adultVanity.setX(1350);
            adultVanity.setY(800);
            homeView.addSprite(adultVanity);

            Bitmap lightABm = BitmapFactory.decodeResource(getResources(), R.drawable.house_light_a1);
            TouchStateAnimatedBitmapSprite lightA = new TouchStateAnimatedBitmapSprite(lightABm, this);
            lightA.setX(1340);
            lightA.setY(652);
            List<Bitmap> lightAonOff = new ArrayList<>(1);
            lightAonOff.add(BitmapFactory.decodeResource(getResources(), R.drawable.house_light_a2));
            lightA.getBitmaps().put(1, lightAonOff);
            lightA.getAudio().put(0, R.raw.pullchainslowon);
            lightA.getAudio().put(1, R.raw.pullchainslowon);
            homeView.addSprite(lightA);

            Bitmap lightBBm = BitmapFactory.decodeResource(getResources(), R.drawable.house_light_b1);
            TouchStateAnimatedBitmapSprite lightB = new TouchStateAnimatedBitmapSprite(lightBBm, this);
            lightB.setX(1045);
            lightB.setY(650);
            List<Bitmap> lightBonOff = new ArrayList<>(1);
            lightBonOff.add(BitmapFactory.decodeResource(getResources(), R.drawable.house_light_b2));
            lightB.getBitmaps().put(1, lightBonOff);
            lightB.getAudio().put(0, R.raw.pullchainslowon);
            lightB.getAudio().put(1, R.raw.pullchainslowon);
            homeView.addSprite(lightB);


            Bitmap blocksBm = BitmapFactory.decodeResource(getResources(), R.drawable.house_toys_blocks);
            TouchEventGameSprite blocks = new TouchEventGameSprite(blocksBm, TOPIC_START_PUZZLE);
            blocks.setX(2040);
            blocks.setY(550);
            blocks.setSelectable(true);
            homeView.addSprite(blocks);

            Bitmap horseBm = BitmapFactory.decodeResource(getResources(), R.drawable.house_toys_horse);
            AnimatedBitmapSprite horse = new AnimatedBitmapSprite(horseBm);
            horse.setX(1865);
            horse.setY(490);
            homeView.addSprite(horse);

            Bitmap batBm = BitmapFactory.decodeResource(getResources(), R.drawable.house_toys_bat);
            AnimatedBitmapSprite bat = new AnimatedBitmapSprite(batBm);
            bat.setX(1640);
            bat.setY(560);
            homeView.addSprite(bat);

            Bitmap pianoBm = BitmapFactory.decodeResource(getResources(), R.drawable.house_music_piano);
            AnimatedBitmapSprite piano = new AnimatedBitmapSprite(pianoBm);
            piano.setX(1250);
            piano.setY(450);
            homeView.addSprite(piano);

            Bitmap guitarBm = BitmapFactory.decodeResource(getResources(), R.drawable.house_music_guitar);
            AnimatedBitmapSprite guitar = new AnimatedBitmapSprite(guitarBm);
            guitar.setX(1380);
            guitar.setY(465);
            homeView.addSprite(guitar);

            Bitmap trumpetBm = BitmapFactory.decodeResource(getResources(), R.drawable.house_music_trumpet);
            AnimatedBitmapSprite trumpet = new AnimatedBitmapSprite(trumpetBm);
            trumpet.setX(1300);
            trumpet.setY(410);
            homeView.addSprite(trumpet);

            Bitmap drumsBm = BitmapFactory.decodeResource(getResources(), R.drawable.house_music_drums);
            AnimatedBitmapSprite drums = new AnimatedBitmapSprite(drumsBm);
            drums.setX(1175);
            drums.setY(570);
            homeView.addSprite(drums);

            homeView.setClipX(800);
            homeView.setClipY(200);
        }
    }


}
