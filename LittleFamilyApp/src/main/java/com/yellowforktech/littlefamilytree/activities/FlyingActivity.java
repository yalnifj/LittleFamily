package com.yellowforktech.littlefamilytree.activities;

import android.os.Bundle;

import com.yellowforktech.littlefamilytree.R;
import com.yellowforktech.littlefamilytree.views.FlyingSurfaceView;

public class FlyingActivity extends LittleFamilyActivity {

    private FlyingSurfaceView flyView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_flying);

        flyView = (FlyingSurfaceView) findViewById(R.id.flyingView);

        setupTopBar();
    }

    @Override
    protected void onStart() {
        super.onStart();

        flyView.resume();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        flyView.stop();
    }

    @Override
    protected void onPause() {
        super.onPause();
        flyView.pause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        flyView.resume();
    }
}
