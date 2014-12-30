package org.finlayfamily.littlefamily.activities.loaders;

import android.content.AsyncTaskLoader;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;

import org.finlayfamily.littlefamily.familysearch.FamilySearchException;
import org.finlayfamily.littlefamily.familysearch.FamilySearchService;

public class AuthenticationLoader extends AsyncTaskLoader<String> {
    public static final String ARG_USERNAME = "username";
    public static final String ARG_PASSWORD = "password";

    private String sessionId;
    private String username;
    private String password;
    public AuthenticationLoader(Context context, Bundle args) {
        super(context);
        username = args.getString(ARG_USERNAME);
        password = args.getString(ARG_PASSWORD);
    }

    @Override
    public String loadInBackground() {
        FamilySearchService service = FamilySearchService.getInstance();
        try {
            if (service.authenticate(username, password)) {
                sessionId = service.getSessionId();
                return sessionId;
            }
        } catch(FamilySearchException e) {
            Log.e(this.getClass().getSimpleName(), "error", e);
            return "ERROR: "+e.toString();
        }
        return null;
    }

    @Override
    public void deliverResult(String data) {
        // Here we cache our response.
        sessionId = data;
        super.deliverResult(data);
    }

    @Override
    protected void onStartLoading() {
        if (sessionId != null) {
            // We have a cached result, so we can just
            // return right away.
            super.deliverResult(sessionId);
        }
    }

    @Override
    protected void onStopLoading() {
        // This prevents the AsyncTask backing this
        // loader from completing if it is currently running.
        cancelLoad();
    }

    @Override
    protected void onReset() {
        super.onReset();

        // Stop the Loader if it is currently running.
        onStopLoading();

        // Get rid of our cache if it exists.
        sessionId = null;
    }
}
