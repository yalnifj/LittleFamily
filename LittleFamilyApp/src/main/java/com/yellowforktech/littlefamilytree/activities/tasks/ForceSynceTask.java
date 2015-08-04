package com.yellowforktech.littlefamilytree.activities.tasks;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.yellowforktech.littlefamilytree.data.DataNetworkState;
import com.yellowforktech.littlefamilytree.data.DataNetworkStateListener;
import com.yellowforktech.littlefamilytree.data.DataService;
import com.yellowforktech.littlefamilytree.data.LittlePerson;

import java.util.List;

/**
 * Created by jfinlay on 1/12/2015.
 */
public class ForceSynceTask extends AsyncTask<LittlePerson, String, LittlePerson> implements DataNetworkStateListener {
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

                    statusUpdate("Syncing parents of "+person.getName());
                    List<LittlePerson> parents = dataService.getParents(person);

                    statusUpdate("Syncing spouses of "+person.getName());
                    List<LittlePerson> spouses = dataService.getSpouses(person);

                    statusUpdate("Syncing children of "+person.getName());
                    List<LittlePerson> children = dataService.getChildren(person);

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
    protected void onProgressUpdate(String... values) {
        super.onProgressUpdate(values);
        if (listener!=null) {
            listener.onStatusUpdate(values[0]);
        }
    }

    @Override
    protected void onPostExecute(LittlePerson person) {
        if (listener!=null) {
            listener.onComplete(person);
        }
    }

    @Override
    public void remoteStateChanged(DataNetworkState state) {

    }

    @Override
    public void statusUpdate(String status) {
        publishProgress(status);
    }

    public interface Listener {
        public void onComplete(LittlePerson person);
        public void onStatusUpdate(String message);
    }
}