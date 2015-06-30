package org.finlayfamily.littlefamily.activities;

import android.content.Intent;
import android.os.Bundle;

import org.finlayfamily.littlefamily.R;
import org.finlayfamily.littlefamily.activities.tasks.ParentsLoaderTask;
import org.finlayfamily.littlefamily.data.LittlePerson;
import org.finlayfamily.littlefamily.views.BubbleSpriteSurfaceView;

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
        //showLoadingDialog();
        ParentsLoaderTask loader = new ParentsLoaderTask(this, this);
        loader.execute(que.peek());
    }

    @Override
    public void onComplete(ArrayList<LittlePerson> parents) {
        que.addAll(parents);
        ArrayList<LittlePerson> children = new ArrayList<>(3);
        children.add(que.poll());
        bubbleView.setParentsAndChildren(parents, children);
        //hideLoadingDialog();
    }

    @Override
    public void onBubbleComplete() {
        playCompleteSound();
        ParentsLoaderTask loader = new ParentsLoaderTask(this, this);
        loader.execute(que.peek());
    }
}
