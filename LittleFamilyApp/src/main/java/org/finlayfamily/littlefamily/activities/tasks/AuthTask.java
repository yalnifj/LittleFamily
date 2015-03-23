package org.finlayfamily.littlefamily.activities.tasks;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import org.finlayfamily.littlefamily.activities.FSLoginActivity;
import org.finlayfamily.littlefamily.data.LittlePerson;
import org.finlayfamily.littlefamily.familysearch.FSResult;
import org.finlayfamily.littlefamily.familysearch.FamilySearchException;
import org.finlayfamily.littlefamily.familysearch.FamilySearchService;
import org.gedcomx.conclusion.Person;

/**
 * Created by jfinlay on 2/19/2015.
 */
public class AuthTask extends AsyncTask<String, Integer, FSResult> {
    private Listener listener;

    public AuthTask(Listener listener) {
        this.listener = listener;
    }

    @Override
    protected FSResult doInBackground(String[] params) {
        Log.d(this.getClass().getSimpleName(), "Starting AuthTask.doInBackground "+params);
        FSResult result = null;
        FamilySearchService service = FamilySearchService.getInstance();
        try {
            if (params.length==2) {
                result = service.authenticate(params[0], params[1]);
            } else {
                result = service.authWithToken(params[0]);
            }
            //Person person = service.getCurrentPerson();

        } catch(FamilySearchException e) {
            Log.e(this.getClass().getSimpleName(), "error", e);
            result = new FSResult();
            result.setData(e.getLocalizedMessage());
        }
        return result;
    }
    public interface Listener {
        public void onComplete(FSResult result);
    }

    @Override
    protected void onPostExecute(FSResult response) {
        if (listener!=null) {
            listener.onComplete(response);
        }
    }
}