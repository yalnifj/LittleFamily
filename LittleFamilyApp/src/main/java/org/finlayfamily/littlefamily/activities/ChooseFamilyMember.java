package org.finlayfamily.littlefamily.activities;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Point;
import android.os.Bundle;
import android.view.Display;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;

import org.finlayfamily.littlefamily.R;
import org.finlayfamily.littlefamily.activities.adapters.FamilyMemberListAdapter;
import org.finlayfamily.littlefamily.activities.tasks.FamilyLoaderTask;
import org.finlayfamily.littlefamily.activities.tasks.PersonLoaderTask;
import org.finlayfamily.littlefamily.activities.util.SystemUiHider;
import org.finlayfamily.littlefamily.data.DataService;
import org.finlayfamily.littlefamily.data.LittlePerson;

import java.util.ArrayList;

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 *
 * @see SystemUiHider
 */
public class ChooseFamilyMember extends LittleFamilyActivity implements AdapterView.OnItemClickListener, FamilyLoaderTask.Listener, PersonLoaderTask.Listener {
    public static final String SELECTED_PERSON = "selectedPerson";
    public static final String FAMILY = "family";
    private static final int LOGIN_REQUEST = 1;

    private GridView gridView;
    private FamilyMemberListAdapter adapter;
    private ProgressDialog pd;
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
        updateColumns();
    }

    @Override
    protected void onStart() {
        super.onStart();

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
        pd = ProgressDialog.show(this, "Please wait...", "Loading data from "+dataService.getServiceType(), true, false);
        launchGame = true;
        selectedPerson = (LittlePerson) gridView.getItemAtPosition(position);
        FamilyLoaderTask task = new FamilyLoaderTask(this, this);
		task.execute(selectedPerson);
    }


    @Override
    public void onComplete(ArrayList<LittlePerson> familyMembers) {
        if (pd!=null) pd.dismiss();
        if (launchGame) {
            launchGame = false;
            Intent intent = new Intent( this, ChooseGameActivity.class );
            intent.putExtra(SELECTED_PERSON, selectedPerson);
            intent.putExtra(FAMILY, familyMembers);
            startActivity(intent);
        } else {
            adapter.setFamily(familyMembers);
            updateColumns();
            speak(getResources().getString(R.string.title_activity_choose_family_member));
        }
    }

    private void updateColumns() {
        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        int width = size.x;
        int height = size.y;
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
