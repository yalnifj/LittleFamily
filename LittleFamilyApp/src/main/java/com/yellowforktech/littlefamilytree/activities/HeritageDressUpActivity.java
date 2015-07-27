package com.yellowforktech.littlefamilytree.activities;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.PixelFormat;
import android.os.Bundle;
import android.view.SurfaceHolder;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;

import com.yellowforktech.littlefamilytree.R;
import com.yellowforktech.littlefamilytree.activities.adapters.DressUpDollsAdapter;
import com.yellowforktech.littlefamilytree.data.LittlePerson;
import com.yellowforktech.littlefamilytree.games.DollConfig;
import com.yellowforktech.littlefamilytree.games.DressUpDolls;
import com.yellowforktech.littlefamilytree.util.PlaceHelper;
import com.yellowforktech.littlefamilytree.views.DressUpView;

public class HeritageDressUpActivity extends LittleFamilyActivity implements DressUpView.DressedListener, AdapterView.OnItemClickListener {

    private DollConfig dollConfig;
    private DressUpView dressUpView;
    private DressUpDollsAdapter adapter;
    private GridView dollGrid;
    private LittlePerson person;

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

        dollGrid = (GridView) findViewById(R.id.dollGrid);
        adapter = new DressUpDollsAdapter(this, person);
        dollGrid.setAdapter(adapter);
        dollGrid.setNumColumns(adapter.getCount());
        dollGrid.setOnItemClickListener(this);

        setupTopBar();
    }

    @Override
    protected void onStart() {
        super.onStart();
        dressUpView.setDollConfig(dollConfig);
        dressUpView.addListener(this);
        Bitmap starBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.star1);
        dressUpView.setStarBitmap(starBitmap);
        dollGrid.setVisibility(View.INVISIBLE);
    }

    @Override
    public void onDressed() {
        playCompleteSound();
        dollGrid.setVisibility(View.VISIBLE);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        dollConfig = (DollConfig) adapter.getItem(position);
        dressUpView.setDollConfig(dollConfig);
        dollGrid.setVisibility(View.INVISIBLE);
    }
}
