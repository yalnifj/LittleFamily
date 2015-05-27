package org.finlayfamily.littlefamily.activities;

import android.app.DialogFragment;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.View;

import org.finlayfamily.littlefamily.R;
import org.finlayfamily.littlefamily.data.LittlePerson;

import java.util.Locale;

/**
 * Created by kids on 4/18/15.
 */
public class LittleFamilyActivity extends FragmentActivity implements TextToSpeech.OnInitListener, TopBarFragment.OnFragmentInteractionListener {
    protected TextToSpeech tts;
    protected MediaPlayer mediaPlayer;
    protected TopBarFragment topBar;
    protected LittlePerson selectedPerson;
    protected LoadingDialog loadingDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        tts = new TextToSpeech(this, this);

        Intent intent = getIntent();
        selectedPerson = (LittlePerson) intent.getSerializableExtra(ChooseFamilyMember.SELECTED_PERSON);
    }

    public void setupTopBar() {
        topBar = (TopBarFragment) getSupportFragmentManager().findFragmentById(R.id.topBarFragment);
        if (topBar==null) {
            topBar = TopBarFragment.newInstance(selectedPerson);
            getSupportFragmentManager().beginTransaction().replace(R.id.topBarFragment, topBar).commit();
        }else {
            Bundle args = new Bundle();
            if (selectedPerson != null) {
                args.putSerializable(TopBarFragment.ARG_PERSON, selectedPerson);
                topBar.setArguments(args);
            }
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        mediaPlayer = MediaPlayer.create(this, R.raw.powerup_success);
    }

    @Override
    protected void onStop() {
        super.onStop();
        mediaPlayer.release();
        mediaPlayer = null;
    }

    public void showLoadingDialog() {
        if (loadingDialog==null) {
            loadingDialog = new LoadingDialog();
            loadingDialog.setStyle(DialogFragment.STYLE_NO_FRAME, R.style.Theme_AppCompat_Light_NoActionBar);
        }
        loadingDialog.show(getFragmentManager(), "Loading");
    }

    public void hideLoadingDialog() {
        loadingDialog.dismiss();
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
        Log.d("LittleFamilyActivity", "Speaking: " + message);
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

    public void onHomeButtonPressed(View view) {
        finish();
    }

    public void onProfileButtonPressed(View view) {

    }

    public void onActivityButtonPressed(View view) {

    }

    public void onSettingsButtonPressed(View view) {

    }
}
