package org.finlayfamily.littlefamily.activities.tasks;

import android.os.AsyncTask;
import android.util.Log;

import org.finlayfamily.littlefamily.remote.RemoteResult;
import org.finlayfamily.littlefamily.remote.RemoteService;

/**
 * Created by jfinlay on 2/19/2015.
 */
public class AuthTask extends AsyncTask<String, Integer, RemoteResult> {
    private Listener listener;
    private RemoteService service;

    public AuthTask(Listener listener, RemoteService service) {
        this.listener = listener;
        this.service = service;
    }

    @Override
    protected RemoteResult doInBackground(String[] params) {
        Log.d(this.getClass().getSimpleName(), "Starting AuthTask.doInBackground "+params);
        RemoteResult result = null;
        try {
            if (params.length==2) {
                result = service.authenticate(params[0], params[1]);
            } else {
                result = service.authWithToken(params[0]);
            }
            //Person person = service.getCurrentPerson();

        } catch(Exception e) {
            Log.e(this.getClass().getSimpleName(), "error", e);
            result = new RemoteResult();
            result.setData(e.getLocalizedMessage());
        }
        return result;
    }
    public interface Listener {
        public void onComplete(RemoteResult result);
    }

    @Override
    protected void onPostExecute(RemoteResult response) {
        if (listener!=null) {
            listener.onComplete(response);
        }
    }
}