package com.yellowforktech.littlefamilytree.activities;

import android.content.Intent;
import android.graphics.PixelFormat;
import android.os.Bundle;
import android.view.SurfaceHolder;

import com.yellowforktech.littlefamilytree.R;
import com.yellowforktech.littlefamilytree.activities.tasks.ChildrenLoaderTask;
import com.yellowforktech.littlefamilytree.activities.tasks.ParentsLoaderTask;
import com.yellowforktech.littlefamilytree.data.DataService;
import com.yellowforktech.littlefamilytree.data.LittlePerson;
import com.yellowforktech.littlefamilytree.views.SongSpriteSurfaceView;

import org.gedcomx.types.GenderType;

import java.util.ArrayList;
import java.util.List;

public class SongActivity extends LittleFamilyActivity {
    private SongSpriteSurfaceView view;
    private List<LittlePerson> people;
    private List<LittlePerson> parents;

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

        setupTopBar();
    }

    @Override
    protected void onStart() {
        super.onStart();
        DataService.getInstance().registerNetworkStateListener(this);
        people = new ArrayList<>();
        loadMoreFamilyMembers();
    }

    @Override
    protected void onStop() {
        super.onStop();
        DataService.getInstance().unregisterNetworkStateListener(this);
    }

    private void loadMoreFamilyMembers() {
        ParentsLoaderTask ptask = new ParentsLoaderTask(new ParentsLoaderTask.Listener() {
            @Override
            public void onComplete(ArrayList<LittlePerson> family) {
                if (family!=null && family.size()>0) {
                    for (LittlePerson parent : family) {
                        if (parent.getGender() == GenderType.Female)
                            parent.setRelationship("Mommy");
                        else
                            parent.setRelationship("Daddy");
                        people.add(parent);
                    }

                    parents = family;

                    ChildrenLoaderTask ctask = new ChildrenLoaderTask(childListener, SongActivity.this);
                    LittlePerson[] people = new LittlePerson[family.size()];
                    people = family.toArray(people);
                    ctask.execute(people);
                }

                ChildrenLoaderTask ctask2 = new ChildrenLoaderTask(new ChildrenLoaderTask.Listener() {
                    @Override
                    public void onComplete(ArrayList<LittlePerson> family) {
                        if (family!=null && family.size()>0) {
                            for (LittlePerson child : family) {
                                if (child.getGender() == GenderType.Female)
                                    child.setRelationship("Daughter");
                                else
                                    child.setRelationship("Son");
                                people.add(child);
                            }
                            view.setFamily(people);
                        }
                    }
                    @Override
                    public void onStatusUpdate(String message) { }
                }, SongActivity.this);
                ctask2.execute(selectedPerson);
            }
        }, this);
        ptask.execute(selectedPerson);
    }

    private ChildrenLoaderTask.Listener childListener = new ChildrenLoaderTask.Listener() {
        @Override
        public void onComplete(ArrayList<LittlePerson> family) {
            if (family!=null && family.size()>0) {
                for (LittlePerson child : family) {
                    if (child.getGender() == GenderType.Female)
                        child.setRelationship("Sister");
                    else
                        child.setRelationship("Brother");
                    people.add(child);
                }
            }

            ParentsLoaderTask gptask = new ParentsLoaderTask(grandParentListener, SongActivity.this);
            LittlePerson[] people = new LittlePerson[parents.size()];
            people = parents.toArray(people);
            gptask.execute(people);
        }
        @Override
        public void onStatusUpdate(String message) { }
    };

    private ParentsLoaderTask.Listener grandParentListener = new ParentsLoaderTask.Listener() {
        @Override
        public void onComplete(ArrayList<LittlePerson> family) {
            if (family!=null && family.size()>0) {
                for (LittlePerson child : family) {
                    if (child.getGender() == GenderType.Female)
                        child.setRelationship("Grand mother");
                    else
                        child.setRelationship("Grand father");
                    people.add(child);
                }
            }

            view.setFamily(people);
        }
    };
}
