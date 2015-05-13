package org.finlayfamily.littlefamily.activities.tasks;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import org.finlayfamily.littlefamily.data.DataService;
import org.finlayfamily.littlefamily.data.LittlePerson;

/**
 * Created by jfinlay on 1/12/2015.
 */
public class PersonLoaderTask extends AsyncTask<Integer, Integer, LittlePerson> {
    private Listener listener;
    private Context context;
    private DataService dataService;

    public PersonLoaderTask(Listener listener, Context context) {
        this.listener = listener;
        this.context = context;
        dataService = DataService.getInstance();
        dataService.setContext(context);
    }

    @Override
    protected LittlePerson doInBackground(Integer[] id) {
        Log.d(this.getClass().getSimpleName(), "Starting PersonLoaderTask.doInBackground "+id);
        LittlePerson person = null;
        try {
            if (id==null || id.length==0) {
                person = dataService.getDefaultPerson();
            } else {
                person = dataService.getPersonById(id[0]);
            }
        } catch(Exception e) {
            Log.e(this.getClass().getSimpleName(), "error", e);
        }
        return person;
    }

    @Override
    protected void onPostExecute(LittlePerson person) {
        if (listener!=null) {
            listener.onComplete(person);
        }
    }

    public interface Listener {
        public void onComplete(LittlePerson person);
    }
}