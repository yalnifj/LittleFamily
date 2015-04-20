package org.finlayfamily.littlefamily.activities;

import android.app.Activity;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;

import org.finlayfamily.littlefamily.R;
import org.finlayfamily.littlefamily.data.LittlePerson;
import org.finlayfamily.littlefamily.games.DollConfig;
import org.finlayfamily.littlefamily.views.DressUpView;

public class HeritageDressUpActivity extends LittleFamilyActivity implements DressUpView.DressedListener {

    private DollConfig dollConfig;
    private DressUpView dressUpView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_heritage_dress_up);

        dressUpView = (DressUpView) findViewById(R.id.dress_up_view);

        Intent intent = getIntent();
        dollConfig = (DollConfig) intent.getSerializableExtra(ChooseCultureActivity.DOLL_CONFIG);
    }

    @Override
    protected void onStart() {
        super.onStart();
        dressUpView.setDollConfig(dollConfig);
        dressUpView.addListener(this);
    }



    @Override
    public void onDressed() {
        playCompleteSound();
    }
}
