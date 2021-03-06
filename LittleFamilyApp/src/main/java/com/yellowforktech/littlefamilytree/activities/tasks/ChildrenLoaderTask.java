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
public class ChildrenLoaderTask extends AsyncTask<LittlePerson, String, ArrayList<LittlePerson>> implements DataNetworkStateListener {
    private Listener listener;
    private Context context;
    private DataService dataService;
    private boolean includeSteps;

    public ChildrenLoaderTask(Listener listener, Context context, boolean includeSteps) {
        this.listener = listener;
        this.context = context;
        this.includeSteps = includeSteps;
        dataService = DataService.getInstance();
        dataService.setContext(context);
        dataService.registerNetworkStateListener(this);
    }

    @Override
    protected ArrayList<LittlePerson> doInBackground(LittlePerson[] persons) {
        Log.d(this.getClass().getSimpleName(), "Starting ChildrenLoaderTask.doInBackground "+persons);
        ArrayList<LittlePerson> familyMembers = new ArrayList<>();
        for (LittlePerson person : persons) {
            try {
                dataService.addToSyncQ(person, 2);
                List<LittlePerson> people = dataService.getChildren(person);
                if (people != null) {
                    if (familyMembers.size()==0) {
                        familyMembers.addAll(people);
                    } else {
                        if (!includeSteps) {
                            ArrayList<LittlePerson> union = new ArrayList<>();
                            for (LittlePerson child : people) {
                                if (familyMembers.contains(child)) union.add(child);
                            }
                            familyMembers = union;
                        } else {
                            for (LittlePerson child : people) {
                                if (!familyMembers.contains(child)) familyMembers.add(child);
                            }
                        }
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