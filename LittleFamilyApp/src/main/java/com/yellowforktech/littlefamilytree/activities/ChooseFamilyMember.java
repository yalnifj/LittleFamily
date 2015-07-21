package com.yellowforktech.littlefamilytree.activities;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;

import com.yellowforktech.littlefamilytree.R;
import com.yellowforktech.littlefamilytree.activities.adapters.FamilyMemberListAdapter;
import com.yellowforktech.littlefamilytree.activities.tasks.FamilyLoaderTask;
import com.yellowforktech.littlefamilytree.activities.tasks.PersonLoaderTask;
import com.yellowforktech.littlefamilytree.data.DataService;
import com.yellowforktech.littlefamilytree.data.LittlePerson;

import java.util.ArrayList;

public class ChooseFamilyMember extends LittleFamilyActivity implements AdapterView.OnItemClickListener, FamilyLoaderTask.Listener, PersonLoaderTask.Listener {
    public static final String SELECTED_PERSON = "selectedPerson";
    public static final String FAMILY = "family";
    public static final int LOGIN_REQUEST = 1;

    private GridView gridView;
    private FamilyMemberListAdapter adapter;
    private ArrayList<LittlePerson> family;
    private boolean launchGame = false;

    private DataService dataService;
    private LittlePerson selectedPerson;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choose_family_member);

        this.dataService = DataService.getInstance();
        this.dataService.setContext(this);


        gridView = (GridView) findViewById(R.id.gridViewFamily);
        adapter = new FamilyMemberListAdapter(this);
        gridView.setAdapter(adapter);
        gridView.setOnItemClickListener(this);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
    }

    @Override
    protected void onStart() {
        super.onStart();
        DataService.getInstance().registerNetworkStateListener(this);
        try {
            if (!dataService.hasData()) {
                Intent intent = new Intent( this, ChooseRemoteService.class );
                startActivityForResult( intent, LOGIN_REQUEST );
            } else {
                PersonLoaderTask task = new PersonLoaderTask(this, this);
                task.execute();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    @Override
    protected void onStop() {
        super.onStop();
        DataService.getInstance().unregisterNetworkStateListener(this);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        updateColumns();
    }

    @Override
    protected void onActivityResult( int requestCode, int resultCode, Intent data ) {
        switch(requestCode) {
            case LOGIN_REQUEST:
                if (resultCode == RESULT_OK) {
                    PersonLoaderTask task = new PersonLoaderTask(this, this);
                    task.execute();
                }
                break;
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        launchGame = true;
        selectedPerson = (LittlePerson) gridView.getItemAtPosition(position);
        Intent intent = new Intent( this, HomeActivity.class );
        intent.putExtra(SELECTED_PERSON, selectedPerson);
        intent.putExtra(FAMILY, family);
        startActivity(intent);
    }


    @Override
    public void onComplete(ArrayList<LittlePerson> familyMembers) {
        this.family = familyMembers;
        adapter.setFamily(familyMembers);
        updateColumns();
        speak(getResources().getString(R.string.title_activity_choose_family_member));
        try {
            if (DataService.getInstance().getDBHelper().getMediaCount() < 3) {
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setMessage(R.string.low_media);
                builder.setPositiveButton("OK", null);
                AlertDialog dialog = builder.create();
                dialog.show();
            }
        } catch (Exception e) {
            Log.e("ChooseFamilyMember", "Error checking database", e);
        }
    }

    @Override
    public void onStatusUpdate(String message) {

    }

    private void updateColumns() {
        int width = getScreenWidth();
        int height = getScreenHeight();
        int cols = 2;
        while(cols < 12 && (width / cols) * Math.ceil(((double)adapter.getCount()) / cols) > height) cols++;
        gridView.setNumColumns(cols);
    }

    @Override
    public void onComplete(LittlePerson person) {
        FamilyLoaderTask task = new FamilyLoaderTask(this, this);
        task.execute(person);
    }
}
