package com.yellowforktech.littlefamilytree.activities.tasks;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.yellowforktech.littlefamilytree.data.DataNetworkState;
import com.yellowforktech.littlefamilytree.data.DataNetworkStateListener;
import com.yellowforktech.littlefamilytree.data.DataService;
import com.yellowforktech.littlefamilytree.data.LittlePerson;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by jfinlay on 1/12/2015.
 */
public class FamilyLoaderTask extends AsyncTask<LittlePerson, String, ArrayList<LittlePerson>> implements DataNetworkStateListener {
    private Listener listener;
    private Context context;
    private DataService dataService;

    public FamilyLoaderTask(Listener listener, Context context) {
        this.listener = listener;
        this.context = context;
        dataService = DataService.getInstance();
        dataService.setContext(context);
        dataService.registerNetworkStateListener(this);
    }

    @Override
    protected ArrayList<LittlePerson> doInBackground(LittlePerson[] persons) {
        Log.d(this.getClass().getSimpleName(), "Starting FamilyLoaderTask.doInBackground "+persons);
        ArrayList<LittlePerson> familyMembers = new ArrayList<>();
        for (LittlePerson person : persons) {
            try {
                List<LittlePerson> people = dataService.getFamilyMembers(person);
                if (people != null) {
                    for (LittlePerson p : people) {
                        familyMembers.add(p);
                        dataService.getMediaForPerson(p);
                        dataService.addToSyncQ(p, 1);
                    }
                }
            } catch (Exception e) {
                Log.e(this.getClass().getSimpleName(), "error", e);
            }
        }
        return familyMembers;
    }

    protected void onProgressUpdate(String... progress) {
        if (listener!=null) {
            listener.onStatusUpdate(progress[0]);
        }
    }


    @Override
    protected void onPostExecute(ArrayList<LittlePerson> familyMembers) {
        dataService.unregisterNetworkStateListener(this);
        if (listener!=null) {
            listener.onComplete(familyMembers);
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
        public void onComplete(ArrayList<LittlePerson> family);
        public void onStatusUpdate(String message);
    }
}