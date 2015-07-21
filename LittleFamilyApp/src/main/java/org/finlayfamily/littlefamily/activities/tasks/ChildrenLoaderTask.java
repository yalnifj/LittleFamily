package org.finlayfamily.littlefamily.activities.tasks;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import org.finlayfamily.littlefamily.data.DataNetworkState;
import org.finlayfamily.littlefamily.data.DataNetworkStateListener;
import org.finlayfamily.littlefamily.data.DataService;
import org.finlayfamily.littlefamily.data.LittlePerson;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by jfinlay on 1/12/2015.
 */
public class ChildrenLoaderTask extends AsyncTask<LittlePerson, String, ArrayList<LittlePerson>> implements DataNetworkStateListener {
    private Listener listener;
    private Context context;
    private DataService dataService;

    public ChildrenLoaderTask(Listener listener, Context context) {
        this.listener = listener;
        this.context = context;
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
                        ArrayList<LittlePerson> union = new ArrayList<>();
                        for(LittlePerson child : people) {
                            if (familyMembers.contains(child)) union.add(child);
                        }
                        familyMembers = union;
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