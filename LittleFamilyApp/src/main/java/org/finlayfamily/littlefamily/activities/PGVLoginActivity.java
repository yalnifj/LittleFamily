package org.finlayfamily.littlefamily.activities;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
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
import org.finlayfamily.littlefamily.activities.tasks.PgvVersionTask;
import org.finlayfamily.littlefamily.data.DataService;
import org.finlayfamily.littlefamily.data.LittlePerson;
import org.finlayfamily.littlefamily.remote.RemoteResult;
import org.finlayfamily.littlefamily.remote.phpgedview.PGVService;

import java.util.ArrayList;

/**
 * A login screen that offers login via email/password.
 */
public class PGVLoginActivity extends Activity implements AuthTask.Listener, PersonLoaderTask.Listener, FamilyLoaderTask.Listener, PgvVersionTask.Listener {
    private static final String PGV_DEFAULT_URL = "http://www.finlayfamily.org/genealogy/";
    private static final String PGV_DEFAULT_USER = "john";
    private static final String PGV_DEFAULT_PASS = "";

    // UI references.
    private AutoCompleteTextView mEmailView;
    private EditText mPasswordView;
    private EditText mPgvUrlView;
    private EditText mDefaultId;
    private ProgressDialog pd;
    private DataService dataService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pgvlogin);

        // Set up the login form.
        mPgvUrlView = (EditText) findViewById(R.id.pgv_url);
        mPgvUrlView.setText(PGV_DEFAULT_URL);

        mEmailView = (AutoCompleteTextView) findViewById(R.id.email);
        mEmailView.setText(PGV_DEFAULT_USER);

        mDefaultId = (EditText) findViewById(R.id.default_id);

        mPasswordView = (EditText) findViewById(R.id.password);
        mPasswordView.setText(PGV_DEFAULT_PASS);
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
                try {
                    String baseUrl = mPgvUrlView.getText().toString();
                    dataService.getDBHelper().saveProperty("PhpGedViewBaseUrl", baseUrl);
                    String defaultPersonId = mDefaultId.getText().toString();
                    if (defaultPersonId.isEmpty()) defaultPersonId = "I1";
                    dataService.getDBHelper().saveProperty("PhpGedViewDefaultPersonId", defaultPersonId);
                    PGVService remoteService = new PGVService(baseUrl, defaultPersonId);
                    dataService.setRemoteService("PhpGedView", remoteService);

                    PgvVersionTask task = new PgvVersionTask(PGVLoginActivity.this, remoteService);
                    task.execute();
                } catch (Exception e) {
                    Log.e("PGVLoginActivity", "Error saving property", e);
                }
            }
        });

        dataService = DataService.getInstance();
        dataService.setContext(this);
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
            pd = ProgressDialog.show(this, "Please wait...", "Logging into PhpGedView", true, false);
            Log.d(this.getClass().getSimpleName(), "Launching new AuthTask for user entered credentials username="+username);

            try {
                PGVService remoteService = (PGVService) dataService.getRemoteService();
                AuthTask task = new AuthTask(this, remoteService);
                task.execute(username, password);
            } catch (Exception e) {
                Log.e("PGVLoginActivity", "Error saving property", e);
            }
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
            Intent intent = new Intent();
            setResult(Activity.RESULT_OK, intent);
            try {
                dataService.getDBHelper().saveProperty("PhpGedViewToken", dataService.getRemoteService().getEncodedAuthToken());
            } catch (Exception e) {
                e.printStackTrace();
            }
            pd.setMessage("Loading person data from PhpGedView...");
            PersonLoaderTask task = new PersonLoaderTask(this, this);
            task.execute();
        }
        else {
            String message = "Username and password combination failed. ";
            if (response!=null) message = response.getData();
            Toast.makeText(PGVLoginActivity.this, message, Toast.LENGTH_LONG).show();
            setResult( Activity.RESULT_CANCELED, null );
        }
    }

    @Override
    public void onComplete(LittlePerson person) {
        pd.setMessage("Loading close family members from PhpGedView...");
        FamilyLoaderTask task = new FamilyLoaderTask(this, this);
        task.execute(person);
    }

    @Override
    public void onComplete(ArrayList<LittlePerson> familyMembers) {
        if (pd!=null) pd.dismiss();
        finish();
    }

    @Override
    public void onComplete(String version) {
        if (version==null) {
            Toast.makeText(this, "This URL is not a valid instance of PhpGedView.", Toast.LENGTH_LONG);
            mPgvUrlView.setError("This URL is not a valid instance of PhpGedView.");
        }
        else {
            attemptLogin();
        }
    }
}



