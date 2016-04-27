package com.yellowforktech.littlefamilytree.activities;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.PixelFormat;
import android.net.Uri;
import android.os.Bundle;
import android.speech.tts.UtteranceProgressListener;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.View;
import android.widget.Toast;

import com.yellowforktech.littlefamilytree.R;
import com.yellowforktech.littlefamilytree.data.DataService;
import com.yellowforktech.littlefamilytree.data.LittlePerson;
import com.yellowforktech.littlefamilytree.events.EventQueue;
import com.yellowforktech.littlefamilytree.sprites.CupcakeSprite;
import com.yellowforktech.littlefamilytree.sprites.TouchEventGameSprite;
import com.yellowforktech.littlefamilytree.views.BirthdayCardSurfaceView;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class BirthdayCardActivity extends LittleFamilyActivity {
    public static final String TOPIC_PERSON_TOUCHED = "personTouched";
    public static final String TOPIC_BIRTHDAY_PERSON_SELECTED = "birthdayPersonSelected";
    public static final String TOPIC_CARD_SELECTED = "cardSelected";
    public static final String BIRTHDAY_PERSON = "birthdayPerson";

    private BirthdayCardSurfaceView view;
    private List<LittlePerson> people;
    private LittlePerson birthdayPerson;

    private ShareAction shareAction = new ShareAction();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_birthday_card);

        view = (BirthdayCardSurfaceView) findViewById(R.id.view);
        view.setActivity(this);
        view.setZOrderOnTop(true);    // necessary
        SurfaceHolder sfhTrackHolder = view.getHolder();
        sfhTrackHolder.setFormat(PixelFormat.TRANSPARENT);

        Intent intent = getIntent();
        selectedPerson = (LittlePerson) intent.getSerializableExtra(ChooseFamilyMember.SELECTED_PERSON);

        birthdayPerson = (LittlePerson) intent.getSerializableExtra(BirthdayCardActivity.BIRTHDAY_PERSON);

        setupTopBar();
    }

    @Override
    protected void onStart() {
        super.onStart();

        DataService.getInstance().registerNetworkStateListener(this);
        try {
            people = DataService.getInstance().getDBHelper().getNextBirthdays(15, 4);
            if (people.size()==0) {
                Log.d("BirthdayCardActivity", "Unable to find birthdays from database");
                people.add(selectedPerson);
            } else {
                DataService.getInstance().addToSyncQ(people, 5);
                Comparator<LittlePerson> comp = new Comparator<LittlePerson>() {
                    @Override
                    public int compare(LittlePerson lhs, LittlePerson rhs) {
                        if (lhs.getBirthDate()!=null && rhs.getBirthDate()!=null) {
                            Calendar lcal = Calendar.getInstance();
                            Calendar rcal = Calendar.getInstance();
                            lcal.setTime(lhs.getBirthDate());
                            rcal.setTime(rhs.getBirthDate());
                            if (lcal.get(Calendar.MONTH) != rcal.get(Calendar.MONTH)) {
                                return lcal.get(Calendar.MONTH) - rcal.get(Calendar.MONTH);
                            }
                            return lcal.get(Calendar.DAY_OF_MONTH) - rcal.get(Calendar.DAY_OF_MONTH);
                        }
                        return 0;
                    }
                };
                Collections.sort(people, comp);
            }
        } catch (Exception e) {
            Log.e("BirthdayCardActivity", "Error getting birthday people", e);
            people = new ArrayList<>();
            people.add(selectedPerson);
        }
        view.setBirthdayPeople(people);
        if (birthdayPerson!=null) {
            view.setBirthdayPerson(birthdayPerson);
            speak("Choose a card to decorate.");
        }

        EventQueue.getInstance().subscribe(TOPIC_BIRTHDAY_PERSON_SELECTED, this);
        EventQueue.getInstance().subscribe(TOPIC_PERSON_TOUCHED, this);
        EventQueue.getInstance().subscribe(TOPIC_CARD_SELECTED, this);
    }

    @Override
    protected void onStop() {
        super.onStop();
        DataService.getInstance().unregisterNetworkStateListener(this);
        EventQueue.getInstance().unSubscribe(TOPIC_BIRTHDAY_PERSON_SELECTED, this);
        EventQueue.getInstance().unSubscribe(TOPIC_PERSON_TOUCHED, this);
        EventQueue.getInstance().unSubscribe(TOPIC_CARD_SELECTED, this);
    }

    @Override
    public void setupTopBar() {
        if (findViewById(R.id.topBarFragment)!=null) {
            topBar = (TopBarFragment) getSupportFragmentManager().findFragmentById(R.id.topBarFragment);
            if (topBar == null) {
                topBar = TopBarFragment.newInstance(selectedPerson, R.layout.fragment_top_bar_card);
                getSupportFragmentManager().beginTransaction().replace(R.id.topBarFragment, topBar).commit();
            } else {
                if (selectedPerson != null) {
                    topBar.getArguments().putSerializable(TopBarFragment.ARG_PERSON, selectedPerson);
                }
            }
        }
    }

    @Override
    public void onInit(int code) {
        super.onInit(code);
        if (birthdayPerson==null) {
            speak("Look who has birthdays coming up.  Choose someone to start.");
        }
    }

    @Override
    public void onEvent(String topic, Object o) {
        super.onEvent(topic, o);
        if (topic.equals(TOPIC_BIRTHDAY_PERSON_SELECTED)) {
            if (o instanceof CupcakeSprite) {
                LittlePerson person = ((CupcakeSprite) o).getPerson();
                view.setBirthdayPerson(person);
                speak("Choose a card to decorate.");
            }
        } else if (topic.equals(TOPIC_PERSON_TOUCHED)) {
            if (o instanceof TouchEventGameSprite) {
                TouchEventGameSprite sprite = (TouchEventGameSprite) o;
                LittlePerson person = (LittlePerson) sprite.getData("person");
                sayGivenNameForPerson(person);
            }
        } else  if (topic.equals(TOPIC_CARD_SELECTED)) {
            if (o instanceof TouchEventGameSprite) {
                TouchEventGameSprite sprite = (TouchEventGameSprite) o;
                Bitmap card = sprite.getBitmaps().get(0).get(0);
                Integer cardNum = (Integer) sprite.getData("cardNum");
                view.setCardBitmap(card, cardNum);
                speak(getResources().getString(R.string.decorate_a_card), new UtteranceProgressListener() {
                    @Override
                    public void onStart(String utteranceId) { }
                    @Override
                    public void onDone(String utteranceId) {
                        sayGivenNameForPerson(view.getBirthdayPerson());
                    }
                    @Override
                    public void onError(String utteranceId) { }
                });
            }
        }
    }

    public void shareImage(View view) {
        showAdultAuthDialog(shareAction);
    }

    public void showCupcakes(View view) {
        this.view.showCupcakes();
        speak("Look who has birthdays coming up.  Choose someone to start.");
    }

    public class ShareAction implements AdultsAuthDialog.AuthCompleteAction {
        public void doAction(boolean success) {
            if (success) {
                Bitmap sharing = view.getSharingBitmap();
                if (sharing != null) {
                    try {
                        File dir = BirthdayCardActivity.this.getExternalCacheDir();
                        File file = File.createTempFile("tempImage", ".jpg", dir);
                        if (file.exists()) {
                            file.delete();
                        }
                        FileOutputStream out = new FileOutputStream(file);
                        sharing.compress(Bitmap.CompressFormat.JPEG, 90, out);
                        out.flush();
                        out.close();
                        Uri screenshotUri = Uri.fromFile(file);
                        Intent sharingIntent = new Intent(Intent.ACTION_SEND);
                        sharingIntent.setType("image/*");
                        sharingIntent.putExtra(Intent.EXTRA_STREAM, screenshotUri);
                        startActivity(Intent.createChooser(sharingIntent, "Share image using"));
                    } catch (Exception e) {
                        Log.e(this.getClass().getName(), "Error sharing file", e);
                        Toast.makeText(BirthdayCardActivity.this, "Unable to share image " + e, Toast.LENGTH_LONG).show();
                        return;
                    }
                } else {
                    Toast.makeText(BirthdayCardActivity.this, "Unable to verify password", Toast.LENGTH_LONG).show();
                }
            }
            hideAdultAuthDialog();
        }
    }
}
