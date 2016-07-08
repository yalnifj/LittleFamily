package com.yellowforktech.littlefamilytree.activities;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.preference.PreferenceManager;
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
import com.yellowforktech.littlefamilytree.events.AutoStartNotifyReceiver;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

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

    private boolean forResult = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choose_family_member);

        this.dataService = DataService.getInstance();
        this.dataService.setContext(this);

        Intent intent = getIntent();
        if (intent.getAction() != null) {
            forResult = true;
        }

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
                //-- setup notification alarms
                AutoStartNotifyReceiver.scheduleAlarms(this);

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
        if (!forResult) {
            Intent intent = new Intent( this, HomeActivity.class );
            intent.putExtra(ChooseFamilyMember.FAMILY, family);
            intent.putExtra(ChooseFamilyMember.SELECTED_PERSON, selectedPerson);
            startActivity(intent);
        } else {
            Intent intent = new Intent();
            intent.putExtra(ChooseFamilyMember.SELECTED_PERSON, selectedPerson);
            intent.putExtra(ChooseFamilyMember.FAMILY, family);
            setResult(Activity.RESULT_OK, intent);
            finish();
        }
    }

    public void onLoginButtonClicked(View view) {
        if (dataService.getServiceType().equals(DataService.SERVICE_TYPE_PHPGEDVIEW)) {
            Intent intent = new Intent( this, PGVLoginActivity.class );
            startActivity(intent);
        }
        if (dataService.getServiceType().equals(DataService.SERVICE_TYPE_FAMILYSEARCH)) {
            Intent intent = new Intent( this, FSLoginActivity.class );
            startActivity(intent);
        }
    }

    public void onParentsGuideClicked(View view) {
        Intent intent = new Intent( this, ParentsGuideActivity.class );
        startActivity(intent);
    }


    int familyCount = 0;
    @Override
    public void onComplete(ArrayList<LittlePerson> familyMembers) {
        if (familyCount==0) {
            familyCount++;
            if (this.family==null) {
                this.family = new ArrayList<>();
            }
            for(LittlePerson p : familyMembers) {
                if ((p.getTreeLevel()==null || p.getTreeLevel() <=0) && !this.family.contains(p)) {
                    this.family.add(p);
                }
            }

            if (family.size()<=1) {
                family.addAll(familyMembers);
            }

            //-- get grandchildren
            LittlePerson[] people = new LittlePerson[family.size()];
            family.toArray(people);
            ChildrenLoaderTask task = new ChildrenLoaderTask(this, this, true);
            task.execute(people);
        } else {
            //-- add grandchildren
            for(LittlePerson p : familyMembers) {
                if (!this.family.contains(p)) {
                    this.family.add(p);
                }
            }

            Comparator<LittlePerson> comp = new Comparator<LittlePerson>() {
                @Override
                public int compare(LittlePerson lhs, LittlePerson rhs) {
                    if (lhs.getAge()!=null && rhs.getAge()!=null) {
                        return lhs.getAge().compareTo(rhs.getAge());
                    }
                    return 0;
                }
            };
            Collections.sort(this.family, comp);

            showFamily();
        }
    }

    public void showFamily() {
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
        familyCount = 0;
        family = new ArrayList<>();
        family.add(person);
        FamilyLoaderTask task = new FamilyLoaderTask(this, this);
        Boolean showStepChildren = PreferenceManager.getDefaultSharedPreferences(this).getBoolean("show_step_children", true);
        task.setGetInLaws(showStepChildren);
        task.execute(person);
    }
}
