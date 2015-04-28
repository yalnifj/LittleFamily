package org.finlayfamily.littlefamily.activities;

import android.graphics.drawable.AnimationDrawable;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;

import org.finlayfamily.littlefamily.R;

public class SplashActivity extends LittleFamilyActivity {

    private AnimationDrawable plantAnimation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        ImageView iv = (ImageView) findViewById(R.id.animatedImage);
        plantAnimation = (AnimationDrawable) iv.getDrawable();
    }

    @Override
    protected void onStart() {
        super.onStart();
        plantAnimation.start();
    }
}
