package com.yellowforktech.littlefamilytree.activities.tasks;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.yellowforktech.littlefamilytree.data.DataService;
import com.yellowforktech.littlefamilytree.data.LittlePerson;

/**
 * Created by jfinlay on 1/12/2015.
 */
public class ForceSynceTask extends AsyncTask<LittlePerson, Integer, LittlePerson> {
    private Listener listener;
    private Context context;
    private DataService dataService;

    public ForceSynceTask(Listener listener, Context context) {
        this.listener = listener;
        this.context = context;
        dataService = DataService.getInstance();
        dataService.setContext(context);
    }

    @Override
    protected LittlePerson doInBackground(LittlePerson[] persons) {
        Log.d(this.getClass().getSimpleName(), "Starting ForceSynceTask.doInBackground "+persons);
        LittlePerson person = null;
        try {
            for (LittlePerson p : persons) {
                try {
                    person = p;
                    dataService.syncPerson(person);
                } catch (Exception e) {
                    Log.e(this.getClass().getSimpleName(), "error", e);
                }
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