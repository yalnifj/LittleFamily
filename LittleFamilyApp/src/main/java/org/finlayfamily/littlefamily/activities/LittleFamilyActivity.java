package org.finlayfamily.littlefamily.activities;

import android.app.Activity;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import org.finlayfamily.littlefamily.R;

import java.util.Locale;

/**
 * Created by kids on 4/18/15.
 */
public class LittleFamilyActivity extends Activity implements TextToSpeech.OnInitListener {
    protected TextToSpeech tts;
    protected MediaPlayer mediaPlayer;
    protected ImageView homeButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        tts = new TextToSpeech(this, this);

        homeButton = (ImageView) findViewById(R.id.homeButton);
    }

    @Override
    protected void onStart() {
        super.onStart();
        mediaPlayer = MediaPlayer.create(this, R.raw.powerup_success);
        if (homeButton!=null) {
            homeButton.setMaxWidth(this.getWindow().getAttributes().width / 10);
            homeButton.setMaxHeight(this.getWindow().getAttributes().height / 10);
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        mediaPlayer.release();
        mediaPlayer = null;
    }

    @Override
    public void onInit(int code) {
        if (code == TextToSpeech.SUCCESS) {
            tts.setLanguage(Locale.getDefault());
            tts.setSpeechRate(0.9f);
        } else {
            tts = null;
            //Toast.makeText(this, "Failed to initialize TTS engine.", Toast.LENGTH_SHORT).show();
            Log.e("LittleFamilyActivity", "Error intializing speech");
        }
    }

    @Override
    protected void onDestroy() {
        if (tts!=null) {
            tts.stop();
            tts.shutdown();
        }
        super.onDestroy();
    }

    protected void speak(String message) {
        Log.d("LittleFamilyActivity", "Speaking: "+message);
        if (tts!=null) {
            if (Build.VERSION.SDK_INT > 20) {
                tts.speak(message, TextToSpeech.QUEUE_FLUSH, null, null);
            }
            else {
                tts.speak(message, TextToSpeech.QUEUE_FLUSH, null);
            }
        }
    }

    public void playCompleteSound() {
        if (mediaPlayer!=null) mediaPlayer.start();
    }

    public void gotoHomeScreen(View view) {
        finish();
    }
}
