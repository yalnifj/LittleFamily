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
import android.speech.tts.UtteranceProgressListener;
import android.speech.tts.Voice;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.widget.Toast;

import com.google.firebase.analytics.FirebaseAnalytics;
import com.yellowforktech.littlefamilytree.R;
import com.yellowforktech.littlefamilytree.activities.tasks.WaitTask;
import com.yellowforktech.littlefamilytree.data.DataNetworkState;
import com.yellowforktech.littlefamilytree.data.DataNetworkStateListener;
import com.yellowforktech.littlefamilytree.data.DataService;
import com.yellowforktech.littlefamilytree.data.LittlePerson;
import com.yellowforktech.littlefamilytree.db.FireHelper;
import com.yellowforktech.littlefamilytree.db.Sale;
import com.yellowforktech.littlefamilytree.events.EventListener;
import com.yellowforktech.littlefamilytree.events.EventQueue;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
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
    public static final String TOPIC_START_BIRTHDAY_CARD = "startBirthdayCard";
	public static final String TOPIC_START_PROFILE = "startProfile";
    public static final String PROP_HAS_PREMIUM = "propHasPremium";
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
    protected Boolean hasPremium = null;
    protected PremiumDialog premiumDialog;
    protected Boolean launchingGame = false;

    public LittlePerson getSelectedPerson() {
        return selectedPerson;
    }

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

        Bundle logBundle = new Bundle();
        logBundle.putString(FirebaseAnalytics.Param.ITEM_NAME, getClass().getSimpleName());
        FirebaseAnalytics.getInstance(this).logEvent(FirebaseAnalytics.Event.VIEW_ITEM, logBundle);
    }

    /**
     * Dispatch onResume() to fragments.  Note that for better inter-operation
     * with older versions of the platform, at the point of this call the
     * fragments attached to the activity are <em>not</em> resumed.  This means
     * that in some cases the previous state may still be saved, not allowing
     * fragment transactions that modify the state.  To correctly interact
     * with fragments in their proper state, you should instead override
     * {@link #onResumeFragments()}.
     */
    @Override
    protected void onResume() {
        super.onResume();
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
        EventQueue.getInstance().subscribe(TOPIC_START_BIRTHDAY_CARD, this);
        EventQueue.getInstance().subscribe(TOPIC_START_PROFILE, this);
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
    }

    /**
     * Dispatch onPause() to fragments.
     */
    @Override
    protected void onPause() {
        super.onPause();
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
        EventQueue.getInstance().unSubscribe(TOPIC_START_BIRTHDAY_CARD, this);
        EventQueue.getInstance().unSubscribe(TOPIC_START_PROFILE, this);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode==200) {
            launchingGame = false;
        }
    }

    public void showLoadingDialog() {
        hideDialog = false;
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

    public boolean hideDialog = true;
    public void hideLoadingDialog() {
        hideDialog = true;
        WaitTask waiter = new WaitTask(new WaitTask.WaitTaskListener() {
            @Override
            public void onProgressUpdate(Integer progress) {
            }

            @Override
            public void onComplete(Integer progress) {
                if (hideDialog) {
                    try {
                        synchronized (dialogShown) {
                            if (loadingDialog != null && loadingDialog.isVisible()) {
                                loadingDialog.dismissAllowingStateLoss();
                            }
                            dialogShown = false;
                            loadingDialog = null;
                        }
                    } catch (Exception e) {
                        Log.e("LittleFamilyActivity", "Error hiding loading dialog", e);
                    }
                }
            }
        });
        waiter.execute(1000L);
    }

    public void showAdultAuthDialog(AdultsAuthDialog.AuthCompleteAction action) {
        boolean skipAuth = false;
        try {
            String remember = DataService.getInstance().getDBHelper().getProperty(DataService.PROPERY_REMEMBER_ME);
            if (remember!=null) {
                Long time = Long.valueOf(remember);
                Date now = new Date();
                if (now.getTime() - time < 1000 * 60 * 20) {
                    skipAuth = true;
                }
            }
        } catch (Exception e) {
            Log.e("LittleFamilyActivity", "Error getting property", e);
        }
        if (!skipAuth) {
            String text = getResources().getString(R.string.ask_for_help);
            speak(text);
            adultAuthDialog = new AdultsAuthDialog();
            adultAuthDialog.setAction(action);
            adultAuthDialog.setStyle(DialogFragment.STYLE_NO_TITLE, R.style.Theme_AppCompat_Light_Dialog);
            adultAuthDialog.show(getFragmentManager(), "Authenticate");
        } else {
            action.doAction(true);
        }
    }

    public void hideAdultAuthDialog() {
        if (adultAuthDialog != null && adultAuthDialog.isVisible()) {
            adultAuthDialog.dismissAllowingStateLoss();
        }
    }

    public void showBuyTryDialog(final int tries, final PremiumDialog.ActionListener listener) {
        final PremiumDialog.ActionListener theListener = listener;
        FireHelper.getInstance().isOnSale(new FireHelper.SaleListener() {
            @Override
            public void onSale(Sale sale) {
                try {
                    premiumDialog = new PremiumDialog();
                    premiumDialog.setTries(tries);
                    premiumDialog.setStyle(DialogFragment.STYLE_NO_TITLE, R.style.Theme_AppCompat_Light_Dialog);
                    premiumDialog.setListener(theListener);
                    if (sale!=null) {
                        premiumDialog.setOnSale(true);
                        premiumDialog.setSalePrice(sale.getPrice());
                        premiumDialog.setSaleText(sale.getSalesText());
                    }
                    premiumDialog.setCancelable(false);
                    premiumDialog.show(getFragmentManager(), "Authenticate");
                } catch (Exception e) {
                    Log.e("LittleFamilyActivity", "Error Showing buy dialog", e);
                }
            }
        });
    }

    public void hideBuyTryDialog() {
        if (premiumDialog != null && premiumDialog.isVisible()) {
            premiumDialog.dismissAllowingStateLoss();
        }
    }

    @Override
    public void onInit(int code) {
        if (code == TextToSpeech.SUCCESS) {
            if (tts.isLanguageAvailable(Locale.getDefault())==TextToSpeech.LANG_AVAILABLE) {
                tts.setLanguage(Locale.getDefault());
            } else {
                tts.setLanguage(Locale.ENGLISH);
            }
            tts.setSpeechRate(1.1f);
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

    public void speak(final String message) {
        Log.d("LittleFamilyActivity", "Speaking: " + message);
        Boolean quietMode = PreferenceManager.getDefaultSharedPreferences(this).getBoolean("quiet_mode", false);
        if (tts!=null && !quietMode) {
            if (Build.VERSION.SDK_INT > 20) {
                tts.speak(message, TextToSpeech.QUEUE_FLUSH, null, null);
                tts.setOnUtteranceProgressListener(new UtteranceProgressListener() {
                    @Override
                    public void onStart(String utteranceId) {}

                    @Override
                    public void onDone(String utteranceId) {}

                    @Override
                    public void onError(String utteranceId) {}
                });
            }
            else {
                tts.speak(message, TextToSpeech.QUEUE_FLUSH, null);
            }
        } else {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(LittleFamilyActivity.this, message, Toast.LENGTH_LONG).show();
                }
            });
        }
    }

    public void speak(final String message, final UtteranceProgressListener listener) {
        Log.d("LittleFamilyActivity", "Speaking: " + message);
        Boolean quietMode = PreferenceManager.getDefaultSharedPreferences(this).getBoolean("quiet_mode", false);
        if (tts!=null && !quietMode) {
            if (Build.VERSION.SDK_INT > 20) {
                tts.setOnUtteranceProgressListener(listener);
                tts.speak(message, TextToSpeech.QUEUE_FLUSH, null, message);
            }
            else {
                tts.setOnUtteranceProgressListener(listener);
                HashMap<String, String> map = new HashMap<String, String>();
                map.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, message);
                tts.speak(message, TextToSpeech.QUEUE_FLUSH, map);
            }
        } else {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(LittleFamilyActivity.this, message, Toast.LENGTH_SHORT).show();
                    listener.onDone(message);
                }
            });
        }
    }

    public void sayGivenNameForPerson(final LittlePerson person) {
        Boolean quietMode = PreferenceManager.getDefaultSharedPreferences(this).getBoolean("quiet_mode", false);
        if (!quietMode && person.getGivenNameAudioPath()!=null) {
            //-- run name in a new thread to not lose it
            Runnable runnable = new Runnable() {
                @Override
                public void run() {
                    final MediaPlayer mPlayer = new MediaPlayer();
                    try {
                        mPlayer.setDataSource(person.getGivenNameAudioPath());
                        mPlayer.setVolume(3.0f, 3.0f);
                        mPlayer.prepare();
                        mPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                            @Override
                            public void onCompletion(MediaPlayer mp) {
                                mPlayer.release();
                            }
                        });
                        mPlayer.start();
                    } catch (IOException e) {
                        Log.e("LittleFamilyScene", "prepare() failed", e);
                    }
                }
            };
            Thread thread = new Thread(runnable);
            thread.start();
        } else {
            if (person.getGivenName()!=null) {
                speak(person.getGivenName());
            }
        }
    }

    public void playCompleteSound() {
        Boolean quietMode = PreferenceManager.getDefaultSharedPreferences(this).getBoolean("quiet_mode", false);
        if (successPlayer !=null && !quietMode) successPlayer.start();
    }

    public void playBuzzSound() {
        Boolean quietMode = PreferenceManager.getDefaultSharedPreferences(this).getBoolean("quiet_mode", false);
        if (buzzPlayer !=null && !quietMode) buzzPlayer.start();
    }

    public void userHasPremium(final FireHelper.PremiumListener listener) {
        listener.results(true);
        /*
        try {
            if (hasPremium!=null) {
                listener.results(hasPremium);
                return;
            }
            String premStr = DataService.getInstance().getDBHelper().getProperty(PROP_HAS_PREMIUM);
            if ("true".equals(premStr)) {
                hasPremium = true;
                listener.results(hasPremium);
                return;
            }
            String username = DataService.getInstance().getDBHelper().getProperty(DataService.SERVICE_USERNAME);
            String serviceType = PreferenceManager.getDefaultSharedPreferences(this).getString(DataService.SERVICE_TYPE, null);
            FireHelper.getInstance().userIsPremium(username, serviceType, new FireHelper.PremiumListener() {
                @Override
                public void results(boolean premium) {
                    hasPremium = premium;
                    if (premium) {
                        try {
                            DataService.getInstance().getDBHelper().saveProperty(PROP_HAS_PREMIUM, "true");
                        } catch (Exception e) {
                            Log.e("LittleFamilyActivity", "Error getting property", e);
                        }
                    }
                    listener.results(hasPremium);
                }
            });

        } catch (Exception e) {
            Log.e("LittleFamilyActivity", "Error getting property", e);
        }
        */
    }

    public int getTries(String gameCode) {
        int tries = 3;
        try {
            String triesStr = DataService.getInstance().getDBHelper().getProperty(gameCode);
            if (triesStr != null) {
                tries = Integer.parseInt(triesStr);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return tries;
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
				Intent intent = new Intent(LittleFamilyActivity.this, NewSettingsActivity.class);
                intent.putExtra(ChooseFamilyMember.SELECTED_PERSON, selectedPerson);
				startActivity(intent);
			} else {
				Toast.makeText(LittleFamilyActivity.this, "Unable to verify password", Toast.LENGTH_LONG).show();
			}
			hideAdultAuthDialog();
		}

        public void onClose() {
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
            case TOPIC_START_BIRTHDAY_CARD:
                startBirthdayCardGame(person);
                break;
			case TOPIC_START_PROFILE:
				onProfileButtonPressed(null);
				break;
        }
    }

    public void startMatchGame(LittlePerson person) {
        if (!launchingGame) {
            launchingGame = true;
            Intent intent = new Intent(this, MatchGameActivity.class);
            if (person != selectedPerson) {
                ArrayList<LittlePerson> people = new ArrayList<>();
                people.add(person);
                intent.putExtra(ChooseFamilyMember.FAMILY, people);
            } else {
                intent.putExtra(ChooseFamilyMember.FAMILY, people);
            }
            intent.putExtra(ChooseFamilyMember.SELECTED_PERSON, person);
            startActivityForResult(intent, 200);
        }
    }

    public void startScratchGame(LittlePerson person) {
        if (!launchingGame) {
            launchingGame = true;
            Intent intent = new Intent(this, ScratchGameActivity.class);
            if (person != selectedPerson) {
                ArrayList<LittlePerson> people = new ArrayList<>();
                people.add(person);
                intent.putExtra(ChooseFamilyMember.FAMILY, people);
            } else {
                intent.putExtra(ChooseFamilyMember.FAMILY, people);
            }
            intent.putExtra(ChooseFamilyMember.SELECTED_PERSON, person);
            startActivityForResult(intent, 200);
        }
    }

    public void startColoringGame(LittlePerson person) {
        if (!launchingGame) {
            launchingGame = true;
            Intent intent = new Intent(this, ColoringGameActivity.class);
            if (person != selectedPerson) {
                ArrayList<LittlePerson> people = new ArrayList<>();
                people.add(person);
                intent.putExtra(ChooseFamilyMember.FAMILY, people);
            } else {
                intent.putExtra(ChooseFamilyMember.FAMILY, people);
            }
            intent.putExtra(ChooseFamilyMember.SELECTED_PERSON, person);
            startActivityForResult(intent, 200);
        }
    }

    public void startHeritageCalc(LittlePerson person) {
        if (!launchingGame) {
            launchingGame = true;
            Intent intent = new Intent(this, ChooseCultureActivity.class);
            intent.putExtra(ChooseFamilyMember.SELECTED_PERSON, person);
            startActivityForResult(intent, 200);
        }
    }

    public void startHeritageDressUpGame(LittlePerson person) {
        Intent intent = new Intent( this, HeritageDressUpActivity.class );
        intent.putExtra(ChooseFamilyMember.SELECTED_PERSON, person);
        startActivity(intent);
    }

    public void startPuzzleGame(LittlePerson person) {
        if (!launchingGame) {
            launchingGame = true;
            Intent intent = new Intent(this, PuzzleGameActivity.class);
            if (person != selectedPerson) {
                ArrayList<LittlePerson> people = new ArrayList<>();
                people.add(person);
                intent.putExtra(ChooseFamilyMember.FAMILY, people);
            } else {
                intent.putExtra(ChooseFamilyMember.FAMILY, people);
            }
            intent.putExtra(ChooseFamilyMember.SELECTED_PERSON, person);
            startActivityForResult(intent, 200);
        }
    }

    public void startTreeGame(LittlePerson person) {
        if (!launchingGame) {
            launchingGame = true;
            Intent intent = new Intent(this, MyTreeActivity.class);
            intent.putExtra(ChooseFamilyMember.SELECTED_PERSON, person);
            startActivityForResult(intent, 200);
        }
    }

    public void startBubbleGame(LittlePerson person) {
        if (!launchingGame) {
            launchingGame = true;
            Intent intent = new Intent(this, BubblePopActivity.class);
            intent.putExtra(ChooseFamilyMember.SELECTED_PERSON, person);
            startActivityForResult(intent, 200);
        }
    }
	
	public void startSongGame(LittlePerson person) {
        if (!launchingGame) {
            launchingGame = true;
            Intent intent = new Intent(this, SongActivity.class);
            intent.putExtra(ChooseFamilyMember.SELECTED_PERSON, person);
            startActivityForResult(intent, 200);
        }
    }

    public void startFlyingGame(LittlePerson person) {
        if (!launchingGame) {
            launchingGame = true;
            Intent intent = new Intent(this, FlyingActivity.class);
            intent.putExtra(ChooseFamilyMember.SELECTED_PERSON, person);
            startActivityForResult(intent, 200);
        }
    }

    public void startBirthdayCardGame(LittlePerson person) {
        if (!launchingGame) {
            launchingGame = true;
            Intent intent = new Intent(this, BirthdayCardActivity.class);
            intent.putExtra(ChooseFamilyMember.SELECTED_PERSON, selectedPerson);
            if (person != null && !person.equals(selectedPerson)) {
                intent.putExtra(BirthdayCardActivity.BIRTHDAY_PERSON, person);
            }
            startActivityForResult(intent, 200);
        }
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (key.equals("tts_voice")) {
            setVoiceFromPreferences();
        }
    }
}
