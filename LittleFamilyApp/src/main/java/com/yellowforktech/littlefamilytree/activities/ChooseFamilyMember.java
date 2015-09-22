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
import com.yellowforktech.littlefamilytree.activities.tasks.ChildrenLoaderTask;
import com.yellowforktech.littlefamilytree.activities.tasks.FamilyLoaderTask;
import com.yellowforktech.littlefamilytree.activities.tasks.PersonLoaderTask;
import com.yellowforktech.littlefamilytree.data.DataService;
import com.yellowforktech.littlefamilytree.data.LittlePerson;

import java.util.ArrayList;
import java.util.List;

public class ChooseFamilyMember extends LittleFamilyActivity implements AdapterView.OnItemClickListener, FamilyLoaderTask.Listener,
        PersonLoaderTask.Listener, ChildrenLoaderTask.Listener {
    public static final String SELECTED_PERSON = "selectedPerson";
    public static final String FAMILY = "family";
    public static final int LOGIN_REQUEST = 1;

    private GridView gridView;
    private FamilyMemberListAdapter adapter;
    private ArrayList<LittlePerson> family;

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
                List<LittlePerson> cousins = dataService.getDBHelper().getCousins();
                if (cousins!=null) {
                    family = new ArrayList<>(cousins);
                }
                if (cousins==null || cousins.size()<3) {
                    PersonLoaderTask task = new PersonLoaderTask(this, this);
                    task.execute();
                } else {
                    loadFamily();
                }
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
    public void onInit(int code) {
        super.onInit(code);
        speak(getResources().getString(R.string.title_activity_choose_family_member));
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
        selectedPerson = (LittlePerson) gridView.getItemAtPosition(position);
        Intent intent = new Intent( this, HomeActivity.class );
        intent.putExtra(SELECTED_PERSON, selectedPerson);
        intent.putExtra(FAMILY, family);
        startActivity(intent);
    }


    int familyCount = 0;
    @Override
    public void onComplete(ArrayList<LittlePerson> familyMembers) {
        if (familyCount==0) {
            familyCount++;
            if (this.family==null) {
                this.family = familyMembers;
            }
            else {
                for(LittlePerson p : familyMembers) {
                    if (!this.family.contains(p)) {
                        this.family.add(p);
                    }
                }
            }
            //-- get grandchildren
            LittlePerson[] people = new LittlePerson[familyMembers.size()];
            familyMembers.toArray(people);
            ChildrenLoaderTask task = new ChildrenLoaderTask(this, this, true);
            task.execute(people);
        } else {
            //-- add grandchildren
            for(LittlePerson p : familyMembers) {
                if (!this.family.contains(p)) {
                    this.family.add(p);
                }
            }
            loadFamily();
        }
    }

    public void loadFamily() {
        adapter.setFamily(this.family);
        updateColumns();
        try {
            if (DataService.getInstance().getDBHelper().getMediaCount() < 5) {
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setMessage(R.string.low_media);
                builder.setPositiveButton("OK", null);
                AlertDialog dialog = builder.create();
                dialog.show();
            }
        } catch (Exception e) {
            Log.e("ChooseFamilyMember", "Error checking database", e);
        }
        gridView.invalidate();
    }

    @Override
    public void onStatusUpdate(String message) {

    }

    private void updateColumns() {
        int width = getScreenWidth();
        int height = getScreenHeight();
        int cols = 1;
        int maxCols = 12;
        if (width < height) maxCols = 3;
        while(cols < maxCols && (width / cols) * Math.ceil(((double)adapter.getCount()) / cols) > height) cols++;
        gridView.setNumColumns(cols);
    }

    @Override
    public void onComplete(LittlePerson person) {
        FamilyLoaderTask task = new FamilyLoaderTask(this, this);
        task.execute(person);
    }
}
