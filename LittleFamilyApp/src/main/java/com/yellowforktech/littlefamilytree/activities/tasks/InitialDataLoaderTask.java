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
public class InitialDataLoaderTask extends AsyncTask<LittlePerson, String, ArrayList<LittlePerson>> implements DataNetworkStateListener {
    private Listener listener;
    private Context context;
    private DataService dataService;

    public InitialDataLoaderTask(Listener listener, Context context) {
        this.listener = listener;
        this.context = context;
        dataService = DataService.getInstance();
        dataService.setContext(context);
        dataService.registerNetworkStateListener(this);
    }

    @Override
    protected ArrayList<LittlePerson> doInBackground(LittlePerson[] persons) {
        Log.d(this.getClass().getSimpleName(), "Starting InitialDataLoaderTask.doInBackground "+persons);
        ArrayList<LittlePerson> familyMembers = new ArrayList<>();
        for (LittlePerson person : persons) {
            try {
                List<LittlePerson> people = dataService.getFamilyMembers(person);
                if (people != null) {
                    familyMembers.addAll(people);
                }
            } catch (Exception e) {
                Log.e(this.getClass().getSimpleName(), "error", e);
            }
        }

        //-- grandparents
        List<LittlePerson> grandParents = new ArrayList<>();
        for(LittlePerson p : familyMembers) {
            try {
                List<LittlePerson> parents = dataService.getParents(p);
                for(LittlePerson parent : parents) {
                    if (!familyMembers.contains(parent)) {
                        grandParents.add(parent);
                    }
                }
            } catch (Exception e) {
                Log.e(this.getClass().getSimpleName(), "error", e);
            }
        }

        //-- grandchildren
        for(LittlePerson p : familyMembers) {
            try {
                List<LittlePerson> children = dataService.getChildren(p);
                dataService.addToSyncQ(children, 1);
            } catch (Exception e) {
                Log.e(this.getClass().getSimpleName(), "error", e);
            }
        }

        //-- great grandparents
        for(LittlePerson p : grandParents) {
            try {
                List<LittlePerson> parents = dataService.getParents(p);
                dataService.addToSyncQ(parents, 3);
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
        if (listener!=null) {
            listener.onComplete(familyMembers);
        }
    }

    @Override
    public void remoteStateChanged(DataNetworkState state) {
        //-- nothing to do
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