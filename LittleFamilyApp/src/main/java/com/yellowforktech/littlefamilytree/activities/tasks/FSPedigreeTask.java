package com.yellowforktech.littlefamilytree.activities.tasks;

import android.os.AsyncTask;
import android.util.Log;

import com.yellowforktech.littlefamilytree.data.DataNetworkState;
import com.yellowforktech.littlefamilytree.data.DataNetworkStateListener;
import com.yellowforktech.littlefamilytree.data.DataService;
import com.yellowforktech.littlefamilytree.data.LittlePerson;
import com.yellowforktech.littlefamilytree.remote.familysearch.PedigreeHelper;

import java.util.Map;

/**
 * Created by jfinlay on 1/12/2015.
 */
public class FSPedigreeTask extends AsyncTask<LittlePerson, String, Map<Integer, LittlePerson>> implements DataNetworkStateListener {
    private Listener listener;
    private DataService dataService;

    public FSPedigreeTask(Listener listener) {
        this.listener = listener;
        dataService = DataService.getInstance();
        dataService.registerNetworkStateListener(this);
    }

    @Override
    protected Map<Integer, LittlePerson> doInBackground(LittlePerson[] persons) {
        Log.d(this.getClass().getSimpleName(), "Starting FSPedigreeTask.doInBackground " + persons);
        Map<Integer, LittlePerson> tree = null;
        for(LittlePerson person : persons) {
            try {
                tree = PedigreeHelper.getPedigree(person, dataService);
            } catch (Exception e) {
                Log.e(this.getClass().getSimpleName(), "error", e);
            }
        }
        return tree;
    }

    protected void onProgressUpdate(String... progress) {
        if (listener!=null) {
            listener.onStatusUpdate(progress[0]);
        }
    }

    @Override
    public void remoteStateChanged(DataNetworkState state) {
    }

    @Override
    public void statusUpdate(String status) {
        publishProgress(status);
    }

    @Override
    protected void onPostExecute(Map<Integer, LittlePerson> tree) {
        dataService.unregisterNetworkStateListener(this);
        if (listener!=null) {
            listener.onComplete(tree);
        }
    }

    public interface Listener {
        public void onComplete(Map<Integer, LittlePerson> tree);
        public void onStatusUpdate(String message);
    }
}