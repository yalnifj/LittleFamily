package com.yellowforktech.littlefamilytree.activities;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.yellowforktech.littlefamilytree.R;
import com.yellowforktech.littlefamilytree.activities.tasks.AuthTask;
import com.yellowforktech.littlefamilytree.activities.tasks.FSPedigreeTask;
import com.yellowforktech.littlefamilytree.activities.tasks.InitialDataLoaderTask;
import com.yellowforktech.littlefamilytree.activities.tasks.PersonLoaderTask;
import com.yellowforktech.littlefamilytree.data.DataService;
import com.yellowforktech.littlefamilytree.data.LittlePerson;
import com.yellowforktech.littlefamilytree.db.FireHelper;
import com.yellowforktech.littlefamilytree.remote.RemoteResult;
import com.yellowforktech.littlefamilytree.remote.familysearch.FamilySearchService;

import java.util.ArrayList;
import java.util.Map;

/**
 * A login screen that offers login via email/password.
 */
public class FSLoginActivity extends Activity implements AuthTask.Listener, PersonLoaderTask.Listener, InitialDataLoaderTask.Listener,
        FSPedigreeTask.Listener {
    private static final String FS_DEFAULT_USER = "tum000205905";
    private static final String FS_DEFAULT_PASS = "1234pass";

    // UI references.
    private AutoCompleteTextView mEmailView;
    private EditText mPasswordView;
    private ProgressDialog pd;
    private DataService dataService;
    private Intent intent;
    private FireHelper fireHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(com.yellowforktech.littlefamilytree.R.layout.activity_fslogin);

        dataService = DataService.getInstance();
        dataService.setContext(this);

        fireHelper = FireHelper.getInstance();
        fireHelper.authenticate();

        // Set up the login form.
        mEmailView = (AutoCompleteTextView) findViewById(com.yellowforktech.littlefamilytree.R.id.email);
        try {
            String defaultUser = dataService.getDBHelper().getProperty(DataService.SERVICE_USERNAME);
            //if (defaultUser==null) defaultUser = FS_DEFAULT_USER;
            mEmailView.setText(defaultUser);
        } catch (Exception e) {
            e.printStackTrace();
        }

        mPasswordView = (EditText) findViewById(com.yellowforktech.littlefamilytree.R.id.password);
        //mPasswordView.setText(FS_DEFAULT_PASS);


        Button mEmailSignInButton = (Button) findViewById(com.yellowforktech.littlefamilytree.R.id.email_sign_in_button);
        mEmailSignInButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                attemptLogin();
            }
        });
    }


    /**
     * Attempts to sign in or register the account specified by the login form.
     * If there are form errors (invalid email, missing fields, etc.), the
     * errors are presented and no actual login attempt is made.
     */
    public void attemptLogin() {
        // Reset errors.
        mEmailView.setError(null);
        mPasswordView.setError(null);

        // Store values at the time of the login attempt.
        String username = mEmailView.getText().toString();
        String password = mPasswordView.getText().toString();

        boolean cancel = false;
        View focusView = null;


        // Check for a valid password, if the user entered one.
        if (!TextUtils.isEmpty(password) && !isPasswordValid(password)) {
            mPasswordView.setError(getString(com.yellowforktech.littlefamilytree.R.string.error_invalid_password));
            focusView = mPasswordView;
            cancel = true;
        }

        // Check for a valid email address.
        if (TextUtils.isEmpty(username)) {
            mEmailView.setError(getString(com.yellowforktech.littlefamilytree.R.string.error_field_required));
            focusView = mEmailView;
            cancel = true;
        } else if (!isEmailValid(username)) {
            mEmailView.setError(getString(com.yellowforktech.littlefamilytree.R.string.error_invalid_email));
            focusView = mEmailView;
            cancel = true;
        }

        if (cancel) {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            focusView.requestFocus();
        } else {
            pd = ProgressDialog.show(this, getResources().getString(R.string.please_wait), getResources().getString(R.string.login_fs), true, false);
            Log.d(this.getClass().getSimpleName(), "Launching new AuthTask for user entered credentials username="+username);
            dataService.setRemoteService(DataService.SERVICE_TYPE_FAMILYSEARCH, FamilySearchService.getInstance());
            AuthTask task = new AuthTask(this, dataService.getRemoteService());
            task.execute(username, password);
        }
    }

    private boolean isEmailValid(String email) {
        return email.length() > 1;
    }

    private boolean isPasswordValid(String password) {
        return password.length() > 1;
    }

    @Override
    public void onComplete(RemoteResult response) {
        if (response!=null && response.isSuccess()) {
            try {
                //dataService.getDBHelper().saveProperty(DataService.SERVICE_TYPE, dataService.getRemoteService().getClass().getSimpleName());
                SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(this).edit();
                editor.putString(DataService.SERVICE_TYPE, dataService.getRemoteService().getClass().getSimpleName());
                editor.commit();
                String username = mEmailView.getText().toString();
                mEmailView.getText().clear();
                dataService.getDBHelper().saveProperty(DataService.SERVICE_USERNAME, username);
                String password = mPasswordView.getText().toString();
                mPasswordView.getText().clear();
                String token = dataService.getRemoteService().createEncodedAuthToken(username, password);
                dataService.saveEncryptedProperty(DataService.SERVICE_TYPE_FAMILYSEARCH + DataService.SERVICE_TOKEN, token);

                fireHelper.createOrUpdateUser(username, dataService.getServiceType(), false);

                username = null;
                password = null;
            } catch (Exception e) {
                Log.e("FSLoginActivity", "Error saving property", e);
            }
            pd.setMessage(getResources().getString(R.string.loading_person));
            intent = new Intent();
            PersonLoaderTask task = new PersonLoaderTask(this, this);
            task.setIgnoreLocal(true);
            task.execute();
        }
        else {
            pd.dismiss();
            String message = getResources().getString(R.string.username_fail);
            if (response!=null) message += response.getData();
            Toast.makeText(FSLoginActivity.this, message, Toast.LENGTH_LONG).show();
            setResult( Activity.RESULT_CANCELED, null );
        }
    }

    @Override
    public void onComplete(LittlePerson person) {
        if (person!=null) {
            pd.setMessage(getResources().getString(R.string.loading_close));
            intent.putExtra(ChooseFamilyMember.SELECTED_PERSON, person);
            try {
                dataService.getDBHelper().saveProperty(DataService.ROOT_PERSON_ID, String.valueOf(person.getId()));
            } catch (Exception e) {
                Log.e("FSLoginActivity", "Error saving property", e);
            }
            InitialDataLoaderTask task = new InitialDataLoaderTask(this, this);
            task.execute(person);
        } else {
            Toast.makeText(FSLoginActivity.this, "Error reading data from FamilySearch", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onComplete(ArrayList<LittlePerson> familyMembers) {
        intent.putExtra(ChooseFamilyMember.FAMILY, familyMembers);
        setResult(Activity.RESULT_OK, intent);

        /*
        LittlePerson[] people = null;
        LittlePerson person = (LittlePerson) intent.getSerializableExtra(ChooseFamilyMember.SELECTED_PERSON);
        pd.setMessage("Loading spouses for "+person.getName());
        try {
            List<LittlePerson> spouses = dataService.getSpouses(person);
            if (spouses!=null) {
                people = new LittlePerson[spouses.size()+1];
                people[0] = person;
                int i =1;
                for(LittlePerson p : spouses) {
                    people[i] = p;
                    i++;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (people==null) {
            people = new LittlePerson[1];
            people[0] = person;
        }

        FSPedigreeTask task = new FSPedigreeTask(this);
        task.execute(people);
        */
        dataService.resumeSync();
        if (pd!=null) pd.dismiss();
        finish();
    }

    @Override
    public void onStatusUpdate(String message) {
        pd.setMessage(message);
    }

    @Override
    public void onComplete(Map<Integer, LittlePerson> tree) {
        if (pd!=null) pd.dismiss();
        finish();
    }
}



