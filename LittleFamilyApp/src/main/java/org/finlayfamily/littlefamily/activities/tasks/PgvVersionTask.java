package org.finlayfamily.littlefamily.activities.tasks;

import android.os.AsyncTask;
import android.util.Log;

import org.finlayfamily.littlefamily.remote.RemoteServiceSearchException;
import org.finlayfamily.littlefamily.remote.phpgedview.PGVService;

public class PgvVersionTask extends AsyncTask<String, Integer, String> {
    private Listener listener;
    private PGVService service;

    public PgvVersionTask(Listener listener, PGVService service) {
        this.listener = listener;
        this.service = service;
    }

    @Override
    protected String doInBackground(String[] params) {
        Log.d(this.getClass().getSimpleName(), "Starting PgvVersionTask.doInBackground "+params);
        String result = null;
        try {
            result = service.getVersion();
        } catch(RemoteServiceSearchException e) {
            Log.e(this.getClass().getSimpleName(), "error", e);
        }
        return result;
    }
    public interface Listener {
        public void onComplete(String result);
    }

    @Override
    protected void onPostExecute(String response) {
        if (listener!=null) {
            listener.onComplete(response);
        }
    }
}