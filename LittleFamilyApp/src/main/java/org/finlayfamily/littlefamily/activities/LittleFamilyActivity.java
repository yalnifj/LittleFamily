package org.finlayfamily.littlefamily.activities;

import android.app.DialogFragment;
import android.content.Intent;
import android.graphics.Point;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.Display;
import android.view.View;

import org.finlayfamily.littlefamily.R;
import org.finlayfamily.littlefamily.data.LittlePerson;
import org.finlayfamily.littlefamily.events.EventListener;
import org.finlayfamily.littlefamily.events.EventQueue;

import java.util.ArrayList;
import java.util.Locale;

/**
 * Created by kids on 4/18/15.
 */
public class LittleFamilyActivity extends FragmentActivity implements TextToSpeech.OnInitListener, TopBarFragment.OnFragmentInteractionListener, EventListener {
    public static final String TOPIC_START_MATCH    = "startMatch";
    public static final String TOPIC_START_SCRATCH  = "startScratch";
    public static final String TOPIC_START_COLORING = "startColoring";
    public static final String TOPIC_START_DRESSUP  = "startDressUp";
    public static final String TOPIC_START_PUZZLE   = "startPuzzle";
    public static final String TOPIC_START_TREE = "startTree";
    protected TextToSpeech tts;
    protected MediaPlayer mediaPlayer;
    protected TopBarFragment topBar;
    protected LittlePerson selectedPerson;
    protected LoadingDialog loadingDialog;
    protected ArrayList<LittlePerson> people;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        tts = new TextToSpeech(this, this);

        Intent intent = getIntent();
        selectedPerson = (LittlePerson) intent.getSerializableExtra(ChooseFamilyMember.SELECTED_PERSON);
        people = (ArrayList<LittlePerson>) intent.getSerializableExtra(ChooseFamilyMember.FAMILY);
    }

    public void setupTopBar() {
        if (findViewById(R.id.topBarFragment)!=null) {
            topBar = (TopBarFragment) getSupportFragmentManager().findFragmentById(R.id.topBarFragment);
            if (topBar == null) {
                topBar = TopBarFragment.newInstance(selectedPerson);
                getSupportFragmentManager().beginTransaction().replace(R.id.topBarFragment, topBar).commit();
            } else {
                Bundle args = new Bundle();
                if (selectedPerson != null) {
                    args.putSerializable(TopBarFragment.ARG_PERSON, selectedPerson);
                    topBar.setArguments(args);
                }
            }
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        mediaPlayer = MediaPlayer.create(this, R.raw.powerup_success);
        EventQueue.getInstance().subscribe(TOPIC_START_MATCH, this);
        EventQueue.getInstance().subscribe(TOPIC_START_COLORING, this);
        EventQueue.getInstance().subscribe(TOPIC_START_DRESSUP, this);
        EventQueue.getInstance().subscribe(TOPIC_START_PUZZLE, this);
        EventQueue.getInstance().subscribe(TOPIC_START_SCRATCH, this);
        EventQueue.getInstance().subscribe(TOPIC_START_TREE, this);
    }

    @Override
    protected void onStop() {
        super.onStop();
        mediaPlayer.release();
        mediaPlayer = null;
        EventQueue.getInstance().unSubscribe(TOPIC_START_MATCH, this);
        EventQueue.getInstance().unSubscribe(TOPIC_START_COLORING, this);
        EventQueue.getInstance().unSubscribe(TOPIC_START_DRESSUP, this);
        EventQueue.getInstance().unSubscribe(TOPIC_START_PUZZLE, this);
        EventQueue.getInstance().unSubscribe(TOPIC_START_SCRATCH, this);
        EventQueue.getInstance().unSubscribe(TOPIC_START_TREE, this);
    }

    public void showLoadingDialog() {
        if (loadingDialog==null) {
            loadingDialog = new LoadingDialog();
            loadingDialog.setStyle(DialogFragment.STYLE_NO_TITLE, R.style.Theme_AppCompat_Dialog);
        }
        if (!loadingDialog.isVisible()) {
            loadingDialog.show(getFragmentManager(), "Loading");
        }
    }

    public void hideLoadingDialog() {
        if (loadingDialog!=null && loadingDialog.isVisible()) {
            loadingDialog.dismissAllowingStateLoss();
        }
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

    private Point screenSize = null;
    public int getScreenWidth() {
        if (screenSize==null) {
            Display display = getWindowManager().getDefaultDisplay();
            screenSize = new Point();
            display.getSize(screenSize);
        }
        return screenSize.x;
    }

    public int getScreenHeight() {
        if (screenSize==null) {
            Display display = getWindowManager().getDefaultDisplay();
            screenSize = new Point();
            display.getSize(screenSize);
        }
        return screenSize.y;
    }

    @Override
    protected void onDestroy() {
        if (tts!=null) {
            tts.stop();
            tts.shutdown();
        }
        super.onDestroy();
    }

    public void speak(String message) {
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

    @Override
    public void onEvent(String topic, Object o) {
        View view = null;
        if (o instanceof View) {
            view = (View) o;
        }
        switch(topic) {
            case TOPIC_START_COLORING:
                startColoringGame(view);
                break;
            case TOPIC_START_DRESSUP:
                startHeritageDressUpGame(view);
                break;
            case TOPIC_START_MATCH:
                startMatchGame(view);
                break;
            case TOPIC_START_PUZZLE:
                startPuzzleGame(view);
                break;
            case TOPIC_START_SCRATCH:
                startScratchGame(view);
                break;
            case TOPIC_START_TREE:
                startTreeGame(view);
                break;
        }
    }

    public void startMatchGame(View view) {
        Intent intent = new Intent( this, MatchGameActivity.class );
        intent.putExtra(ChooseFamilyMember.FAMILY, people);
        intent.putExtra(ChooseFamilyMember.SELECTED_PERSON, selectedPerson);
        startActivity(intent);
    }

    public void startScratchGame(View view) {
        Intent intent = new Intent( this, ScratchGameActivity.class );
        intent.putExtra(ChooseFamilyMember.FAMILY, people);
        intent.putExtra(ChooseFamilyMember.SELECTED_PERSON, selectedPerson);
        startActivity(intent);
    }

    public void startColoringGame(View view) {
        Intent intent = new Intent( this, ColoringGameActivity.class );
        intent.putExtra(ChooseFamilyMember.FAMILY, people);
        intent.putExtra(ChooseFamilyMember.SELECTED_PERSON, selectedPerson);
        startActivity(intent);
    }

    public void startHeritageDressUpGame(View view) {
        Intent intent = new Intent( this, ChooseCultureActivity.class );
        intent.putExtra(ChooseFamilyMember.SELECTED_PERSON, selectedPerson);
        startActivity(intent);
    }

    public void startPuzzleGame(View view) {
        Intent intent = new Intent( this, PuzzleGameActivity.class );
        intent.putExtra(ChooseFamilyMember.FAMILY, people);
        intent.putExtra(ChooseFamilyMember.SELECTED_PERSON, selectedPerson);
        startActivity(intent);
    }

    public void startTreeGame(View view) {
        Intent intent = new Intent( this, MyTreeActivity.class );
        intent.putExtra(ChooseFamilyMember.SELECTED_PERSON, selectedPerson);
        startActivity(intent);
    }
}
