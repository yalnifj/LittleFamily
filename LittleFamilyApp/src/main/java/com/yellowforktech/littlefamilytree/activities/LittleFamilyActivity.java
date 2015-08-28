package com.yellowforktech.littlefamilytree.activities;

import android.app.DialogFragment;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Point;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.speech.tts.TextToSpeech;
import android.speech.tts.Voice;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.widget.Toast;

import com.yellowforktech.littlefamilytree.R;
import com.yellowforktech.littlefamilytree.data.DataNetworkState;
import com.yellowforktech.littlefamilytree.data.DataNetworkStateListener;
import com.yellowforktech.littlefamilytree.data.LittlePerson;
import com.yellowforktech.littlefamilytree.events.EventListener;
import com.yellowforktech.littlefamilytree.events.EventQueue;

import java.util.ArrayList;
import java.util.Locale;
import java.util.Set;

/**
 * Created by kids on 4/18/15.
 */
public class LittleFamilyActivity extends FragmentActivity implements TextToSpeech.OnInitListener, TopBarFragment.OnFragmentInteractionListener,
        EventListener, DataNetworkStateListener, SharedPreferences.OnSharedPreferenceChangeListener {
    public static final String TOPIC_START_MATCH    = "startMatch";
    public static final String TOPIC_START_SCRATCH  = "startScratch";
    public static final String TOPIC_START_COLORING = "startColoring";
    public static final String TOPIC_START_DRESSUP  = "startDressUp";
    public static final String TOPIC_START_PUZZLE   = "startPuzzle";
    public static final String TOPIC_START_TREE = "startTree";
    public static final String TOPIC_START_HERITAGE_CALC = "startHeritageCalc";
    public static final String TOPIC_START_BUBBLES = "startBubblePop";
    public static final String TOPIC_START_SETTINGS = "startSettings";
	public static final String TOPIC_START_SONG = "startSong";
    public static final String TOPIC_START_FLYING = "startFlying";
    protected TextToSpeech tts;
    protected MediaPlayer successPlayer;
    protected MediaPlayer buzzPlayer;
    protected TopBarFragment topBar;
    protected LittlePerson selectedPerson;
    protected LoadingDialog loadingDialog;
    protected ArrayList<LittlePerson> people;
    protected Boolean dialogShown = false;
    protected AdultsAuthDialog adultAuthDialog;
	protected SettingsAction settingsAction = new SettingsAction();

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
                if (selectedPerson != null) {
                    topBar.getArguments().putSerializable(TopBarFragment.ARG_PERSON, selectedPerson);
                }
            }
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
		System.gc();
        successPlayer = MediaPlayer.create(this, R.raw.powerup_success);
        buzzPlayer = MediaPlayer.create(this, R.raw.beepboop);
        EventQueue.getInstance().subscribe(TOPIC_START_MATCH, this);
        EventQueue.getInstance().subscribe(TOPIC_START_COLORING, this);
        EventQueue.getInstance().subscribe(TOPIC_START_DRESSUP, this);
        EventQueue.getInstance().subscribe(TOPIC_START_HERITAGE_CALC, this);
        EventQueue.getInstance().subscribe(TOPIC_START_PUZZLE, this);
        EventQueue.getInstance().subscribe(TOPIC_START_SCRATCH, this);
        EventQueue.getInstance().subscribe(TOPIC_START_TREE, this);
        EventQueue.getInstance().subscribe(TOPIC_START_BUBBLES, this);
        EventQueue.getInstance().subscribe(TOPIC_START_SETTINGS, this);
        EventQueue.getInstance().subscribe(TOPIC_START_SONG, this);
        EventQueue.getInstance().subscribe(TOPIC_START_FLYING, this);
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (successPlayer!=null) {
            successPlayer.release();
            successPlayer = null;
        }
        if (buzzPlayer!=null) {
            buzzPlayer.release();
            buzzPlayer = null;
        }
        EventQueue.getInstance().unSubscribe(TOPIC_START_MATCH, this);
        EventQueue.getInstance().unSubscribe(TOPIC_START_COLORING, this);
        EventQueue.getInstance().unSubscribe(TOPIC_START_DRESSUP, this);
        EventQueue.getInstance().unSubscribe(TOPIC_START_HERITAGE_CALC, this);
        EventQueue.getInstance().unSubscribe(TOPIC_START_PUZZLE, this);
        EventQueue.getInstance().unSubscribe(TOPIC_START_SCRATCH, this);
        EventQueue.getInstance().unSubscribe(TOPIC_START_TREE, this);
        EventQueue.getInstance().unSubscribe(TOPIC_START_BUBBLES, this);
        EventQueue.getInstance().unSubscribe(TOPIC_START_SETTINGS, this);
        EventQueue.getInstance().unSubscribe(TOPIC_START_SONG, this);
        EventQueue.getInstance().unSubscribe(TOPIC_START_FLYING, this);
    }

    public void showLoadingDialog() {
        try {
            synchronized (dialogShown) {
                if (!dialogShown) {
                    loadingDialog = new LoadingDialog();
                    loadingDialog.setStyle(DialogFragment.STYLE_NO_TITLE, R.style.Theme_AppCompat_Dialog);
                    loadingDialog.show(getFragmentManager(), "Loading");
                    dialogShown = true;
                }
            }
        } catch(Exception e){
            Log.e("LittleFamilyActivity", "Error showing loading dialog", e);
        }
    }

    public void hideLoadingDialog() {
        try {
            synchronized (dialogShown) {
                if (loadingDialog != null && loadingDialog.isVisible()) {
                    loadingDialog.dismissAllowingStateLoss();
                }
                dialogShown = false;
                loadingDialog = null;
            }
        } catch(Exception e) {
            Log.e("LittleFamilyActivity", "Error hiding loading dialog", e);
        }
    }

    public void showAdultAuthDialog(AdultsAuthDialog.AuthCompleteAction action) {
        String text = getResources().getString(R.string.ask_for_help);
        speak(text);
        adultAuthDialog = new AdultsAuthDialog();
        adultAuthDialog.setAction(action);
        adultAuthDialog.setStyle(DialogFragment.STYLE_NO_TITLE, R.style.Theme_AppCompat_Light_Dialog);
        adultAuthDialog.show(getFragmentManager(), "Authenticate");
    }

    public void hideAdultAuthDialog() {
        if (adultAuthDialog != null && adultAuthDialog.isVisible()) {
            adultAuthDialog.dismissAllowingStateLoss();
        }
    }

    @Override
    public void onInit(int code) {
        if (code == TextToSpeech.SUCCESS) {
            tts.setLanguage(Locale.getDefault());
            tts.setSpeechRate(0.9f);
            setVoiceFromPreferences();
        } else {
            tts = null;
            //Toast.makeText(this, "Failed to initialize TTS engine.", Toast.LENGTH_SHORT).show();
            Log.e("LittleFamilyActivity", "Error intializing speech");
        }
    }

    private void setVoiceFromPreferences() {
        if (Build.VERSION.SDK_INT > 20) {
            String voiceName = PreferenceManager.getDefaultSharedPreferences(this).getString("tts_voice", "");
            if (!voiceName.isEmpty()) {
                Voice voice = null;
                Set<Voice> voices = tts.getVoices();
                for (Voice v : voices) {
                    if (v.getName().equals(voiceName)) {
                        voice = v;
                    }
                }
                if (voice!=null) {
                    tts.setVoice(voice);
                }
            }

            PreferenceManager.getDefaultSharedPreferences(this).registerOnSharedPreferenceChangeListener(this);
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
            PreferenceManager.getDefaultSharedPreferences(this).unregisterOnSharedPreferenceChangeListener(this);
            tts.stop();
            tts.shutdown();
        }
        super.onDestroy();
    }

    @Override
    public void remoteStateChanged(DataNetworkState state) {
        if (state==DataNetworkState.REMOTE_STARTING) {
            showLoadingDialog();
        }
        if (state==DataNetworkState.REMOTE_FINISHED) {
            hideLoadingDialog();
        }
    }

    @Override
    public void statusUpdate(String status) {

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
        if (successPlayer !=null) successPlayer.start();
    }

    public void playBuzzSound() {
        if (buzzPlayer !=null) buzzPlayer.start();
    }

    public void onHomeButtonPressed(View view) {
        finish();
    }

    public void onProfileButtonPressed(View view) {
        Intent intent = new Intent(this, ChooseFamilyMember.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish(); // call this to finish the current activity
    }

    public void onSettingsButtonPressed(View view) {
        showAdultAuthDialog(settingsAction);
    }
	
	public class SettingsAction implements AdultsAuthDialog.AuthCompleteAction {
		public void doAction(boolean success) {
			if (success) {
				Intent intent = new Intent(LittleFamilyActivity.this, SettingsActivity.class);
				startActivity(intent);
			} else {
				Toast.makeText(LittleFamilyActivity.this, "Unable to verify password", Toast.LENGTH_LONG).show();
			}
			hideAdultAuthDialog();
		}
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
            case TOPIC_START_SETTINGS:
                showAdultAuthDialog(settingsAction);
                break;
			case TOPIC_START_SONG:
                startSongGame(person);
                break;
            case TOPIC_START_FLYING:
                startFlyingGame(person);
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
	
	public void startSongGame(LittlePerson person) {
        Intent intent = new Intent( this, SongActivity.class );
        intent.putExtra(ChooseFamilyMember.SELECTED_PERSON, person);
        startActivity(intent);
    }

    public void startFlyingGame(LittlePerson person) {
        Intent intent = new Intent( this, FlyingActivity.class );
        intent.putExtra(ChooseFamilyMember.SELECTED_PERSON, person);
        startActivity(intent);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (key.equals("tts_voice")) {
            setVoiceFromPreferences();
        }
    }
}
