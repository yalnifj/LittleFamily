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
    public static final String TOPIC_START_HERITAGE_CALC = "startHeritageCalc";
    public static final String TOPIC_START_BUBBLES = "startBubblePop";
    protected TextToSpeech tts;
    protected MediaPlayer mediaPlayer;
    protected TopBarFragment topBar;
    protected LittlePerson selectedPerson;
    protected LoadingDialog loadingDialog;
    protected ArrayList<LittlePerson> people;
    protected Boolean dialogShown = false;

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
        EventQueue.getInstance().subscribe(TOPIC_START_HERITAGE_CALC, this);
        EventQueue.getInstance().subscribe(TOPIC_START_PUZZLE, this);
        EventQueue.getInstance().subscribe(TOPIC_START_SCRATCH, this);
        EventQueue.getInstance().subscribe(TOPIC_START_TREE, this);
        EventQueue.getInstance().subscribe(TOPIC_START_BUBBLES, this);
    }

    @Override
    protected void onStop() {
        super.onStop();
        mediaPlayer.release();
        mediaPlayer = null;
        EventQueue.getInstance().unSubscribe(TOPIC_START_MATCH, this);
        EventQueue.getInstance().unSubscribe(TOPIC_START_COLORING, this);
        EventQueue.getInstance().unSubscribe(TOPIC_START_DRESSUP, this);
        EventQueue.getInstance().unSubscribe(TOPIC_START_HERITAGE_CALC, this);
        EventQueue.getInstance().unSubscribe(TOPIC_START_PUZZLE, this);
        EventQueue.getInstance().unSubscribe(TOPIC_START_SCRATCH, this);
        EventQueue.getInstance().unSubscribe(TOPIC_START_TREE, this);
        EventQueue.getInstance().unSubscribe(TOPIC_START_BUBBLES, this);
    }

    public void showLoadingDialog() {
        synchronized (dialogShown) {
            if (!dialogShown) {
                loadingDialog = new LoadingDialog();
                loadingDialog.setStyle(DialogFragment.STYLE_NO_TITLE, R.style.Theme_AppCompat_Dialog);
                loadingDialog.show(getFragmentManager(), "Loading");
                dialogShown = true;
            }
        }
    }

    public void hideLoadingDialog() {
        synchronized (dialogShown) {
            if (loadingDialog != null && loadingDialog.isVisible()) {
                loadingDialog.dismissAllowingStateLoss();
            }
            dialogShown = false;
            loadingDialog = null;
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
        LittlePerson person = selectedPerson;
        if (o instanceof LittlePerson) {
            person = (LittlePerson) o;
        }
        switch(topic) {
            case TOPIC_START_COLORING:
                startColoringGame(person);
                break;
            case TOPIC_START_DRESSUP:
                startHeritageDressUpGame(person);
                break;
            case TOPIC_START_HERITAGE_CALC:
                startHeritageCalc(person);
                break;
            case TOPIC_START_MATCH:
                startMatchGame(person);
                break;
            case TOPIC_START_PUZZLE:
                startPuzzleGame(person);
                break;
            case TOPIC_START_SCRATCH:
                startScratchGame(person);
                break;
            case TOPIC_START_TREE:
                startTreeGame(person);
                break;
            case TOPIC_START_BUBBLES:
                startBubbleGame(person);
                break;
        }
    }

    public void startMatchGame(LittlePerson person) {
        Intent intent = new Intent( this, MatchGameActivity.class );
        if (person!=selectedPerson) {
            ArrayList<LittlePerson> people = new ArrayList<>();
            people.add(person);
            intent.putExtra(ChooseFamilyMember.FAMILY, people);
        } else {
            intent.putExtra(ChooseFamilyMember.FAMILY, people);
        }
        intent.putExtra(ChooseFamilyMember.SELECTED_PERSON, person);
        startActivity(intent);
    }

    public void startScratchGame(LittlePerson person) {
        Intent intent = new Intent( this, ScratchGameActivity.class );
        if (person!=selectedPerson) {
            ArrayList<LittlePerson> people = new ArrayList<>();
            people.add(person);
            intent.putExtra(ChooseFamilyMember.FAMILY, people);
        } else {
            intent.putExtra(ChooseFamilyMember.FAMILY, people);
        }
        intent.putExtra(ChooseFamilyMember.SELECTED_PERSON, person);
        startActivity(intent);
    }

    public void startColoringGame(LittlePerson person) {
        Intent intent = new Intent( this, ColoringGameActivity.class );
        if (person!=selectedPerson) {
            ArrayList<LittlePerson> people = new ArrayList<>();
            people.add(person);
            intent.putExtra(ChooseFamilyMember.FAMILY, people);
        } else {
            intent.putExtra(ChooseFamilyMember.FAMILY, people);
        }
        intent.putExtra(ChooseFamilyMember.SELECTED_PERSON, person);
        startActivity(intent);
    }

    public void startHeritageCalc(LittlePerson person) {
        Intent intent = new Intent( this, ChooseCultureActivity.class );
        intent.putExtra(ChooseFamilyMember.SELECTED_PERSON, person);
        startActivity(intent);
    }

    public void startHeritageDressUpGame(LittlePerson person) {
        Intent intent = new Intent( this, HeritageDressUpActivity.class );
        intent.putExtra(ChooseFamilyMember.SELECTED_PERSON, person);
        startActivity(intent);
    }

    public void startPuzzleGame(LittlePerson person) {
        Intent intent = new Intent( this, PuzzleGameActivity.class );
        if (person!=selectedPerson) {
            ArrayList<LittlePerson> people = new ArrayList<>();
            people.add(person);
            intent.putExtra(ChooseFamilyMember.FAMILY, people);
        } else {
            intent.putExtra(ChooseFamilyMember.FAMILY, people);
        }
        intent.putExtra(ChooseFamilyMember.SELECTED_PERSON, person);
        startActivity(intent);
    }

    public void startTreeGame(LittlePerson person) {
        Intent intent = new Intent( this, MyTreeActivity.class );
        intent.putExtra(ChooseFamilyMember.SELECTED_PERSON, person);
        startActivity(intent);
    }

    public void startBubbleGame(LittlePerson person) {
        Intent intent = new Intent( this, BubblePopActivity.class );
        intent.putExtra(ChooseFamilyMember.SELECTED_PERSON, person);
        startActivity(intent);
    }
}
