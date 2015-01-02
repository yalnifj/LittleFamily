package org.finlayfamily.littlefamily.activities;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
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
import org.finlayfamily.littlefamily.familysearch.FSResult;
import org.finlayfamily.littlefamily.familysearch.FamilySearchException;
import org.finlayfamily.littlefamily.familysearch.FamilySearchService;

/**
 * A login screen that offers login via email/password.
 */
public class FSLoginActivity extends Activity {
    private static final int LOADER_LOGIN = 0x1;

    private static final String FS_DEFAULT_USER = "tum000205905";
    private static final String FS_DEFAULT_PASS = "1234pass";

    // UI references.
    private AutoCompleteTextView mEmailView;
    private EditText mPasswordView;
    private View mLoginFormView;
    private ProgressDialog pd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fslogin);

        // Set up the login form.
        mEmailView = (AutoCompleteTextView) findViewById(R.id.email);
        mEmailView.setText(FS_DEFAULT_USER);

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

        mLoginFormView = findViewById(R.id.login_form);
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
            AuthTask task = new AuthTask();
            task.execute(username, password);

            /*
            Bundle args = new Bundle();
            args.putSerializable(AuthLoader.ARG_USERNAME, username);
            args.putSerializable(AuthLoader.ARG_PASSWORD, password);
            getLoaderManager().restartLoader(LOADER_LOGIN, args, this);
            FamilySearchService service = FamilySearchService.getInstance();
            try {
                RESTResult result = service.authenticate(username, password);
            } catch (Exception e) {
                Toast.makeText(this, e.getLocalizedMessage(), Toast.LENGTH_LONG).show();
            }
            */
        }
    }

    private boolean isEmailValid(String email) {
        return email.length() > 1;
    }

    private boolean isPasswordValid(String password) {
        return password.length() > 1;
    }

    public class AuthTask extends AsyncTask<String, Integer, FSResult> {
        @Override
        protected FSResult doInBackground(String[] params) {
            FSResult result = null;
            FamilySearchService service = FamilySearchService.getInstance();
            try {
                result = service.authenticate(params[0], params[1]);
                service.getCurrentPerson();
            } catch(FamilySearchException e) {
                Log.e(this.getClass().getSimpleName(), "error", e);
                result = new FSResult();
                result.setData(e.getLocalizedMessage());
            }
            return result;
        }

        @Override
        protected void onPostExecute(FSResult response) {
            if (pd!=null) pd.dismiss();
            if (response!=null && response.isSuccess()) {
                Intent intent = new Intent();
                setResult(Activity.RESULT_OK, intent);
                finish();
            }
            else {
                String message = "Username and password combination failed. ";
                if (response!=null) message = response.getData();
                Toast.makeText(FSLoginActivity.this, message, Toast.LENGTH_LONG).show();
                setResult( Activity.RESULT_CANCELED, null );
            }
        }
    }
}



