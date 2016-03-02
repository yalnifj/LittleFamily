package com.yellowforktech.littlefamilytree.activities;

import android.content.Intent;
import android.graphics.PixelFormat;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.view.SurfaceHolder;

import com.yellowforktech.littlefamilytree.R;
import com.yellowforktech.littlefamilytree.activities.tasks.WaitTask;
import com.yellowforktech.littlefamilytree.data.DataService;
import com.yellowforktech.littlefamilytree.data.LittlePerson;
import com.yellowforktech.littlefamilytree.games.TreeWalker;
import com.yellowforktech.littlefamilytree.views.SongSpriteSurfaceView;

import java.util.List;

public class SongActivity extends LittleFamilyActivity implements TreeWalker.Listener {
    private SongSpriteSurfaceView view;
    private TreeWalker treeWalker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_song);

        view = (SongSpriteSurfaceView) findViewById(R.id.view);
        view.setActivity(this);
        view.setZOrderOnTop(true);    // necessary
        SurfaceHolder sfhTrackHolder = view.getHolder();
        sfhTrackHolder.setFormat(PixelFormat.TRANSPARENT);

        Intent intent = getIntent();
        selectedPerson = (LittlePerson) intent.getSerializableExtra(ChooseFamilyMember.SELECTED_PERSON);
        view.setPlayer(selectedPerson);

        treeWalker = new TreeWalker(this, selectedPerson, this);

        setupTopBar();
    }

    @Override
    protected void onStart() {
        super.onStart();
        DataService.getInstance().registerNetworkStateListener(this);
        treeWalker.loadFamilyMembers();
    }

    @Override
    public void onInit(int code) {
        super.onInit(code);
        if (code == TextToSpeech.SUCCESS) {
            WaitTask waiter = new WaitTask(new WaitTask.WaitTaskListener() {
                @Override
                public void onProgressUpdate(Integer progress) { }

                @Override
                public void onComplete(Integer progress) {
                    speak(getString(R.string.choose_a_song));
                }
            });
            waiter.execute(1500L);
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        DataService.getInstance().unregisterNetworkStateListener(this);
    }

    @Override
    public void onComplete(List<LittlePerson> people) {
        view.setFamily(people);
    }

    public void loadMorePeople() {
        treeWalker.loadMorePeople();
    }
}
