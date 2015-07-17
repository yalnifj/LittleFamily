package org.finlayfamily.littlefamily.activities;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.finlayfamily.littlefamily.R;
import org.finlayfamily.littlefamily.activities.tasks.AuthTask;
import org.finlayfamily.littlefamily.activities.tasks.FamilyLoaderTask;
import org.finlayfamily.littlefamily.activities.tasks.PersonLoaderTask;
import org.finlayfamily.littlefamily.data.DataService;
import org.finlayfamily.littlefamily.data.LittlePerson;
import org.finlayfamily.littlefamily.remote.RemoteResult;
import org.finlayfamily.littlefamily.remote.familysearch.FamilySearchService;

import java.util.ArrayList;

/**
 * A login screen that offers login via email/password.
 */
public class FSLoginActivity extends Activity implements AuthTask.Listener, PersonLoaderTask.Listener, FamilyLoaderTask.Listener {
    private static final String FS_DEFAULT_USER = "tum000205905";
    private static final String FS_DEFAULT_PASS = "1234pass";

    // UI references.
    private AutoCompleteTextView mEmailView;
    private EditText mPasswordView;
    private ProgressDialog pd;
    private DataService dataService;
    private Intent intent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fslogin);

        dataService = DataService.getInstance();
        dataService.setContext(this);

        // Set up the login form.
        mEmailView = (AutoCompleteTextView) findViewById(R.id.email);
        try {
            String defaultUser = dataService.getDBHelper().getProperty(DataService.SERVICE_USERNAME);
            if (defaultUser==null) defaultUser = FS_DEFAULT_USER;
            mEmailView.setText(defaultUser);
        } catch (Exception e) {
            e.printStackTrace();
        }

        mPasswordView = (EditText) findViewById(R.id.password);
        mPasswordView.setText(FS_DEFAULT_PASS);
        mPasswordView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
                if (id == R.id.login || id == EditorInfo.IME_NULL) {
                    attemptLogin();
                    return true;
                }
                return false;
            }
        });

        Button mEmailSignInButton = (Button) findViewById(R.id.email_sign_in_button);
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
            mPasswordView.setError(getString(R.string.error_invalid_password));
            focusView = mPasswordView;
            cancel = true;
        }

        // Check for a valid email address.
        if (TextUtils.isEmpty(username)) {
            mEmailView.setError(getString(R.string.error_field_required));
            focusView = mEmailView;
            cancel = true;
        } else if (!isEmailValid(username)) {
            mEmailView.setError(getString(R.string.error_invalid_email));
            focusView = mEmailView;
            cancel = true;
        }

        if (cancel) {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            focusView.requestFocus();
        } else {
            pd = ProgressDialog.show(this, "Please wait...", "Logging into FamilySearch", true, false);
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
                username = null;
                password = null;
            } catch (Exception e) {
                Log.e("FSLoginActivity", "Error saving property", e);
            }
            pd.setMessage("Loading person data from FamilySearch...");
            intent = new Intent();
            PersonLoaderTask task = new PersonLoaderTask(this, this);
            task.execute();
        }
        else {
            String message = "Username and password combination failed. ";
            if (response!=null) message = response.getData();
            Toast.makeText(FSLoginActivity.this, message, Toast.LENGTH_LONG).show();
            setResult( Activity.RESULT_CANCELED, null );
        }
    }

    @Override
    public void onComplete(LittlePerson person) {
        pd.setMessage("Loading close family members from FamilySearch...");
        intent.putExtra(ChooseFamilyMember.SELECTED_PERSON, person);
        FamilyLoaderTask task = new FamilyLoaderTask(this, this);
        task.execute(person);
    }

    @Override
    public void onComplete(ArrayList<LittlePerson> familyMembers) {
        if (pd!=null) pd.dismiss();
        intent.putExtra(ChooseFamilyMember.FAMILY, familyMembers);
        setResult(Activity.RESULT_OK, intent);
        finish();
    }

    @Override
    public void onStatusUpdate(String message) {
        pd.setMessage(message);
    }
}



