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
    private static final int LOADER_FAMILY = 0x1;
    /**
     * Whether or not the system UI should be auto-hidden after
     * {@link #AUTO_HIDE_DELAY_MILLIS} milliseconds.
     */
    private static final boolean AUTO_HIDE = true;

    /**
     * If {@link #AUTO_HIDE} is set, the number of milliseconds to wait after
     * user interaction before hiding the system UI.
     */
    private static final int AUTO_HIDE_DELAY_MILLIS = 3000;

    /**
     * If set, will toggle the system UI visibility upon interaction. Otherwise,
     * will show the system UI visibility upon interaction.
     */
    private static final boolean TOGGLE_ON_CLICK = true;

    /**
     * The flags to pass to {@link SystemUiHider#getInstance}.
     */
    private static final int HIDER_FLAGS = SystemUiHider.FLAG_HIDE_NAVIGATION;

    /**
     * The instance of the {@link SystemUiHider} for this activity.
     */
    private SystemUiHider mSystemUiHider;

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

        // Set up an instance of SystemUiHider to control the system UI for
        // this activity.
        mSystemUiHider = SystemUiHider.getInstance(this, gridView, HIDER_FLAGS);
        mSystemUiHider.setup();
        mSystemUiHider
                .setOnVisibilityChangeListener(new SystemUiHider.OnVisibilityChangeListener() {
                    // Cached values.
                    int mControlsHeight;
                    int mShortAnimTime;

                    @Override
                    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
                    public void onVisibilityChange(boolean visible) {
                        if (visible && AUTO_HIDE) {
                            // Schedule a hide().
                            delayedHide(AUTO_HIDE_DELAY_MILLIS);
                        }
                    }
                });
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        // Trigger the initial hide() shortly after the activity has been
        // created, to briefly hint to the user that UI controls
        // are available.
        delayedHide(100);
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

    /**
     * Touch listener to use for in-layout UI controls to delay hiding the
     * system UI. This is to prevent the jarring behavior of controls going away
     * while interacting with activity UI.
     */
    View.OnTouchListener mDelayHideTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            if (AUTO_HIDE) {
                delayedHide(AUTO_HIDE_DELAY_MILLIS);
            }
            return false;
        }
    };

    Handler mHideHandler = new Handler();
    Runnable mHideRunnable = new Runnable() {
        @Override
        public void run() {
            mSystemUiHider.hide();
        }
    };

    /**
     * Schedules a call to hide() in [delay] milliseconds, canceling any
     * previously scheduled calls.
     */
    private void delayedHide(int delayMillis) {
        mHideHandler.removeCallbacks(mHideRunnable);
        mHideHandler.postDelayed(mHideRunnable, delayMillis);
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
