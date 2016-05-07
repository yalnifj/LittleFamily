package com.yellowforktech.littlefamilytree.activities;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.speech.tts.UtteranceProgressListener;
import android.util.DisplayMetrics;
import android.util.Log;

import com.yellowforktech.littlefamilytree.R;
import com.yellowforktech.littlefamilytree.data.LittlePerson;
import com.yellowforktech.littlefamilytree.events.EventQueue;
import com.yellowforktech.littlefamilytree.sprites.AnimatedBitmapSprite;
import com.yellowforktech.littlefamilytree.sprites.BirdSprite;
import com.yellowforktech.littlefamilytree.sprites.CatSprite;
import com.yellowforktech.littlefamilytree.sprites.ClippedRepeatedBackgroundSprite;
import com.yellowforktech.littlefamilytree.sprites.MovingAnimatedBitmapSprite;
import com.yellowforktech.littlefamilytree.sprites.MovingTouchStateAnimatedBitmapSprite;
import com.yellowforktech.littlefamilytree.sprites.PersonLeavesButton;
import com.yellowforktech.littlefamilytree.sprites.TouchEventGameSprite;
import com.yellowforktech.littlefamilytree.sprites.TouchStateAnimatedBitmapSprite;
import com.yellowforktech.littlefamilytree.util.ImageHelper;
import com.yellowforktech.littlefamilytree.views.HomeView;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class HomeActivity extends LittleFamilyActivity {
    public static final String TOPIC_ANIMATE_CAT = "animateCat";

    private LittlePerson selectedPerson;
    private ArrayList<LittlePerson> people;
    private ClippedRepeatedBackgroundSprite homeBackground;
    private HomeView homeView;
    private CatSprite catSprite;

    @SuppressWarnings("unchecked")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        homeView = (HomeView) findViewById(R.id.homeView);

        Intent intent = getIntent();
        people = (ArrayList<LittlePerson>) intent.getSerializableExtra(ChooseFamilyMember.FAMILY);
        selectedPerson = (LittlePerson) intent.getSerializableExtra(ChooseFamilyMember.SELECTED_PERSON);
        if (selectedPerson==null && people!=null && people.size()>0) {
            selectedPerson = people.get(0);
        }
    }
	
	public void onStart() {
		super.onStart();
        EventQueue.getInstance().subscribe(TOPIC_ANIMATE_CAT, this);

		setupHomeViewSprites();
        homeView.resume();
	}
	
	public void onStop() {
		super.onStop();

        EventQueue.getInstance().unSubscribe(TOPIC_ANIMATE_CAT, this);

		homeView.stop();
        homeView.onDestroy();
        if (homeBackground!=null) {
            homeBackground.onDestroy();
        }
        homeBackground = null;
		System.gc();
	}

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onInit(int code) {
        super.onInit(code);
		if (selectedPerson != null) {
      	  String message = getResources().getString(R.string.player_greeting);
       	  speak(message, new UtteranceProgressListener() {
              @Override
              public void onStart(String utteranceId) {
                  Log.v("HomeActivity", "utterance started");
              }
              @Override
              public void onDone(String utteranceId) {
                  Log.v("HomeActivity", "utterance done");
                  sayGivenNameForPerson(selectedPerson);
              }
              @Override
              public void onError(String utteranceId) {
                  Log.v("HomeActivity", "utterance error");
              }
          });
		}
    }

    private void setupHomeViewSprites() {
        //-- background
        if (homeBackground==null) {
            //px = dp * (dpi / 160)
            DisplayMetrics dm = new DisplayMetrics();
            getWindowManager().getDefaultDisplay().getMetrics(dm);
            int maxWidth = (int) (1280*dm.density);
            int maxHeight = (int) (800*dm.density);
            homeView.setMaxHeight(maxHeight);
            homeView.setMaxWidth(maxWidth);
            homeView.setScaleSet(false);
            homeView.setScale(1.0f);

            Bitmap backBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.house_background2);
            homeBackground = new ClippedRepeatedBackgroundSprite(backBitmap, maxWidth, maxHeight);
            homeBackground.setWidth(homeView.getWidth());
            homeBackground.setHeight(homeView.getHeight());
            homeView.setBackgroundSprite(homeBackground);

            Bitmap starBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.star1);
            homeView.setStarBitmap(starBitmap);
			
			if (selectedPerson!=null) {
				Bitmap photo = null;
				if (selectedPerson.getPhotoPath() != null) {
					photo = ImageHelper.loadBitmapFromFile(selectedPerson.getPhotoPath(), ImageHelper.getOrientation(selectedPerson.getPhotoPath()), (int) (40*dm.density), (int) (30*dm.density), false);
				}
                if (photo==null){
					photo = ImageHelper.loadBitmapFromResource(this, selectedPerson.getDefaultPhotoResource(), 0, (int)(30*dm.density), (int)(30*dm.density));
				}
				TouchEventGameSprite profileSprite = new TouchEventGameSprite(photo, LittleFamilyActivity.TOPIC_START_PROFILE, dm);
				profileSprite.setSelectable(true);
				homeView.setProfileSprite(profileSprite);
			}

            Bitmap cloudBm1 = BitmapFactory.decodeResource(getResources(), R.drawable.house_cloud1);
            MovingTouchStateAnimatedBitmapSprite cloud1 = new MovingTouchStateAnimatedBitmapSprite(cloudBm1, this);
            cloud1.setWidth(cloudBm1.getWidth());
            cloud1.setHeight(cloudBm1.getHeight());
            cloud1.setMaxWidth(maxWidth);
            cloud1.setMaxHeight(maxHeight);
            cloud1.setSlope(0);
            cloud1.setSpeed(0.5f);
            cloud1.setY(15*dm.density);
            cloud1.setWrap(true);
            cloud1.setResources(getResources());
            List<Integer> darker = new ArrayList<>(1);
            darker.add(R.drawable.house_cloud1a);
            cloud1.getBitmapIds().put(1, darker);
            darker = new ArrayList<>(1);
            darker.add(R.drawable.house_cloud1b);
            cloud1.getBitmapIds().put(2, darker);
            darker = new ArrayList<>(1);
            darker.add(R.drawable.house_cloud1c);
            cloud1.getBitmapIds().put(3, darker);
            darker = new ArrayList<>(2);
            darker.add(R.drawable.house_cloud1d);
            darker.add(R.drawable.house_cloud1e);
            cloud1.getBitmapIds().put(4, darker);
            cloud1.setStateTransition(4, 20);
			cloud1.getAudio().put(4, R.raw.rain);
            homeView.addSprite(cloud1);

            Bitmap cloudBm2 = BitmapFactory.decodeResource(getResources(), R.drawable.house_cloud2);
            MovingAnimatedBitmapSprite cloud2 = new MovingAnimatedBitmapSprite(cloudBm2, maxWidth, maxHeight);
            cloud2.setWidth(cloudBm2.getWidth());
            cloud2.setHeight(cloudBm2.getHeight());
            cloud2.setSlope(0);
            cloud2.setSpeed(0.5f);
            cloud2.setX((int) (maxWidth * 0.75));
            cloud2.setY(25*dm.density);
            cloud2.setWrap(true);
            homeView.addSprite(cloud2);


            Bitmap treeBm = BitmapFactory.decodeResource(getResources(), R.drawable.house_tree1);
            AnimatedBitmapSprite tree = new AnimatedBitmapSprite(treeBm);
            tree.setX(50 * dm.density);
            tree.setY(250 * dm.density);
            homeView.addSprite(tree);

            List<Bitmap> leaves = new ArrayList<>(6);
            leaves.add(BitmapFactory.decodeResource(getResources(), R.drawable.leaves_overlay6));
            leaves.add(BitmapFactory.decodeResource(getResources(), R.drawable.leaves_overlay5));
            leaves.add(BitmapFactory.decodeResource(getResources(), R.drawable.leaves_overlay4));
            leaves.add(BitmapFactory.decodeResource(getResources(), R.drawable.leaves_overlay3));
            leaves.add(BitmapFactory.decodeResource(getResources(), R.drawable.leaves_overlay2));
            leaves.add(BitmapFactory.decodeResource(getResources(), R.drawable.leaves_overlay1));
            PersonLeavesButton personLeaf = new PersonLeavesButton(TOPIC_START_TREE, people, leaves, this);
            personLeaf.setX(220 * dm.density);
            personLeaf.setY(330 * dm.density);
            personLeaf.setSelectable(true);
            homeView.addSprite(personLeaf);
            homeView.addActivitySprite(personLeaf);

            Bitmap birdBm = BitmapFactory.decodeResource(getResources(), R.drawable.house_tree_bird);
            BirdSprite birdSprite = new BirdSprite(birdBm, this, TOPIC_START_FLYING);
            List<Bitmap> state1 = new ArrayList<>(5);
            state1.add(BitmapFactory.decodeResource(getResources(), R.drawable.house_tree_bird));
            state1.add(BitmapFactory.decodeResource(getResources(), R.drawable.house_tree_bird1));
            state1.add(BitmapFactory.decodeResource(getResources(), R.drawable.house_tree_bird2));
            state1.add(BitmapFactory.decodeResource(getResources(), R.drawable.house_tree_bird1));
            state1.add(BitmapFactory.decodeResource(getResources(), R.drawable.house_tree_bird));
            birdSprite.getBitmaps().put(1, state1);
            birdSprite.setX(100 * dm.density);
            birdSprite.setY(330 * dm.density);
            birdSprite.setResources(this.getResources());
            birdSprite.setSelectable(true);
            birdSprite.setIgnoreAlpha(true);
            homeView.addSprite(birdSprite);
            //homeView.addActivitySprite(birdSprite);

            Bitmap flowerBm2 = BitmapFactory.decodeResource(getResources(), R.drawable.house_flowers_a1);
            TouchStateAnimatedBitmapSprite flower1 = new TouchStateAnimatedBitmapSprite(flowerBm2, this);
            flower1.setX(90*dm.density);
            flower1.setY(600 * dm.density);
            flower1.setIgnoreAlpha(true);
            flower1.setResources(getResources());
            List<Integer> spinning = new ArrayList<>(5);
            spinning.add(R.drawable.house_flowers_a2);
            spinning.add(R.drawable.house_flowers_a3);
            spinning.add(R.drawable.house_flowers_a4);
            spinning.add(R.drawable.house_flowers_a5);
            spinning.add(R.drawable.house_flowers_a1);
            flower1.getBitmapIds().put(1, spinning);
            flower1.getAudio().put(1, R.raw.spinning);
            flower1.setStateTransition(1, TouchStateAnimatedBitmapSprite.TRANSITION_LOOP3);
            flower1.setFlipHoriz(true);
            flower1.setIgnoreAlpha(true);
            homeView.addSprite(flower1);

            TouchStateAnimatedBitmapSprite flower2 = new TouchStateAnimatedBitmapSprite(flowerBm2, this);
            flower2.setX(265*dm.density);
            flower2.setY(600*dm.density);
            flower2.setIgnoreAlpha(true);
            flower2.setResources(getResources());
            List<Integer> spinning2 = new ArrayList<>(5);
            spinning2.addAll(spinning); //-- reuse the bitmaps
            flower2.getBitmapIds().put(1, spinning2);
            flower2.getAudio().put(1, R.raw.spinning);
            flower2.setStateTransition(1, TouchStateAnimatedBitmapSprite.TRANSITION_LOOP3);
            flower2.setIgnoreAlpha(true);
            homeView.addSprite(flower2);

            /*
            Bitmap roomsBm = BitmapFactory.decodeResource(getResources(), R.drawable.house_rooms);
            AnimatedBitmapSprite rooms = new AnimatedBitmapSprite(roomsBm);
            rooms.setX(450 * dm.density);
            rooms.setY(85 * dm.density);
            homeView.addSprite(rooms);
            */

            for(int c=0; c<5; c++) {
                for(int r=0; r<5; r++) {
                    try {
                        String tileFile = "tiles/house_rooms_" + c + "_" + r + ".png";
                        InputStream is = getAssets().open(tileFile);
                        Bitmap tileBm = BitmapFactory.decodeStream(is);
                        if (tileBm != null) {
                            AnimatedBitmapSprite tile = new AnimatedBitmapSprite(tileBm);
                            float x = (450 + c * tileBm.getWidth())* dm.density;
                            float y = (85 + r * tileBm.getHeight()) * dm.density;
                            tile.setX(x);
                            tile.setY(y);
                            tile.setWidth((int) (tileBm.getWidth() * dm.density));
                            tile.setHeight((int) (tileBm.getHeight()* dm.density));
                            homeView.addSprite(tile);
                        }
                    } catch (FileNotFoundException e) {
                    } catch (IOException e) {
                        Log.e(this.getClass().getName(), "Error opening tile "+c+" "+r, e);
                    }
                }
            }

            Bitmap couchBm = BitmapFactory.decodeResource(getResources(), R.drawable.house_familyroom_couch);
            TouchEventGameSprite couch = new TouchEventGameSprite(couchBm, TOPIC_ANIMATE_CAT, dm);
            couch.setX(555 * dm.density);
            couch.setY(575 * dm.density);
            homeView.addSprite(couch);

            catSprite = new CatSprite(this);
            catSprite.setX(couch.getX() + (couch.getWidth()/2));
            catSprite.setY(628*dm.density);
            homeView.addSprite(catSprite);

            Bitmap frameBm = BitmapFactory.decodeResource(getResources(), R.drawable.house_familyroom_frame);
            TouchStateAnimatedBitmapSprite frameBtn = new TouchStateAnimatedBitmapSprite(frameBm, this);
            frameBtn.setX(612*dm.density);
            frameBtn.setY(517*dm.density);
            frameBtn.setResources(getResources());
            List<Integer> jumping = new ArrayList<>(25);
            jumping.add(R.drawable.house_familyroom_frame1);
            jumping.add(R.drawable.house_familyroom_frame2);
            jumping.add(R.drawable.house_familyroom_frame3);
            jumping.add(R.drawable.house_familyroom_frame4);
            jumping.add(R.drawable.house_familyroom_frame5);
            jumping.add(R.drawable.house_familyroom_frame6);
            jumping.add(R.drawable.house_familyroom_frame7);
            jumping.add(R.drawable.house_familyroom_frame8);
            jumping.add(R.drawable.house_familyroom_frame9);
            jumping.add(R.drawable.house_familyroom_frame10);
            jumping.add(R.drawable.house_familyroom_frame11);
            jumping.add(R.drawable.house_familyroom_frame12);
            jumping.add(R.drawable.house_familyroom_frame13);
            jumping.add(R.drawable.house_familyroom_frame14);
            jumping.add(R.drawable.house_familyroom_frame15);
            jumping.add(R.drawable.house_familyroom_frame16);
            jumping.add(R.drawable.house_familyroom_frame17);
            jumping.add(R.drawable.house_familyroom_frame18);
            jumping.add(R.drawable.house_familyroom_frame19);
            jumping.add(R.drawable.house_familyroom_frame20);
            jumping.add(R.drawable.house_familyroom_frame20);
            jumping.add(R.drawable.house_familyroom_frame21);
            jumping.add(R.drawable.house_familyroom_frame22);
            jumping.add(R.drawable.house_familyroom_frame23);
            jumping.add(R.drawable.house_familyroom_frame24);
            jumping.add(R.drawable.house_familyroom_frame25);
            frameBtn.getBitmapIds().put(1, jumping);
            frameBtn.setStateTransition(1, TouchStateAnimatedBitmapSprite.TRANSITION_LOOP1);
            frameBtn.setStateTransitionEvent(0, TOPIC_START_MATCH);
            frameBtn.setSelectable(true);
            homeView.addSprite(frameBtn);
            homeView.addActivitySprite(frameBtn);

            Bitmap tableBm = BitmapFactory.decodeResource(getResources(), R.drawable.house_familyroom_table);
            AnimatedBitmapSprite familyTable1 = new AnimatedBitmapSprite(tableBm);
            familyTable1.setX(491 * dm.density);
            familyTable1.setY(608 * dm.density);
            homeView.addSprite(familyTable1);

            Bitmap lampBm = BitmapFactory.decodeResource(getResources(), R.drawable.house_familyroom_lamp1);
            TouchStateAnimatedBitmapSprite lamp = new TouchStateAnimatedBitmapSprite(lampBm, this);
            lamp.setX(482*dm.density);
            lamp.setY(560*dm.density);
            lamp.setResources(getResources());
            List<Integer> onOff = new ArrayList<>(1);
            onOff.add(R.drawable.house_familyroom_lamp2);
            lamp.getBitmapIds().put(1, onOff);
            lamp.getAudio().put(0, R.raw.pullchainslowon);
            lamp.getAudio().put(1, R.raw.pullchainslowon);
            homeView.addSprite(lamp);

            AnimatedBitmapSprite familyTable2 = new AnimatedBitmapSprite(tableBm);
            familyTable2.setX(735 * dm.density);
            familyTable2.setY(608 * dm.density);
            homeView.addSprite(familyTable2);

            TouchStateAnimatedBitmapSprite lamp2 = new TouchStateAnimatedBitmapSprite(lampBm, this);
            lamp2.setX(725*dm.density);
            lamp2.setY(560*dm.density);
            lamp2.setResources(getResources());
            lamp2.getBitmapIds().put(1, onOff);
            lamp2.getAudio().put(0, R.raw.pullchainslowon);
            lamp2.getAudio().put(1, R.raw.pullchainslowon);
            homeView.addSprite(lamp2);

            Bitmap childBedBm = BitmapFactory.decodeResource(getResources(), R.drawable.house_chilldroom_bed);
            AnimatedBitmapSprite childBed = new AnimatedBitmapSprite(childBedBm);
            childBed.setX(827*dm.density);
            childBed.setY(367*dm.density);
            homeView.addSprite(childBed);

            Bitmap childDeskBm = BitmapFactory.decodeResource(getResources(), R.drawable.house_chilldroom_desk);
            TouchStateAnimatedBitmapSprite childDesk = new TouchStateAnimatedBitmapSprite(childDeskBm, this);
            childDesk.setX(1065*dm.density);
            childDesk.setY(390*dm.density);
            childDesk.setResources(getResources());
            List<Integer> erasing = new ArrayList<>(9);
            erasing.add(R.drawable.house_chilldroom_desk1);
            erasing.add(R.drawable.house_chilldroom_desk2);
            erasing.add(R.drawable.house_chilldroom_desk3);
            erasing.add(R.drawable.house_chilldroom_desk4);
            erasing.add(R.drawable.house_chilldroom_desk5);
            erasing.add(R.drawable.house_chilldroom_desk6);
            erasing.add(R.drawable.house_chilldroom_desk7);
            erasing.add(R.drawable.house_chilldroom_desk8);
            erasing.add(R.drawable.house_chilldroom_desk9);
            childDesk.getBitmapIds().put(1, erasing);
            childDesk.setStateTransition(1, TouchStateAnimatedBitmapSprite.TRANSITION_LOOP1);
            childDesk.getAudio().put(1, R.raw.switch_on);
            List<Integer> erasing2 = new ArrayList<>(9);
            erasing2.add(R.drawable.house_chilldroom_desk10);
            erasing2.add(R.drawable.house_chilldroom_desk11);
            erasing2.add(R.drawable.house_chilldroom_desk12);
            erasing2.add(R.drawable.house_chilldroom_desk13);
            erasing2.add(R.drawable.house_chilldroom_desk14);
            erasing2.add(R.drawable.house_chilldroom_desk15);
            erasing2.add(R.drawable.house_chilldroom_desk16);
            erasing2.add(R.drawable.house_chilldroom_desk17);
            erasing2.add(R.drawable.house_chilldroom_desk18);
            erasing2.add(R.drawable.house_chilldroom_desk19);
            childDesk.getBitmapIds().put(2, erasing2);
            childDesk.setStateTransition(2, TouchStateAnimatedBitmapSprite.TRANSITION_LOOP1);
            childDesk.getAudio().put(2, R.raw.erasing);
            List<Integer> erasing3 = new ArrayList<>(2);
            erasing3.add(erasing2.get(erasing2.size() - 1));
            erasing3.add(erasing2.get(erasing2.size() - 1));
            childDesk.getBitmapIds().put(3, erasing3);
            childDesk.setStateTransition(3, 5);
            childDesk.setStateTransitionEvent(3, TOPIC_START_SCRATCH);
            childDesk.setSelectable(true);
            homeView.addSprite(childDesk);
            homeView.addActivitySprite(childDesk);

            Bitmap childPaintBm = BitmapFactory.decodeResource(getResources(), R.drawable.house_chilldroom_paint);
            TouchStateAnimatedBitmapSprite childPaint = new TouchStateAnimatedBitmapSprite(childPaintBm, this);
            childPaint.setX(1000*dm.density);
            childPaint.setY(401*dm.density);
            childPaint.setSelectable(true);
            childPaint.setResources(getResources());
            List<Integer> painting = new ArrayList<>(15);
            painting.add(R.drawable.house_chilldroom_paint1);
            painting.add(R.drawable.house_chilldroom_paint2);
            painting.add(R.drawable.house_chilldroom_paint3);
            painting.add(R.drawable.house_chilldroom_paint4);
            painting.add(R.drawable.house_chilldroom_paint5);
            painting.add(R.drawable.house_chilldroom_paint6);
            painting.add(R.drawable.house_chilldroom_paint7);
            painting.add(R.drawable.house_chilldroom_paint8);
            painting.add(R.drawable.house_chilldroom_paint9);
            painting.add(R.drawable.house_chilldroom_paint10);
            painting.add(R.drawable.house_chilldroom_paint11);
            painting.add(R.drawable.house_chilldroom_paint12);
            painting.add(R.drawable.house_chilldroom_paint13);
            painting.add(R.drawable.house_chilldroom_paint14);
            painting.add(R.drawable.house_chilldroom_paint15);
            childPaint.getBitmapIds().put(1, painting);
            childPaint.setStateTransition(1, TouchStateAnimatedBitmapSprite.TRANSITION_LOOP1);
            List<Integer> painting2 = new ArrayList<>(2);
            painting2.add(painting.get(painting.size()-1));
            painting2.add(painting.get(painting.size()-1));
            childPaint.getBitmapIds().put(2, painting2);
            childPaint.setStateTransition(2, 5);
            childPaint.setStateTransitionEvent(2, TOPIC_START_COLORING);
            homeView.addSprite(childPaint);
            homeView.addActivitySprite(childPaint);

            Bitmap childTeddyBm = BitmapFactory.decodeResource(getResources(), R.drawable.house_chilldroom_teddy);
            TouchStateAnimatedBitmapSprite childTeddy = new TouchStateAnimatedBitmapSprite(childTeddyBm, this);
            childTeddy.setX(925*dm.density);
            childTeddy.setY(447*dm.density);
            childTeddy.setResources(getResources());
            List<Integer> falling = new ArrayList<>(5);
            falling.add(R.drawable.house_chilldroom_teddy2);
            falling.add(R.drawable.house_chilldroom_teddy3);
            falling.add(R.drawable.house_chilldroom_teddy4);
            falling.add(R.drawable.house_chilldroom_teddy5);
            falling.add(R.drawable.house_chilldroom_teddy6);
            childTeddy.getBitmapIds().put(1, falling);
            childTeddy.getAudio().put(1, R.raw.slide_whistle_down01);
            childTeddy.setStateTransition(1, TouchStateAnimatedBitmapSprite.TRANSITION_LOOP1);
            List<Integer> fallen = new ArrayList<>(1);
            fallen.add(R.drawable.house_chilldroom_teddy6);
            childTeddy.getBitmapIds().put(2, fallen);
            List<Integer> rising = new ArrayList<>(falling);
            Collections.reverse(rising);
            childTeddy.getBitmapIds().put(3, rising);
            childTeddy.getAudio().put(3, R.raw.slide_whistle_up04);
            childTeddy.setStateTransition(3, TouchStateAnimatedBitmapSprite.TRANSITION_LOOP1);
            homeView.addSprite(childTeddy);

            Bitmap kitchenABm = BitmapFactory.decodeResource(getResources(), R.drawable.house_kitchen_a);
            AnimatedBitmapSprite kitchenA = new AnimatedBitmapSprite(kitchenABm);
            kitchenA.setX(840 * dm.density);
            kitchenA.setY(512 * dm.density);
            homeView.addSprite(kitchenA);

            Bitmap kitchenBBm = BitmapFactory.decodeResource(getResources(), R.drawable.house_kitchen_b);
            AnimatedBitmapSprite kitchenB = new AnimatedBitmapSprite(kitchenBBm);
            kitchenB.setX(kitchenA.getX() + kitchenA.getWidth());
            kitchenB.setY(512*dm.density);
            homeView.addSprite(kitchenB);

            Bitmap kitchenCBm = BitmapFactory.decodeResource(getResources(), R.drawable.house_kitchen_c);
            AnimatedBitmapSprite kitchenC = new AnimatedBitmapSprite(kitchenCBm);
            kitchenC.setX(kitchenB.getX() + kitchenB.getWidth());
            kitchenC.setY(512*dm.density);
            homeView.addSprite(kitchenC);

            Bitmap kitchenDBm = BitmapFactory.decodeResource(getResources(), R.drawable.house_kitchen_d);
            AnimatedBitmapSprite kitchenD = new AnimatedBitmapSprite(kitchenDBm);
            kitchenD.setX(kitchenC.getX() + kitchenC.getWidth());
            kitchenD.setY(512*dm.density);
            homeView.addSprite(kitchenD);

            Bitmap kitchenEBm = BitmapFactory.decodeResource(getResources(), R.drawable.house_kitchen_e);
            AnimatedBitmapSprite kitchenE = new AnimatedBitmapSprite(kitchenEBm);
            kitchenE.setX(kitchenD.getX() + kitchenD.getWidth());
            kitchenE.setY(512*dm.density);
            homeView.addSprite(kitchenE);

            Bitmap toasterBm = BitmapFactory.decodeResource(getResources(), R.drawable.house_toaster1);
            TouchStateAnimatedBitmapSprite toaster = new TouchStateAnimatedBitmapSprite(toasterBm, this);
            toaster.setX(1085*dm.density);
            toaster.setY(565*dm.density);
            toaster.setResources(getResources());
            List<Integer> toastDown = new ArrayList<>(2);
            toastDown.add(R.drawable.house_toaster2);
            toastDown.add(R.drawable.house_toaster3);
            toaster.getBitmapIds().put(1, toastDown);
            toaster.setStateTransition(1, TouchStateAnimatedBitmapSprite.TRANSITION_LOOP1);
            List<Integer> toastIn = new ArrayList<>(1);
            toastIn.add(R.drawable.house_toaster4);
            toastIn.add(R.drawable.house_toaster4);
            toaster.getBitmapIds().put(2, toastIn);
            toaster.setStateTransition(2, 10);
            List<Integer> toastUp = new ArrayList<>(17);
            toastUp.add(R.drawable.house_toaster5);
            toastUp.add(R.drawable.house_toaster6);
            toastUp.add(R.drawable.house_toaster7);
            toastUp.add(R.drawable.house_toaster8);
            toastUp.add(R.drawable.house_toaster9);
            toastUp.add(R.drawable.house_toaster10);
            toastUp.add(R.drawable.house_toaster11);
            toastUp.add(R.drawable.house_toaster12);
            toastUp.add(R.drawable.house_toaster13);
            toastUp.add(R.drawable.house_toaster14);
            toastUp.add(R.drawable.house_toaster15);
            toastUp.add(R.drawable.house_toaster16);
            toastUp.add(R.drawable.house_toaster17);
            toastUp.add(R.drawable.house_toaster18);
            toastUp.add(R.drawable.house_toaster19);
            toastUp.add(R.drawable.house_toaster20);
            toastUp.add(R.drawable.house_toaster21);
            toaster.getBitmapIds().put(3, toastUp);
            toaster.setStateTransition(3, TouchStateAnimatedBitmapSprite.TRANSITION_LOOP1);
            toaster.getAudio().put(1, R.raw.toaster1);
            toaster.getAudio().put(3, R.raw.toaster2);
            homeView.addSprite(toaster);

            Bitmap kettleBm = BitmapFactory.decodeResource(getResources(), R.drawable.house_kitchen_kettle);
            TouchStateAnimatedBitmapSprite kettle = new TouchStateAnimatedBitmapSprite(kettleBm, this);
            kettle.setX(1120 * dm.density);
            kettle.setY(562 * dm.density);
            kettle.setResources(getResources());
            List<Integer> warmingUp = new ArrayList<>(5);
            warmingUp.add(R.drawable.house_kitchen_kettle2);
            warmingUp.add(R.drawable.house_kitchen_kettle3);
            warmingUp.add(R.drawable.house_kitchen_kettle4);
            warmingUp.add(R.drawable.house_kitchen_kettle5);
            warmingUp.add(R.drawable.house_kitchen_kettle6);
            kettle.getBitmapIds().put(1, warmingUp);
            kettle.setStateTransition(1, TouchStateAnimatedBitmapSprite.TRANSITION_LOOP3);
            List<Integer> steaming = new ArrayList<>(5);
            steaming.add(R.drawable.house_kitchen_kettle7);
            steaming.add(R.drawable.house_kitchen_kettle8);
            steaming.add(R.drawable.house_kitchen_kettle9);
            steaming.add(R.drawable.house_kitchen_kettle10);
            steaming.add(R.drawable.house_kitchen_kettle11);
            kettle.getBitmapIds().put(2, steaming);
            kettle.setStateTransition(2, TouchStateAnimatedBitmapSprite.TRANSITION_LOOP3);
            kettle.getAudio().put(1, R.raw.kettle);
            homeView.addSprite(kettle);

            Bitmap bubbles1bm = BitmapFactory.decodeResource(getResources(), R.drawable.bubbles1);
            TouchEventGameSprite bubbles = new TouchEventGameSprite(bubbles1bm, TOPIC_START_BUBBLES, dm);
            bubbles.getBitmaps().get(0).add(BitmapFactory.decodeResource(getResources(), R.drawable.bubbles2));
            bubbles.getBitmaps().get(0).add(BitmapFactory.decodeResource(getResources(), R.drawable.bubbles3));
            bubbles.getBitmaps().get(0).add(BitmapFactory.decodeResource(getResources(), R.drawable.bubbles4));
            bubbles.getBitmaps().get(0).add(BitmapFactory.decodeResource(getResources(), R.drawable.bubbles5));
            bubbles.getBitmaps().get(0).add(BitmapFactory.decodeResource(getResources(), R.drawable.bubbles6));
            bubbles.getBitmaps().get(0).add(BitmapFactory.decodeResource(getResources(), R.drawable.bubbles7));
            bubbles.getBitmaps().get(0).add(BitmapFactory.decodeResource(getResources(), R.drawable.bubbles8));
            bubbles.setStepsPerFrame(5);
            bubbles.setX(910*dm.density);
            bubbles.setY(567*dm.density);
            bubbles.setSelectable(true);
            bubbles.setIgnoreAlpha(true);
            homeView.addSprite(bubbles);
            homeView.addActivitySprite(bubbles);

            Bitmap freezerBm = BitmapFactory.decodeResource(getResources(), R.drawable.house_kitchen_freezer);
            TouchStateAnimatedBitmapSprite freezer = new TouchStateAnimatedBitmapSprite(freezerBm, this);
            freezer.setX(1043 * dm.density);
            freezer.setY(545 * dm.density);
            freezer.setResources(getResources());
            List<Integer> freezeOpen = new ArrayList<>(6);
            freezeOpen.add(R.drawable.house_kitchen_freezer1);
            freezeOpen.add(R.drawable.house_kitchen_freezer2);
            freezeOpen.add(R.drawable.house_kitchen_freezer3);
            freezeOpen.add(R.drawable.house_kitchen_freezer4);
            freezeOpen.add(R.drawable.house_kitchen_freezer5);
            freezer.getBitmapIds().put(1, freezeOpen);
            freezer.setStateTransition(1, TouchStateAnimatedBitmapSprite.TRANSITION_LOOP1);
            List<Integer> freezeOpened = new ArrayList<>(1);
            freezeOpened.add(R.drawable.house_kitchen_freezer6);
            freezer.getBitmapIds().put(2, freezeOpened);
            List<Integer> freezeClose = new ArrayList<>(freezeOpen);
            Collections.reverse(freezeClose);
            freezer.getBitmapIds().put(3, freezeClose);
            freezer.setStateTransition(3, TouchStateAnimatedBitmapSprite.TRANSITION_LOOP1);
            homeView.addSprite(freezer);

            Bitmap fridgeBm = BitmapFactory.decodeResource(getResources(), R.drawable.house_kitchen_fridge);
            TouchStateAnimatedBitmapSprite fridge = new TouchStateAnimatedBitmapSprite(fridgeBm, this);
            fridge.setX(1043 * dm.density);
            fridge.setY(580 * dm.density);
            fridge.setResources(getResources());
            List<Integer> fridgeOpen = new ArrayList<>(6);
            fridgeOpen.add(R.drawable.house_kitchen_fridge1);
            fridgeOpen.add(R.drawable.house_kitchen_fridge2);
            fridgeOpen.add(R.drawable.house_kitchen_fridge3);
            fridgeOpen.add(R.drawable.house_kitchen_fridge4);
            fridgeOpen.add(R.drawable.house_kitchen_fridge5);
            fridge.getBitmapIds().put(1, fridgeOpen);
            fridge.setStateTransition(1, TouchStateAnimatedBitmapSprite.TRANSITION_LOOP1);
            List<Integer> fridgeOpened = new ArrayList<>(1);
            fridgeOpened.add(R.drawable.house_kitchen_fridge6);
            fridge.getBitmapIds().put(2, fridgeOpened);
            List<Integer> fridgeClose = new ArrayList<>(fridgeOpen);
            Collections.reverse(fridgeClose);
            fridge.getBitmapIds().put(3, fridgeClose);
            fridge.setStateTransition(3, TouchStateAnimatedBitmapSprite.TRANSITION_LOOP1);
            homeView.addSprite(fridge);


            Bitmap wardrobeBm = BitmapFactory.decodeResource(getResources(), R.drawable.house_adult_wardrobe);
            TouchStateAnimatedBitmapSprite wardrobe = new TouchStateAnimatedBitmapSprite(wardrobeBm, this);
            wardrobe.setX(747*dm.density);
            wardrobe.setY(350*dm.density);
            wardrobe.setSelectable(true);
            List<Integer> opening = new ArrayList<>(8);
            opening.add(R.drawable.house_adult_wardrobe1);
            opening.add(R.drawable.house_adult_wardrobe2);
            opening.add(R.drawable.house_adult_wardrobe3);
            opening.add(R.drawable.house_adult_wardrobe4);
            opening.add(R.drawable.house_adult_wardrobe5);
            opening.add(R.drawable.house_adult_wardrobe6);
            opening.add(R.drawable.house_adult_wardrobe7);
            opening.add(R.drawable.house_adult_wardrobe8);
            wardrobe.getBitmapIds().put(1, opening);
            wardrobe.setStateTransition(1, TouchStateAnimatedBitmapSprite.TRANSITION_LOOP1);
            List<Integer> opening2 = new ArrayList<>(2);
            opening2.add(R.drawable.house_adult_wardrobe8);
            opening2.add(R.drawable.house_adult_wardrobe8);
            wardrobe.getBitmapIds().put(2, opening2);
            wardrobe.setStateTransition(2, TouchStateAnimatedBitmapSprite.TRANSITION_LOOP3);
            wardrobe.setStateTransitionEvent(2, TOPIC_START_HERITAGE_CALC);
            wardrobe.setResources(getResources());
            homeView.addSprite(wardrobe);
            homeView.addActivitySprite(wardrobe);

            Bitmap adultBedBm = BitmapFactory.decodeResource(getResources(), R.drawable.house_adult_bed);
            AnimatedBitmapSprite adultBed = new AnimatedBitmapSprite(adultBedBm);
            adultBed.setX(487*dm.density);
            adultBed.setY(346*dm.density);
            homeView.addSprite(adultBed);

            Bitmap adultVanityBm = BitmapFactory.decodeResource(getResources(), R.drawable.house_adult_vanity);
            TouchStateAnimatedBitmapSprite adultVanity = new TouchStateAnimatedBitmapSprite(adultVanityBm, this);
            adultVanity.setX(673*dm.density);
            adultVanity.setY(400*dm.density);
            List<Integer> animatingVanity = new ArrayList<>(12);
            animatingVanity.add(R.drawable.house_adult_vanity1);
            animatingVanity.add(R.drawable.house_adult_vanity2);
            animatingVanity.add(R.drawable.house_adult_vanity3);
            animatingVanity.add(R.drawable.house_adult_vanity4);
            animatingVanity.add(R.drawable.house_adult_vanity5);
            animatingVanity.add(R.drawable.house_adult_vanity6);
            animatingVanity.add(R.drawable.house_adult_vanity7);
            animatingVanity.add(R.drawable.house_adult_vanity8);
            animatingVanity.add(R.drawable.house_adult_vanity9);
            animatingVanity.add(R.drawable.house_adult_vanity10);
            animatingVanity.add(R.drawable.house_adult_vanity11);
            animatingVanity.add(R.drawable.house_adult_vanity12);
            adultVanity.getBitmapIds().put(1, animatingVanity);
            adultVanity.setStateTransition(1, TouchStateAnimatedBitmapSprite.TRANSITION_LOOP1);
            List<Integer> opening3 = new ArrayList<>(2);
            opening3.add(R.drawable.house_adult_vanity12);
            opening3.add(R.drawable.house_adult_vanity12);
            adultVanity.getBitmapIds().put(2, opening3);
            adultVanity.setStateTransition(2, TouchStateAnimatedBitmapSprite.TRANSITION_LOOP3);
            adultVanity.setStateTransitionEvent(2, TOPIC_START_BIRTHDAY_CARD);
			adultVanity.setResources(getResources());
            homeView.addSprite(adultVanity);
            homeView.addActivitySprite(adultVanity);

            Bitmap lightABm = BitmapFactory.decodeResource(getResources(), R.drawable.house_light_a1);
            TouchStateAnimatedBitmapSprite lightA = new TouchStateAnimatedBitmapSprite(lightABm, this);
            lightA.setX(670*dm.density);
            lightA.setY(326*dm.density);
            List<Bitmap> lightAonOff = new ArrayList<>(1);
            lightAonOff.add(BitmapFactory.decodeResource(getResources(), R.drawable.house_light_a2));
            lightA.getBitmaps().put(1, lightAonOff);
            lightA.getAudio().put(0, R.raw.pullchainslowon);
            lightA.getAudio().put(1, R.raw.pullchainslowon);
            homeView.addSprite(lightA);

            Bitmap lightBBm = BitmapFactory.decodeResource(getResources(), R.drawable.house_light_b1);
            TouchStateAnimatedBitmapSprite lightB = new TouchStateAnimatedBitmapSprite(lightBBm, this);
            lightB.setX(522*dm.density);
            lightB.setY(325*dm.density);
            List<Bitmap> lightBonOff = new ArrayList<>(1);
            lightBonOff.add(BitmapFactory.decodeResource(getResources(), R.drawable.house_light_b2));
            lightB.getBitmaps().put(1, lightBonOff);
            lightB.getAudio().put(0, R.raw.pullchainslowon);
            lightB.getAudio().put(1, R.raw.pullchainslowon);
            homeView.addSprite(lightB);


            Bitmap blocksBm = BitmapFactory.decodeResource(getResources(), R.drawable.house_toys_blocks);
            TouchStateAnimatedBitmapSprite blocks = new TouchStateAnimatedBitmapSprite(blocksBm, this);
            blocks.setX(1020*dm.density);
            blocks.setY(270*dm.density);
            blocks.setSelectable(true);
            blocks.setResources(getResources());
            List<Integer> blockAnim = new ArrayList<>(8);
            blockAnim.add(R.drawable.house_toys_blocks1);
            blockAnim.add(R.drawable.house_toys_blocks2);
            blockAnim.add(R.drawable.house_toys_blocks3);
            blockAnim.add(R.drawable.house_toys_blocks4);
            blockAnim.add(R.drawable.house_toys_blocks5);
            blockAnim.add(R.drawable.house_toys_blocks6);
            blockAnim.add(R.drawable.house_toys_blocks7);
            blockAnim.add(R.drawable.house_toys_blocks8);
            blocks.getBitmapIds().put(1, blockAnim);
            blocks.setStateTransition(1, TouchStateAnimatedBitmapSprite.TRANSITION_LOOP1);
            blocks.setStateTransitionEvent(0, TOPIC_START_PUZZLE);
            homeView.addSprite(blocks);
            homeView.addActivitySprite(blocks);

            Bitmap horseBm = BitmapFactory.decodeResource(getResources(), R.drawable.house_toys_horse);
            TouchStateAnimatedBitmapSprite horse = new TouchStateAnimatedBitmapSprite(horseBm, this);
            horse.setSelectable(true);
            horse.setResources(getResources());
            List<Integer> horseAnim = new ArrayList<>(11);
            horseAnim.add(R.drawable.house_toys_horse1);
            horseAnim.add(R.drawable.house_toys_horse2);
            horseAnim.add(R.drawable.house_toys_horse3);
            horseAnim.add(horseAnim.get(1));
            horseAnim.add(horseAnim.get(0));
            horseAnim.add(R.drawable.house_toys_horse);
            horseAnim.add(R.drawable.house_toys_horse4);
            horseAnim.add(R.drawable.house_toys_horse5);
            horseAnim.add(R.drawable.house_toys_horse6);
            horseAnim.add(horseAnim.get(7));
            horseAnim.add(horseAnim.get(6));
            horseAnim.add(R.drawable.house_toys_horse);
            horse.getBitmapIds().put(1, horseAnim);
            horse.setStateTransition(1, TouchStateAnimatedBitmapSprite.TRANSITION_LOOP3);
            horse.setX(925*dm.density);
            horse.setY(240*dm.density);
            homeView.addSprite(horse);

            Bitmap batBm = BitmapFactory.decodeResource(getResources(), R.drawable.house_toys_bat);
            TouchStateAnimatedBitmapSprite bat = new TouchStateAnimatedBitmapSprite(batBm, this);
            bat.setX(802*dm.density);
            bat.setY(156*dm.density);
            bat.setSelectable(true);
            bat.setResources(getResources());
            List<Integer> batAnim1 = new ArrayList<>(8);
            batAnim1.add(R.drawable.house_toys_bat1);
            batAnim1.add(R.drawable.house_toys_bat2);
            batAnim1.add(R.drawable.house_toys_bat3);
            batAnim1.add(R.drawable.house_toys_bat4);
            batAnim1.add(R.drawable.house_toys_bat5);
            batAnim1.add(R.drawable.house_toys_bat6);
            batAnim1.add(R.drawable.house_toys_bat7);
            batAnim1.add(R.drawable.house_toys_bat8);
            bat.getBitmapIds().put(1, batAnim1);
            bat.setStateTransition(1, TouchStateAnimatedBitmapSprite.TRANSITION_LOOP1);
            List<Integer> batAnim2 = new ArrayList<>(6);
            batAnim2.add(R.drawable.house_toys_bat9);
            batAnim2.add(R.drawable.house_toys_bat10);
            batAnim2.add(R.drawable.house_toys_bat11);
            batAnim2.add(R.drawable.house_toys_bat12);
            batAnim2.add(R.drawable.house_toys_bat13);
            batAnim2.add(R.drawable.house_toys_bat14);
            bat.getBitmapIds().put(2, batAnim2);
            bat.setStateTransition(2, TouchStateAnimatedBitmapSprite.TRANSITION_LOOP1);
            bat.getAudio().put(2, R.raw.baseball_bat);
            List<Integer> batAnim3 = new ArrayList<>(8);
            batAnim3.add(R.drawable.house_toys_bat15);
            batAnim3.add(R.drawable.house_toys_bat16);
            batAnim3.add(R.drawable.house_toys_bat17);
            batAnim3.add(R.drawable.house_toys_bat18);
            batAnim3.add(R.drawable.house_toys_bat19);
            batAnim3.add(R.drawable.house_toys_bat20);
            batAnim3.add(R.drawable.house_toys_bat21);
            batAnim3.add(R.drawable.house_toys_bat22);
            bat.getBitmapIds().put(3, batAnim3);
            bat.setStateTransition(3, TouchStateAnimatedBitmapSprite.TRANSITION_LOOP1);
            bat.getAudio().put(3, R.raw.glass_break);
            homeView.addSprite(bat);

            Bitmap pianoBm = BitmapFactory.decodeResource(getResources(), R.drawable.house_music_piano);
            TouchStateAnimatedBitmapSprite piano = new TouchStateAnimatedBitmapSprite(pianoBm, this);
            piano.setResources(getResources());
            piano.setX(625 * dm.density);
            piano.setY(225 * dm.density);
            List<Integer> pstate1 = new ArrayList<>(3);
            pstate1.add(R.drawable.house_music_piano1);
            pstate1.add(R.drawable.house_music_piano2);
            pstate1.add(R.drawable.house_music_piano3);
            piano.getBitmapIds().put(1, pstate1);
            piano.setStateTransition(1, TouchStateAnimatedBitmapSprite.TRANSITION_LOOP1);
            List<Integer> pstate2 = new ArrayList<>(pstate1);
            Collections.reverse(pstate2);
            piano.getBitmapIds().put(2, pstate2);
            piano.setStateTransition(2, TouchStateAnimatedBitmapSprite.TRANSITION_LOOP1);
            piano.getBitmapIds().put(3, pstate1);
            piano.setStateTransition(3, TouchStateAnimatedBitmapSprite.TRANSITION_LOOP1);
            piano.getBitmapIds().put(4, pstate2);
            piano.setStateTransition(4, TouchStateAnimatedBitmapSprite.TRANSITION_LOOP1);
            piano.getBitmapIds().put(5, pstate1);
            piano.setStateTransition(5, TouchStateAnimatedBitmapSprite.TRANSITION_LOOP1);
            piano.getBitmapIds().put(6, pstate2);
            piano.setStateTransition(6, TouchStateAnimatedBitmapSprite.TRANSITION_LOOP1);
			piano.setStateTransitionEvent(0, TOPIC_START_SONG);
            piano.getAudio().put(1, R.raw.piano);
            homeView.addSprite(piano);
			homeView.addActivitySprite(piano);

            Bitmap guitarBm = BitmapFactory.decodeResource(getResources(), R.drawable.house_music_guitar);
            TouchStateAnimatedBitmapSprite guitar = new TouchStateAnimatedBitmapSprite(guitarBm, this);
            guitar.setResources(getResources());
            guitar.setX(700 * dm.density);
            guitar.setY(242 * dm.density);
            List<Integer> playing = new ArrayList<>(4);
            playing.add(R.drawable.house_music_guitar1);
            playing.add(R.drawable.house_music_guitar2);
            playing.add(R.drawable.house_music_guitar3);
            playing.add(R.drawable.house_music_guitar2);
            guitar.getBitmapIds().put(1, playing);
            guitar.setStateTransition(1, TouchStateAnimatedBitmapSprite.TRANSITION_LOOP5);
            guitar.getAudio().put(1, R.raw.guitar);
            homeView.addSprite(guitar);

            Bitmap trumpetBm = BitmapFactory.decodeResource(getResources(), R.drawable.house_music_trumpet);
            TouchStateAnimatedBitmapSprite trumpet = new TouchStateAnimatedBitmapSprite(trumpetBm, this);
            trumpet.setResources(getResources());
            trumpet.setIgnoreAlpha(true);
            trumpet.setX(660*dm.density);
            trumpet.setY(200*dm.density);
            List<Integer> playingTrumptet = new ArrayList<>(4);
            playingTrumptet.add(R.drawable.house_music_trumpet1);
            playingTrumptet.add(R.drawable.house_music_trumpet2);
            playingTrumptet.add(R.drawable.house_music_trumpet3);
            playingTrumptet.add(R.drawable.house_music_trumpet2);
            trumpet.getBitmapIds().put(1, playingTrumptet);
            trumpet.setStateTransition(1, TouchStateAnimatedBitmapSprite.TRANSITION_LOOP5);
            trumpet.getAudio().put(1, R.raw.trumpet);
            homeView.addSprite(trumpet);

            Bitmap drumsBm = BitmapFactory.decodeResource(getResources(), R.drawable.house_music_drums);
            TouchStateAnimatedBitmapSprite drums = new TouchStateAnimatedBitmapSprite(drumsBm, this);
            drums.setResources(getResources());
            drums.setX(585 * dm.density);
            drums.setY(275 * dm.density);
            List<Integer> playingDrums = new ArrayList<>(8);
            playingDrums.add(R.drawable.house_music_drums1);
            playingDrums.add(R.drawable.house_music_drums2);
            playingDrums.add(R.drawable.house_music_drums3);
            playingDrums.add(R.drawable.house_music_drums4);
            playingDrums.add(R.drawable.house_music_drums5);
            playingDrums.add(R.drawable.house_music_drums6);
            playingDrums.add(R.drawable.house_music_drums7);
            playingDrums.add(R.drawable.house_music_drums8);
            drums.getBitmapIds().put(1, playingDrums);
            drums.setStateTransition(1, TouchStateAnimatedBitmapSprite.TRANSITION_LOOP3);
            drums.getAudio().put(1, R.raw.drums);
            homeView.addSprite(drums);

            int clipX = (int) (400*dm.density);
            int clipY = (int) (90*dm.density);
            if (maxWidth - clipX < getScreenWidth()) {
                clipX = maxWidth - getScreenWidth();
                if (clipX < 0) clipX = 0;
            }
            if (maxHeight - clipY < getScreenHeight()) {
                clipY = maxHeight - getScreenHeight();
                if (clipY < 0) clipY = 0;
            }

            homeBackground.setClipX(clipX);
            homeBackground.setClipY(clipY);
            homeView.setClipX(clipX);
            homeView.setClipY(clipY);
        }
    }

    @Override
    public void onEvent(String topic, Object o) {
        super.onEvent(topic, o);
        if (topic.equals(TOPIC_ANIMATE_CAT)) {
            catSprite.setAnimating(true);
        }
    }
}
