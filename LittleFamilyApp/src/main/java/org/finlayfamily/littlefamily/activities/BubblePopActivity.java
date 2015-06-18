package org.finlayfamily.littlefamily.activities;

import android.content.Intent;
import android.os.Bundle;

import org.finlayfamily.littlefamily.R;
import org.finlayfamily.littlefamily.activities.tasks.ParentsLoaderTask;
import org.finlayfamily.littlefamily.data.LittlePerson;
import org.finlayfamily.littlefamily.views.BubbleSpriteSurfaceView;

import java.util.ArrayList;

public class BubblePopActivity extends LittleFamilyActivity implements ParentsLoaderTask.Listener, BubbleSpriteSurfaceView.BubbleCompleteListener{
    private LittlePerson selectedPerson;
    private BubbleSpriteSurfaceView bubbleView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bubble_pop);

        bubbleView = (BubbleSpriteSurfaceView) findViewById(R.id.bubbleView);

        Intent intent = getIntent();
        selectedPerson = (LittlePerson) intent.getSerializableExtra(ChooseFamilyMember.SELECTED_PERSON);


        setupTopBar();
    }

    @Override
    protected void onStart() {
        super.onStart();
        ParentsLoaderTask loader = new ParentsLoaderTask(this, this);
        loader.execute(selectedPerson);
        showLoadingDialog();
    }

    @Override
    public void onComplete(ArrayList<LittlePerson> parents) {
        hideLoadingDialog();
        bubbleView.setParents(parents);
        ArrayList<LittlePerson> children = new ArrayList<>(3);
        children.add(selectedPerson);
        bubbleView.setChildren(children);
    }

    @Override
    public void onBubbleComplete() {

    }
}
