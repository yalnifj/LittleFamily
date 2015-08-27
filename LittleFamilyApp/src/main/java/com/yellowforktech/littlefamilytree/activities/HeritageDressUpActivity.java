package com.yellowforktech.littlefamilytree.activities;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.PixelFormat;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.View;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.yellowforktech.littlefamilytree.R;
import com.yellowforktech.littlefamilytree.data.LittlePerson;
import com.yellowforktech.littlefamilytree.games.DollConfig;
import com.yellowforktech.littlefamilytree.games.DressUpDolls;
import com.yellowforktech.littlefamilytree.util.PlaceHelper;
import com.yellowforktech.littlefamilytree.views.DressUpView;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

public class HeritageDressUpActivity extends LittleFamilyActivity implements DressUpView.DressedListener, View.OnClickListener {

    private DollConfig dollConfig;
    private DressUpView dressUpView;
    private HorizontalScrollView dollScroller;
    private LinearLayout dollLayout;
    private LittlePerson person;
    private DressUpDolls dressUpDolls;
    private List<DollConfig> allDolls;
    private List<String> allPlaces;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_heritage_dress_up);

        Intent intent = getIntent();
        dollConfig = (DollConfig) intent.getSerializableExtra(ChooseCultureActivity.DOLL_CONFIG);
        person = (LittlePerson) intent.getSerializableExtra(ChooseFamilyMember.SELECTED_PERSON);
        if (dollConfig==null) {
            DressUpDolls dressUpDolls = new DressUpDolls();
            dollConfig = dressUpDolls.getDollConfig(PlaceHelper.getTopPlace(person.getBirthPlace()), person);
        }

        dressUpView = (DressUpView) findViewById(R.id.dress_up_view);
        dressUpView.setZOrderOnTop(true);    // necessary
        SurfaceHolder sfhTrackHolder = dressUpView.getHolder();
        sfhTrackHolder.setFormat(PixelFormat.TRANSPARENT);

        dollScroller = (HorizontalScrollView) findViewById(R.id.horizontalScrollView);
        dollLayout = (LinearLayout) findViewById(R.id.dollLayout);

        setupTopBar();
    }

    @Override
    protected void onStart() {
        super.onStart();
        dressUpView.setDollConfig(dollConfig);
        dressUpView.addListener(this);
        Bitmap starBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.star1);
        dressUpView.setStarBitmap(starBitmap);
        dollScroller.setVisibility(View.INVISIBLE);

        dressUpDolls = new DressUpDolls();
        Set<String> places = dressUpDolls.getDollPlaces();
        allPlaces = new ArrayList<>(places.size());
        allDolls = new ArrayList<>(places.size());
        for(String place : places) {
            String[] parts = place.split(" ");
            String upPlace = "";
            for(String part : parts) {
                if (part!=null && !part.isEmpty()) {
                    if (!upPlace.isEmpty()) upPlace+=" ";
                    upPlace += part.substring(0, 1).toUpperCase() + part.substring(1);
                }
            }
            allPlaces.add(upPlace);
        }
        Collections.sort(allPlaces);
        for(String place : allPlaces) {
            allDolls.add(dressUpDolls.getDollConfig(place, person));
        }

        for(int index=0; index < allDolls.size(); index++) {
            LinearLayout ll = new LinearLayout(this);
            ll.setOrientation(LinearLayout.VERTICAL);
            ImageView dollImage = new ImageView(this);
            DollConfig dc = allDolls.get(index);
            if (dc!=null) {
                String thumbnailFile = dc.getThumbnail();
                try {
                    InputStream is = this.getAssets().open(thumbnailFile);
                    Bitmap thumbnail = BitmapFactory.decodeStream(is);
                    dollImage.setImageBitmap(thumbnail);
                    is.close();
                } catch (IOException e) {
                    Log.e("DressUpDollsAdapter", "Error opening asset file", e);
                }
            }
            dollImage.setTag(dc);
            dollImage.setOnClickListener(this);
            ll.addView(dollImage);
            TextView place = new TextView(this);
            if (Build.VERSION.SDK_INT > 16) {
                place.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
            }
            String placeTxt = allPlaces.get(index);
            place.setText(placeTxt);
            ll.addView(place);

            dollLayout.addView(ll);
        }
    }

    @Override
    public void onDressed() {
        playCompleteSound();
        dollScroller.setVisibility(View.VISIBLE);
    }

    @Override
    public void onClick(View v) {
        dollConfig = (DollConfig) v.getTag();
        dressUpView.setDollConfig(dollConfig);
        dollScroller.setVisibility(View.INVISIBLE);
    }

    /*
    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        dollConfig = (DollConfig) adapter.getItem(position);
        dressUpView.setDollConfig(dollConfig);
        dollLayout.setVisibility(View.INVISIBLE);
    }
    */
}
