package com.yellowforktech.littlefamilytree.activities.tasks;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.yellowforktech.littlefamilytree.data.DataService;
import com.yellowforktech.littlefamilytree.data.LittlePerson;

import java.util.ArrayList;
import java.util.Map;

/**
 * Created by jfinlay on 1/12/2015.
 */
public class SearchLoaderTask extends AsyncTask<Map<String, String>, Integer, ArrayList<LittlePerson>> {
    private Listener listener;
    private Context context;
    private DataService dataService;

    public SearchLoaderTask(Listener listener, Context context) {
        this.listener = listener;
        this.context = context;
        dataService = DataService.getInstance();
        dataService.setContext(context);
    }

    @Override
    protected ArrayList<LittlePerson> doInBackground(Map<String, String>[] params) {
        Log.d(this.getClass().getSimpleName(), "Starting SearchLoaderTask.doInBackground "+params);
        ArrayList<LittlePerson> people = new ArrayList<>();
        Map<String, String> values = params[0];
        String remoteId = values.get("remoteId");
        if (remoteId!=null && !remoteId.isEmpty()) {
            try {
                LittlePerson person = dataService.getPersonByRemoteId(remoteId);
                people.add(person);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            String given = values.get("givenName");
            String surname = values.get("surname");
            if ((given!=null && !given.isEmpty()) || (surname!=null && !surname.isEmpty())) {
                try {
                    people = (ArrayList<LittlePerson>) dataService.searchPeople(given, surname);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        return people;
    }

    @Override
    protected void onPostExecute(ArrayList<LittlePerson> people) {
        if (listener!=null) {
            listener.onSearchComplete(people);
        }
    }

    public interface Listener {
        public void onSearchComplete(ArrayList<LittlePerson> people);
    }
}