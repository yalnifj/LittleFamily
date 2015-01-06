package org.finlayfamily.littlefamily.activities;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.Toast;

import org.finlayfamily.littlefamily.R;
import org.finlayfamily.littlefamily.activities.adapters.FamilyMemberListAdapter;
import org.finlayfamily.littlefamily.activities.util.SystemUiHider;
import org.finlayfamily.littlefamily.data.LittlePerson;
import org.finlayfamily.littlefamily.familysearch.FamilySearchException;
import org.finlayfamily.littlefamily.familysearch.FamilySearchService;
import org.finlayfamily.littlefamily.util.ImageHelper;
import org.gedcomx.conclusion.Person;
import org.gedcomx.conclusion.Relationship;
import org.gedcomx.links.Link;

import java.io.File;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.List;

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 *
 * @see SystemUiHider
 */
public class ChooseFamilyMember extends Activity implements AdapterView.OnItemClickListener {
    private static final int LOGIN_REQUEST = 1;

    private GridView gridView;
    private FamilyMemberListAdapter adapter;
    private ProgressDialog pd;

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
            FamilyLoaderTask task = new FamilyLoaderTask();
            task.execute();
        }

    }

    @Override
    protected void onActivityResult( int requestCode, int resultCode, Intent data ) {
        switch(requestCode) {
            case LOGIN_REQUEST:
                if (resultCode == RESULT_OK) {
                    Log.d("onActivityResult", "SESSION_ID:" + familySearchService.getSessionId());
                    FamilyLoaderTask task = new FamilyLoaderTask();
                    task.execute();
                }
                break;
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

    }

    public class FamilyLoaderTask extends AsyncTask<String, Integer, List<LittlePerson>> {
        @Override
        protected List<LittlePerson> doInBackground(String[] params) {
            List<LittlePerson> familyMembers = new ArrayList<>();
            FamilySearchService service = FamilySearchService.getInstance();
            try {
                List<Relationship> family = service.getCloseRelatives();
                Person currentPerson = service.getCurrentPerson();
                LittlePerson cp = buildLittlePerson(currentPerson);
                familyMembers.add(cp);

                for(Relationship r : family) {
                    Log.d("onPostExecute", "Relationship " + r.getKnownType() + " with " + r.getPerson1().getResourceId() + ":" + r.getPerson2().getResourceId());
                    if (!r.getPerson1().getResourceId().equals(currentPerson.getId())) {
                        Person fsPerson = service.getPerson(r.getPerson1().getResourceId());
                        LittlePerson person = buildLittlePerson(fsPerson);
                        familyMembers.add(person);
                    }
                    if (!r.getPerson2().getResourceId().equals(currentPerson.getId())) {
                        Person fsPerson = service.getPerson(r.getPerson2().getResourceId());
                        LittlePerson person = buildLittlePerson(fsPerson);
                        familyMembers.add(person);
                    }
                }
            } catch(FamilySearchException e) {
                Log.e(this.getClass().getSimpleName(), "error", e);
                Toast.makeText(ChooseFamilyMember.this, "Error communicating with FamilySearch. " + e, Toast.LENGTH_LONG).show();
            }
            return familyMembers;
        }

        @Override
        protected void onPostExecute(List<LittlePerson> familyMembers) {
            if (pd!=null) pd.dismiss();
            adapter.setFamily(familyMembers);
        }

        public LittlePerson buildLittlePerson(Person fsPerson) throws FamilySearchException {
            FamilySearchService service = FamilySearchService.getInstance();
            LittlePerson person = new LittlePerson(fsPerson);
            //-- check if we already have a photo downloaded for this person
            File dataFolder = ImageHelper.getDataFolder(ChooseFamilyMember.this);
            File imageFile = new File(dataFolder, fsPerson.getId());
            if (imageFile.exists()) {
                person.setPhotoPath(imageFile.getAbsolutePath());
            } else {
                Link portrait = service.getPersonPortrait(fsPerson.getId());
                if (portrait != null) {
                    String imagePath = null;
                    try {
                        Uri uri = Uri.parse(portrait.getHref().toString());
                        imagePath = service.downloadImage(uri, fsPerson.getId(), ChooseFamilyMember.this);
                        person.setPhotoPath(imagePath);
                    } catch (MalformedURLException e) {
                        Log.e(this.getClass().getSimpleName(), "error", e);
                    }
                }
            }
            return person;
        }

    }
}
