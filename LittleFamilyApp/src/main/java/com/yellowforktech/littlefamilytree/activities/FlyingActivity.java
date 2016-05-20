package com.yellowforktech.littlefamilytree.activities;

import android.content.Intent;
import android.graphics.PixelFormat;
import android.os.Bundle;
import android.view.SurfaceHolder;

import com.yellowforktech.littlefamilytree.R;
import com.yellowforktech.littlefamilytree.data.LittlePerson;
import com.yellowforktech.littlefamilytree.events.EventQueue;
import com.yellowforktech.littlefamilytree.games.TreeWalker;
import com.yellowforktech.littlefamilytree.views.FlyingSurfaceView;

import java.util.List;

public class FlyingActivity extends LittleFamilyActivity implements TreeWalker.Listener{

    public static final String TOPIC_SKIP_CUTSCENE = "topic_skip_custscene";
    private FlyingSurfaceView flyView;
    private TreeWalker treeWalker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_flying);

        flyView = (FlyingSurfaceView) findViewById(R.id.flyingView);
        flyView.setZOrderOnTop(true);    // necessary
        SurfaceHolder sfhTrackHolder = flyView.getHolder();
        sfhTrackHolder.setFormat(PixelFormat.TRANSPARENT);
        flyView.setActivity(this);

        Intent intent = getIntent();
        selectedPerson = (LittlePerson) intent.getSerializableExtra(ChooseFamilyMember.SELECTED_PERSON);
        treeWalker = new TreeWalker(this, selectedPerson, this, false);

        setupTopBar();
    }

    @Override
    protected void onStart() {
        super.onStart();
        EventQueue.getInstance().subscribe(TOPIC_SKIP_CUTSCENE, this);
        treeWalker.loadFamilyMembers();
        flyView.resume();
    }

    @Override
    protected void onStop() {
        super.onStop();
        EventQueue.getInstance().unSubscribe(TOPIC_SKIP_CUTSCENE, this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        flyView.stop();
    }

    @Override
    protected void onPause() {
        super.onPause();
        flyView.pause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        flyView.resume();
    }

    @Override
    public void onEvent(String topic, Object o) {
        super.onEvent(topic, o);
        if (topic.equals(TOPIC_SKIP_CUTSCENE)) {
            flyView.skipCutScene();
        }
    }

    public void loadMorePeople() {
        treeWalker.loadMorePeople();
    }

    @Override
    public void onComplete(List<LittlePerson> people) {
        flyView.setFamily(people);
    }
}
