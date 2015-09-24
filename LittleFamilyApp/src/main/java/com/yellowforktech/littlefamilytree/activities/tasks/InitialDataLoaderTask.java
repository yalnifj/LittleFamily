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
        Log.d(this.getClass().getSimpleName(), "Starting InitialDataLoaderTask.doInBackground " + persons);
        ArrayList<LittlePerson> familyMembers = new ArrayList<>();
        for (LittlePerson person : persons) {
            familyMembers.add(person);
            try {
                List<LittlePerson> people = dataService.getFamilyMembers(person);
                if (people != null) {
                    familyMembers.addAll(people);
                }
            } catch (Exception e) {
                Log.e(this.getClass().getSimpleName(), "error", e);
            }
        }

        //-- grandchildren
        List<LittlePerson> grandParents = new ArrayList<>();
        List<LittlePerson> grandChildren = new ArrayList<>();
        for(LittlePerson p : familyMembers) {
            try {
                List<LittlePerson> parents = dataService.getParents(p);
                if (parents!=null) {
                    for (LittlePerson parent : parents) {
                        if (!familyMembers.contains(parent) && !grandParents.contains(parent)) {
                            grandParents.add(parent);
                        }
                    }
                }
                List<LittlePerson> children = dataService.getChildren(p);
                if (children!=null) {
                    for (LittlePerson child : children) {
                        if (!familyMembers.contains(child) && !grandChildren.contains(child)) {
                            grandChildren.add(child);
                        }
                    }
                }
            } catch (Exception e) {
                Log.e(this.getClass().getSimpleName(), "error", e);
            }
        }

        familyMembers.addAll(grandChildren);
        for(LittlePerson p : grandChildren) {
            try {
                List<LittlePerson> children = dataService.getChildren(p);
                if (children!=null) {
                    for (LittlePerson child : children) {
                        if (!familyMembers.contains(child)) {
                            familyMembers.add(child);
                        }
                    }
                }
            } catch (Exception e) {
                Log.e(this.getClass().getSimpleName(), "error", e);
            }
        }

        for(LittlePerson p : grandParents) {
            try {
                if (familyMembers.size()<10) {
                    List<LittlePerson> parents = dataService.getParents(p);
                    dataService.addToSyncQ(parents, 3);
                }
                dataService.addToSyncQ(p, 2);
            } catch (Exception e) {
                Log.e(this.getClass().getSimpleName(), "error", e);
            }
        }
        try {
            dataService.addToSyncQ(familyMembers, 2);
        } catch (Exception e) {
            Log.e(this.getClass().getSimpleName(), "error", e);
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