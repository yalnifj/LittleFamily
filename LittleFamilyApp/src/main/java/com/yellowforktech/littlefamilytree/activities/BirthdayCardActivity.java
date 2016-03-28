package com.yellowforktech.littlefamilytree.activities;

import android.content.Intent;
import android.graphics.PixelFormat;
import android.os.Bundle;
import android.util.Log;
import android.view.SurfaceHolder;

import com.yellowforktech.littlefamilytree.R;
import com.yellowforktech.littlefamilytree.data.DataService;
import com.yellowforktech.littlefamilytree.data.LittlePerson;
import com.yellowforktech.littlefamilytree.views.BirthdayCardSurfaceView;

import java.util.ArrayList;
import java.util.List;

public class BirthdayCardActivity extends LittleFamilyActivity {

    private BirthdayCardSurfaceView view;
    private List<LittlePerson> people;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_birthday_card);

        view = (BirthdayCardSurfaceView) findViewById(R.id.view);
        view.setActivity(this);
        view.setZOrderOnTop(true);    // necessary
        SurfaceHolder sfhTrackHolder = view.getHolder();
        sfhTrackHolder.setFormat(PixelFormat.TRANSPARENT);

        Intent intent = getIntent();
        selectedPerson = (LittlePerson) intent.getSerializableExtra(ChooseFamilyMember.SELECTED_PERSON);

        setupTopBar();
    }

    @Override
    protected void onStart() {
        super.onStart();
        DataService.getInstance().registerNetworkStateListener(this);
        try {
            people = DataService.getInstance().getDBHelper().getNext30Birthdays();
            for(LittlePerson p : people) {
                Log.d("BirthdayCardActivity", "Found person "+p.getName()+" born on "+p.getBirthDate());
            }
            if (people.size()==0) {
                Log.d("BirthdayCardActivity", "Unable to find birthdays from database");
                people.add(selectedPerson);
            }
        } catch (Exception e) {
            Log.e("BirthdayCardActivity", "Error getting birthday people", e);
            people = new ArrayList<>();
            people.add(selectedPerson);
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        DataService.getInstance().unregisterNetworkStateListener(this);
    }
}
