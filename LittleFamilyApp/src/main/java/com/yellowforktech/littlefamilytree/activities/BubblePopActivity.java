package com.yellowforktech.littlefamilytree.activities;

import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;

import com.yellowforktech.littlefamilytree.R;
import com.yellowforktech.littlefamilytree.activities.tasks.FamilyLoaderTask;
import com.yellowforktech.littlefamilytree.activities.tasks.ParentsLoaderTask;
import com.yellowforktech.littlefamilytree.activities.tasks.WaitTask;
import com.yellowforktech.littlefamilytree.data.LittlePerson;
import com.yellowforktech.littlefamilytree.games.RecentPersonTracker;
import com.yellowforktech.littlefamilytree.views.BubbleSpriteSurfaceView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Set;

public class BubblePopActivity extends LittleFamilyActivity implements ParentsLoaderTask.Listener, BubbleSpriteSurfaceView.BubbleCompleteListener{
    private LittlePerson selectedPerson;
    private Queue<LittlePerson> que;
    private Set<LittlePerson> completed;
    private BubbleSpriteSurfaceView bubbleView;
    private RecentPersonTracker personTracker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bubble_pop);

        bubbleView = (BubbleSpriteSurfaceView) findViewById(R.id.bubbleView);
        bubbleView.setActivity(this);
        bubbleView.registerListener(this);

        Intent intent = getIntent();
        selectedPerson = (LittlePerson) intent.getSerializableExtra(ChooseFamilyMember.SELECTED_PERSON);

        personTracker = RecentPersonTracker.getInstance();

        que = new LinkedList<>();
        que.add(selectedPerson);

        completed = new HashSet<>();

        setupTopBar();
    }

    @Override
    protected void onStart() {
        super.onStart();
        ParentsLoaderTask loader = new ParentsLoaderTask(this, this, true);
        loader.execute(que.peek());
        bubbleView.resume();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

    }

    @Override
    protected void onStop() {
        super.onStop();
        bubbleView.stop();
    }

    @Override
    protected void onPause() {
        super.onPause();
        bubbleView.pause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        bubbleView.resume();
    }

    @Override
    public void onComplete(ArrayList<LittlePerson> parents) {
        LittlePerson person = null;
        synchronized (que) {
            que.addAll(parents);
            ArrayList<LittlePerson> children = new ArrayList<>(3);
            person = que.poll();
            if (personTracker.personRecentlyUsed(person) && que.size()>0) {
                ParentsLoaderTask loader = new ParentsLoaderTask(this, this);
                loader.execute(que.peek());
                return;
            }
            completed.add(person);
            personTracker.addPerson(person);
            children.add(person);
            bubbleView.setParentsAndChildren(parents, children);
        }

        if (person.getTreeLevel()!=null && person.getTreeLevel()<2) {
            FamilyLoaderTask familyLoaderTask = new FamilyLoaderTask(new FamilyLoaderTask.Listener() {
                @Override
                public void onComplete(ArrayList<LittlePerson> family) {
                    synchronized (que) {
                        for (LittlePerson p : family) {
                            if (!completed.contains(p) && !que.contains(p)) {
                                que.add(p);
                            }
                        }

                        Collections.shuffle((List<?>) que);
                    }
                }

                @Override
                public void onStatusUpdate(String message) {
                }
            }, this);
            familyLoaderTask.execute(person);
        }
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
                ParentsLoaderTask loader = new ParentsLoaderTask(BubblePopActivity.this, BubblePopActivity.this, true);
                loader.execute(que.peek());
            }
        });
        waiter.execute(3000L);
    }
}
