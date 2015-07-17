package org.finlayfamily.littlefamily.activities;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;

import org.finlayfamily.littlefamily.R;
import org.finlayfamily.littlefamily.data.DataService;
import org.finlayfamily.littlefamily.data.ErrorLogger;
import org.finlayfamily.littlefamily.events.EventListener;
import org.finlayfamily.littlefamily.events.EventQueue;
import org.finlayfamily.littlefamily.sprites.TouchStateAnimatedBitmapSprite;
import org.finlayfamily.littlefamily.views.SpritedSurfaceView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SplashActivity extends Activity implements EventListener {

    private SpritedSurfaceView plantView;
    private DataService dataService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        plantView = (SpritedSurfaceView) findViewById(R.id.plantView);

        ErrorLogger logger = ErrorLogger.getInstance(this);
        if (!logger.isAlive()) logger.start();
    }

    @Override
    protected void onStart() {
        super.onStart();
        dataService = DataService.getInstance();
        dataService.setContext(this);

        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);

        EventQueue.getInstance().subscribe("ChooseFamilyMember", this);

        List<Bitmap> plants = new ArrayList<>(7);
        plants.add(BitmapFactory.decodeResource(getResources(), R.drawable.growing_plant1));
        plants.add(BitmapFactory.decodeResource(getResources(), R.drawable.growing_plant2));
        plants.add(BitmapFactory.decodeResource(getResources(), R.drawable.growing_plant3));
        plants.add(BitmapFactory.decodeResource(getResources(), R.drawable.growing_plant4));
        plants.add(BitmapFactory.decodeResource(getResources(), R.drawable.growing_plant5));
        plants.add(BitmapFactory.decodeResource(getResources(), R.drawable.growing_plant6));
        plants.add(BitmapFactory.decodeResource(getResources(), R.drawable.growing_plant7));
        Map<Integer, List<Bitmap>> bitmaps = new HashMap<>(1);
        bitmaps.put(0, plants);
        TouchStateAnimatedBitmapSprite plant = new TouchStateAnimatedBitmapSprite(this);
        plant.getBitmaps().put(0, plants);
        plant.getBitmaps().put(1, plants);
        plant.setWidth((int) (100*dm.density));
        plant.setHeight((int) (150*dm.density));
        plant.setX(0);
        plant.setY(0);
        plant.setStepsPerFrame(5);
        plant.setStateTransition(0, TouchStateAnimatedBitmapSprite.TRANSITION_LOOP3);
        plant.setStateTransition(1, TouchStateAnimatedBitmapSprite.TRANSITION_LOOP1);
        plant.setStateTransitionEvent(1, "ChooseFamilyMember");
        plantView.addSprite(plant);
    }

    @Override
    protected void onStop() {
        super.onStop();
        plantView.stop();
        plantView.getSprites().clear();

        EventQueue.getInstance().unSubscribe("ChooseFamilyMember", this);
    }

    public void gotoChooseFamilyMember(View view) {
        try {
            if (dataService.getServiceType()==null || !dataService.hasData()) {
                Intent intent = new Intent(this, ChooseRemoteService.class);
                startActivity(intent);
            } else {
                Intent intent = new Intent( this, ChooseFamilyMember.class );
                startActivity(intent);
            }
        } catch (Exception e) {
            Log.e("SplashActivity", "Error getting data from DataService", e);
        }
    }

    @Override
    public void onEvent(String topic, Object o) {
        gotoChooseFamilyMember(null);
    }
}
