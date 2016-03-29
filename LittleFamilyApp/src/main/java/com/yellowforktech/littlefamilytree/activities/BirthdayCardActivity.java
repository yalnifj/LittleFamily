package com.yellowforktech.littlefamilytree.activities;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.PixelFormat;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.View;
import android.widget.Toast;

import com.yellowforktech.littlefamilytree.R;
import com.yellowforktech.littlefamilytree.data.DataService;
import com.yellowforktech.littlefamilytree.data.LittlePerson;
import com.yellowforktech.littlefamilytree.util.ImageHelper;
import com.yellowforktech.littlefamilytree.views.BirthdayCardSurfaceView;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;

public class BirthdayCardActivity extends LittleFamilyActivity {
    public static final String TOPIC_PERSON_TOUCHED = "personTouched";

    private BirthdayCardSurfaceView view;
    private List<LittlePerson> people;

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

        setupTopBar();
    }

    @Override
    protected void onStart() {
        super.onStart();
        DataService.getInstance().registerNetworkStateListener(this);
        try {
            people = DataService.getInstance().getDBHelper().getNext30Birthdays();
            if (people.size()==0) {
                Log.d("BirthdayCardActivity", "Unable to find birthdays from database");
                people.add(selectedPerson);
            } else {
                DataService.getInstance().addToSyncQ(people, 5);
            }
            //-- temp set to the next birthday person
            view.setBirthdayPerson(people.get(0));
        } catch (Exception e) {
            Log.e("BirthdayCardActivity", "Error getting birthday people", e);
            people = new ArrayList<>();
            people.add(selectedPerson);
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        DataService.getInstance().unregisterNetworkStateListener(this);
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

    public void shareImage(View view) {
        showAdultAuthDialog(shareAction);
    }

    public class ShareAction implements AdultsAuthDialog.AuthCompleteAction {
        public void doAction(boolean success) {
            if (success) {
                Bitmap sharing = view.getSharingBitmap();
                if (sharing != null) {
                    File dir = ImageHelper.getDataFolder(BirthdayCardActivity.this);
                    File file = new File(dir, "tempImage.jpg");
                    if (file.exists()) {
                        file.delete();
                    }
                    try {
                        FileOutputStream out = new FileOutputStream(file);
                        sharing.compress(Bitmap.CompressFormat.JPEG, 90, out);
                        out.flush();
                        out.close();
                    } catch (Exception e) {
                        Log.e(this.getClass().getName(), "Error sharing file", e);
                        Toast.makeText(BirthdayCardActivity.this, "Unable to share image " + e, Toast.LENGTH_LONG).show();
                        return;
                    }
                    Uri screenshotUri = Uri.fromFile(file);
                    Intent sharingIntent = new Intent(Intent.ACTION_SEND);
                    sharingIntent.setType("image/*");
                    sharingIntent.putExtra(Intent.EXTRA_STREAM, screenshotUri);
                    startActivity(Intent.createChooser(sharingIntent, "Share image using"));
                } else {
                    Toast.makeText(BirthdayCardActivity.this, "Unable to verify password", Toast.LENGTH_LONG).show();
                }
            }
            hideAdultAuthDialog();
        }
    }
}
