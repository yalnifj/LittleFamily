package com.yellowforktech.littlefamilytree.activities;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.PixelFormat;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.SurfaceHolder;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.Space;
import android.widget.TextView;

import com.yellowforktech.littlefamilytree.R;
import com.yellowforktech.littlefamilytree.activities.adapters.SkinListAdapter;
import com.yellowforktech.littlefamilytree.data.LittlePerson;
import com.yellowforktech.littlefamilytree.games.DollConfig;
import com.yellowforktech.littlefamilytree.games.DressUpDolls;
import com.yellowforktech.littlefamilytree.util.ImageHelper;
import com.yellowforktech.littlefamilytree.util.PlaceHelper;
import com.yellowforktech.littlefamilytree.views.DressUpView;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class HeritageDressUpActivity extends LittleFamilyActivity implements DressUpView.DressedListener, View.OnClickListener {

    private DollConfig dollConfig;
    private DressUpView dressUpView;
    private View dollScroller;
    private LinearLayout dollLayout;
    private LittlePerson person;
    private DressUpDolls dressUpDolls;
    private List<DollConfig> allDolls;
    private List<String> allPlaces;
    private String skinColor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_heritage_dress_up);

        Intent intent = getIntent();
        dollConfig = (DollConfig) intent.getSerializableExtra(ChooseCultureActivity.DOLL_CONFIG);
        person = (LittlePerson) intent.getSerializableExtra(ChooseFamilyMember.SELECTED_PERSON);
        if (dollConfig==null) {
            DressUpDolls dressUpDolls = new DressUpDolls();
            dollConfig = dressUpDolls.getDollConfig(PlaceHelper.getTopPlace(person.getBirthPlace()), person, this);
        }

        dressUpView = (DressUpView) findViewById(R.id.dress_up_view);
        dressUpView.setZOrderOnTop(true);    // necessary
        SurfaceHolder sfhTrackHolder = dressUpView.getHolder();
        sfhTrackHolder.setFormat(PixelFormat.TRANSPARENT);

        dollScroller = findViewById(R.id.horizontalScrollView);
        if (dollScroller==null) {
            dollScroller = findViewById(R.id.scrollView);
        }
        dollLayout = (LinearLayout) findViewById(R.id.dollLayout);

        setupTopBar();
    }

    @Override
    public void setupTopBar() {
        if (findViewById(R.id.topBarFragment)!=null) {
            topBar = (TopBarFragment) getSupportFragmentManager().findFragmentById(R.id.topBarFragment);
            if (topBar == null) {
                topBar = TopBarFragment.newInstance(selectedPerson, R.layout.fragment_top_bar_dressup);
                getSupportFragmentManager().beginTransaction().replace(R.id.topBarFragment, topBar).commit();
            } else {
                if (selectedPerson != null) {
                    topBar.getArguments().putSerializable(TopBarFragment.ARG_PERSON, selectedPerson);
                    topBar.updatePerson(selectedPerson);
                }
            }
        }
    }

    @Override
    protected void onStart() {
        super.onStart();

        int skinViewResourceId = ImageHelper.getPersonDefaultImage(this, selectedPerson);

        ImageView imageView = (ImageView) topBar.getView().findViewById(R.id.skinBtn);
        imageView.setImageResource(skinViewResourceId);

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
            allDolls.add(dressUpDolls.getDollConfig(place, person, this));
        }

        DollConfigLoader dcl = new DollConfigLoader();
        dcl.execute();
    }

    @Override
    public void onDressed() {
        playCompleteSound();
        dollScroller.setVisibility(View.VISIBLE);
        dollScroller.bringToFront();
        dollScroller.getParent().requestLayout();
        ((View)dollScroller.getParent()).invalidate();
    }

    @Override
    public void onClick(View v) {
        dollConfig = (DollConfig) v.getTag();
        dressUpView.setDollConfig(dollConfig);
        dollScroller.setVisibility(View.INVISIBLE);
    }

    public void changeSkin(View v) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        final ListAdapter adapter = new SkinListAdapter(this);
        builder.setTitle(R.string.pref_title_skin_color)
                .setAdapter(adapter, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        skinColor = (String) adapter.getItem(which);

                        int skinViewResourceId = ImageHelper.getPersonCartoon(selectedPerson, skinColor);

                        ImageView imageView = (ImageView) topBar.getView().findViewById(R.id.skinBtn);
                        imageView.setImageResource(skinViewResourceId);

                        dollConfig.setSkinTone(skinColor);
                        dressUpView.setDollConfig(dollConfig);
                        dollScroller.setVisibility(View.INVISIBLE);

                        dialog.dismiss();
                    }
                });
        AlertDialog dialog = builder.create();
        dialog.show();
    }


    public class DollConfigLoader extends AsyncTask<String, String, Map<Integer, Bitmap>> {

        @Override
        protected Map<Integer, Bitmap> doInBackground(String... params) {
            DisplayMetrics dm = new DisplayMetrics();
            getWindowManager().getDefaultDisplay().getMetrics(dm);
            Map<Integer, Bitmap> map = new HashMap<>();
            for(int index=0; index < allDolls.size(); index++) {
                DollConfig dc = allDolls.get(index);
                if (dc!=null) {
                    String thumbnailFile = dc.getThumbnail();
                    try {
                        InputStream is = HeritageDressUpActivity.this.getAssets().open(thumbnailFile);
                        Bitmap thumbnail = ImageHelper.loadBitmapFromStream(is, 0, (int) (120 * dm.density), (int) (120 * dm.density));
                        //Bitmap thumbnail = BitmapFactory.decodeStream(is);
                        map.put(index, thumbnail);
                        is.close();
                    } catch (IOException e) {
                        Log.e("DressUpDollsAdapter", "Error opening asset file", e);
                    }
                }
            }
            return map;
        }

        @Override
        protected void onPostExecute(Map<Integer, Bitmap> integerBitmapMap) {
            super.onPostExecute(integerBitmapMap);
            DisplayMetrics dm = new DisplayMetrics();
            getWindowManager().getDefaultDisplay().getMetrics(dm);
            for(int index=0; index < allDolls.size(); index++) {
                LinearLayout ll = new LinearLayout(HeritageDressUpActivity.this);
                ll.setOrientation(LinearLayout.VERTICAL);
                ll.setMinimumWidth((int) (95*dm.density));
                ImageView dollImage = new ImageView(HeritageDressUpActivity.this);
                DollConfig dc = allDolls.get(index);
                if (dc!=null) {
                    Bitmap thumbnail = integerBitmapMap.get(index);
                    dollImage.setImageBitmap(thumbnail);
                }
                dollImage.setTag(dc);
                dollImage.setOnClickListener(HeritageDressUpActivity.this);
                ll.addView(dollImage);
                TextView place = new TextView(HeritageDressUpActivity.this);
                if (Build.VERSION.SDK_INT > 16) {
                    place.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
                }
                String placeTxt = allPlaces.get(index);
                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                params.gravity = Gravity.CENTER_HORIZONTAL;
                place.setText(placeTxt);
                place.setLayoutParams(params);
                ll.addView(place);

                dollLayout.addView(ll);

                Space spacer = new Space(HeritageDressUpActivity.this);
                spacer.setMinimumHeight(15);
                dollLayout.addView(spacer);
            }

        }
    }
}
