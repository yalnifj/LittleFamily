package com.yellowforktech.littlefamilytree.activities;

import android.content.Intent;
import android.os.Bundle;

import com.yellowforktech.littlefamilytree.R;
import com.yellowforktech.littlefamilytree.data.DataService;
import com.yellowforktech.littlefamilytree.data.LittlePerson;
import com.yellowforktech.littlefamilytree.games.TreeWalker;
import com.yellowforktech.littlefamilytree.views.FlyingSurfaceView;

import java.util.List;

public class FlyingActivity extends LittleFamilyActivity implements TreeWalker.Listener{

    private FlyingSurfaceView flyView;
    private TreeWalker treeWalker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_flying);

        flyView = (FlyingSurfaceView) findViewById(R.id.flyingView);
        flyView.setActivity(this);

        Intent intent = getIntent();
        selectedPerson = (LittlePerson) intent.getSerializableExtra(ChooseFamilyMember.SELECTED_PERSON);
        treeWalker = new TreeWalker(this, selectedPerson, this, false);

        setupTopBar();
    }

    @Override
    protected void onStart() {
        super.onStart();
        DataService.getInstance().registerNetworkStateListener(this);
        treeWalker.loadFamilyMembers();
        flyView.resume();
    }

    @Override
    protected void onStop() {
        super.onStop();
        DataService.getInstance().unregisterNetworkStateListener(this);
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

    public void loadMorePeople() {
        treeWalker.loadMorePeople();
    }

    @Override
    public void onComplete(List<LittlePerson> people) {
        flyView.setFamily(people);
    }
}
