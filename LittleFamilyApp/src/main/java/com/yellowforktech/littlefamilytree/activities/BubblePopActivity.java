package com.yellowforktech.littlefamilytree.activities;

import android.content.Intent;
import android.os.Bundle;

import com.yellowforktech.littlefamilytree.R;
import com.yellowforktech.littlefamilytree.activities.tasks.ParentsLoaderTask;
import com.yellowforktech.littlefamilytree.activities.tasks.WaitTask;
import com.yellowforktech.littlefamilytree.data.DataService;
import com.yellowforktech.littlefamilytree.data.LittlePerson;
import com.yellowforktech.littlefamilytree.views.BubbleSpriteSurfaceView;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Queue;

public class BubblePopActivity extends LittleFamilyActivity implements ParentsLoaderTask.Listener, BubbleSpriteSurfaceView.BubbleCompleteListener{
    private LittlePerson selectedPerson;
    private Queue<LittlePerson> que;
    private BubbleSpriteSurfaceView bubbleView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bubble_pop);

        bubbleView = (BubbleSpriteSurfaceView) findViewById(R.id.bubbleView);
        bubbleView.setActivity(this);
        bubbleView.registerListener(this);

        Intent intent = getIntent();
        selectedPerson = (LittlePerson) intent.getSerializableExtra(ChooseFamilyMember.SELECTED_PERSON);

        que = new LinkedList<>();
        que.add(selectedPerson);

        setupTopBar();
    }

    @Override
    protected void onStart() {
        super.onStart();
        DataService.getInstance().registerNetworkStateListener(this);
        ParentsLoaderTask loader = new ParentsLoaderTask(this, this);
        loader.execute(que.peek());
    }

    @Override
    protected void onStop() {
        super.onStop();
        DataService.getInstance().unregisterNetworkStateListener(this);
    }

    @Override
    public void onComplete(ArrayList<LittlePerson> parents) {
        que.addAll(parents);
        ArrayList<LittlePerson> children = new ArrayList<>(3);
        children.add(que.poll());
        bubbleView.setParentsAndChildren(parents, children);
    }

    @Override
    public void onBubbleComplete() {
        playCompleteSound();
        WaitTask waiter = new WaitTask(new WaitTask.WaitTaskListener() {
            @Override
            public void onProgressUpdate(Integer progress) {
            }

            @Override
            public void onComplete(Integer progress) {
                ParentsLoaderTask loader = new ParentsLoaderTask(BubblePopActivity.this, BubblePopActivity.this);
                loader.execute(que.peek());
            }
        });
        waiter.execute(3000L);
    }
}
