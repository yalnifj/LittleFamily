package org.finlayfamily.littlefamily.activities;

import android.app.Activity;
import android.content.Intent;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import org.finlayfamily.littlefamily.R;
import org.finlayfamily.littlefamily.data.DataService;
import org.finlayfamily.littlefamily.data.ErrorLogger;
import org.finlayfamily.littlefamily.views.AnimationDrawableCallback;

public class SplashActivity extends Activity {

    private AnimationDrawable plantAnimation;
    private DataService dataService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        ImageView iv = (ImageView) findViewById(R.id.animatedImage);
        plantAnimation = (AnimationDrawable) iv.getDrawable();
        plantAnimation.setCallback(new GrowCallback(plantAnimation, iv));

        ErrorLogger.getInstance(this).start();
    }

    @Override
    protected void onStart() {
        super.onStart();
        plantAnimation.start();
        dataService = DataService.getInstance();
        dataService.setContext(this);
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

    public class GrowCallback extends AnimationDrawableCallback {

        public GrowCallback(AnimationDrawable animationDrawable, Drawable.Callback callback) {
            super(animationDrawable, callback);
        }

        private int count = 0;
        @Override
        public void onAnimationAdvanced(int currentFrame, int totalFrames) {
            Log.d("SplashActivity", "currentFrame="+currentFrame);
            if (currentFrame % (totalFrames-1)==0) {
                count++;
                if (count>3) {
                    plantAnimation.stop();
                    gotoChooseFamilyMember(null);
                }
            }
        }

        @Override
        public void onAnimationCompleted() {

        }
    }
}
