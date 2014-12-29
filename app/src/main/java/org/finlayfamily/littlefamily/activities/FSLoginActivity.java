package org.finlayfamily.littlefamily.activities;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.LoaderManager.LoaderCallbacks;
import android.content.Intent;
import android.content.Loader;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import net.neilgoodman.loader.RESTLoader;
import net.neilgoodman.loader.RESTResponse;

import org.familysearch.identity.Identity;
import org.finlayfamily.littlefamily.R;
import org.simpleframework.xml.Serializer;
import org.simpleframework.xml.core.Persister;

import java.util.List;

/**
 * A login screen that offers login via email/password.
 */
public class FSLoginActivity extends Activity implements LoaderCallbacks<RESTResponse> {
    private static final int LOADER_LOGIN = 0x1;

    protected static final String SESSION_ID = "sessionid";
    private static final String FS_IDENTITY_PATH = "https://sandbox.familysearch.org/identity/v2/login";
    private static final String FS_APP_KEY = "a0T3000000BM5hcEAD";
    private static final String FS_DEFAULT_USER = "tum000205905";
    private static final String FS_DEFAULT_PASS = "1234pass";

    // UI references.
    private AutoCompleteTextView mEmailView;
    private EditText mPasswordView;
    private View mProgressView;
    private View mLoginFormView;

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
        mProgressView = findViewById(R.id.login_progress);
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
            // Show a progress spinner, and kick off a background task to
            // perform the user login attempt.
            Uri uri = Uri.parse(FS_IDENTITY_PATH);

            Bundle params = new Bundle();
            params.putString("key", FS_APP_KEY);
            Bundle headers = new Bundle();
            headers.putString("Authorization", "Basic " + Base64.encodeToString( (username + ":" + password).getBytes(), Base64.NO_WRAP ));

            Bundle args = new Bundle();
            args.putSerializable(RESTLoader.ARGS_VERB, RESTLoader.HTTPVerb.GET);
            args.putParcelable(RESTLoader.ARGS_URI, uri);
            args.putParcelable(RESTLoader.ARGS_PARAMS, params);
            args.putParcelable(RESTLoader.ARGS_HEADERS, headers);

            getLoaderManager().restartLoader( LOADER_LOGIN, args, this );
        }
    }

    private boolean isEmailValid(String email) {
        return email.length() > 1;
    }

    private boolean isPasswordValid(String password) {
        return password.length() > 1;
    }

    /**
     * Shows the progress UI and hides the login form.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    public void showProgress(final boolean show) {
        // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
        // for very easy animations. If available, use these APIs to fade-in
        // the progress spinner.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

            mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
            mLoginFormView.animate().setDuration(shortAnimTime).alpha(
                    show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
                }
            });

            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mProgressView.animate().setDuration(shortAnimTime).alpha(
                    show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
                }
            });
        } else {
            // The ViewPropertyAnimator APIs are not available, so simply show
            // and hide the relevant UI components.
            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
        }
    }

    @Override
    public Loader<RESTResponse> onCreateLoader(int i, Bundle bundle) {
        return RESTLoader.create( this, bundle );
    }

    @Override
    public void onLoadFinished(Loader<RESTResponse> loader, RESTResponse response) {
        int    code = response.getCode();
        String data = response.getData();

        Log.d( getLocalClassName(), "onLoadFinished(" + loader.getId() + ") " + code + ": " + data );

        if (loader.getId() == LOADER_LOGIN) {
            if (code == 200 && !data.equals("")) {
                Serializer serializer = new Persister();
                try {
                    Identity session = serializer.read(Identity.class, data);
                    String sessionId = session.getSession().id;
                    Log.i( getLocalClassName(), "session: " + sessionId );

                    Intent intent = new Intent();
                    intent.putExtra( SESSION_ID, sessionId );
                    setResult( Activity.RESULT_OK, intent );
                    finish();
                }
                catch (Exception e) {
                    Log.e( getLocalClassName(), "error", e );
                }
            }
            else {
                Toast.makeText(this, "Failed to login (" + code + "). Check your internet settings.", Toast.LENGTH_LONG).show();
                setResult( Activity.RESULT_CANCELED, null );
            }
        }
    }

    @Override
    public void onLoaderReset(Loader<RESTResponse> cursorLoader) {

    }
}



