package org.finlayfamily.littlefamily.activities;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.Toast;

import org.finlayfamily.littlefamily.R;
import org.finlayfamily.littlefamily.activities.adapters.FamilyMemberListAdapter;
import org.finlayfamily.littlefamily.activities.tasks.FamilyLoaderTask;
import org.finlayfamily.littlefamily.activities.util.SystemUiHider;
import org.finlayfamily.littlefamily.data.DataHelper;
import org.finlayfamily.littlefamily.data.LittlePerson;
import org.finlayfamily.littlefamily.familysearch.FamilySearchException;
import org.finlayfamily.littlefamily.familysearch.FamilySearchService;
import org.gedcomx.conclusion.Person;
import org.gedcomx.conclusion.Relationship;

import java.util.ArrayList;
import java.util.List;

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 *
 * @see SystemUiHider
 */
public class ChooseFamilyMember extends Activity implements AdapterView.OnItemClickListener, FamilyLoaderTask.Listener {
    public static final String SELECTED_PERSON = "selectedPerson";
    public static final String FAMILY = "family";
    private static final int LOGIN_REQUEST = 1;

    private GridView gridView;
    private FamilyMemberListAdapter adapter;
    private ProgressDialog pd;
    private boolean launchGame = false;

    private FamilySearchService familySearchService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        this.familySearchService = FamilySearchService.getInstance();

        setContentView(R.layout.activity_choose_family_member);

        gridView = (GridView) findViewById(R.id.gridViewFamily);
        adapter = new FamilyMemberListAdapter(this);
        gridView.setAdapter(adapter);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
    }

    @Override
    protected void onStart() {
        super.onStart();

        if (familySearchService.getSessionId()==null) {
            Intent intent = new Intent( this, FSLoginActivity.class );
            startActivityForResult( intent, LOGIN_REQUEST );
        } else {
            pd = ProgressDialog.show(this, "Please wait...", "Loading data from FamilySearch", true, false);
            LittlePerson littlePerson = null;
            try {
                Person person = familySearchService.getCurrentPerson();
                littlePerson = DataHelper.buildLittlePerson(person, this);
                FamilyLoaderTask task = new FamilyLoaderTask(littlePerson, this, this);
                task.execute();
            } catch (FamilySearchException e) {
                e.printStackTrace();
            }
        }

    }

    @Override
    protected void onActivityResult( int requestCode, int resultCode, Intent data ) {
        switch(requestCode) {
            case LOGIN_REQUEST:
                if (resultCode == RESULT_OK) {
                    Log.d("onActivityResult", "SESSION_ID:" + familySearchService.getSessionId());
                    Person person = null;
                    try {
                        person = familySearchService.getCurrentPerson();
                        LittlePerson littlePerson = DataHelper.buildLittlePerson(person, this);
                        FamilyLoaderTask task = new FamilyLoaderTask(littlePerson, this, this);
                        task.execute();
                    } catch (FamilySearchException e) {
                        e.printStackTrace();
                    }
                }
                break;
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        pd = ProgressDialog.show(this, "Please wait...", "Loading data from FamilySearch", true, false);
        launchGame = true;
        LittlePerson person = (LittlePerson) gridView.getItemAtPosition(position);
        FamilyLoaderTask task = new FamilyLoaderTask(person, this, this);
		task.execute();
    }


    @Override
    public void onComplete(ArrayList<LittlePerson> familyMembers) {
        if (pd!=null) pd.dismiss();
        if (launchGame) {
            launchGame = false;
            Intent intent = new Intent( this, MatchGameActivity.class );
            intent.putExtra(FAMILY, familyMembers);
            startActivity(intent);
        } else {
            adapter.setFamily(familyMembers);
        }
    }
}
