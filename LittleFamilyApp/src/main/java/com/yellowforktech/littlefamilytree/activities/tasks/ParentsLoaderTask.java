package com.yellowforktech.littlefamilytree.activities.tasks;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.yellowforktech.littlefamilytree.data.DataService;
import com.yellowforktech.littlefamilytree.data.LittlePerson;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by jfinlay on 1/12/2015.
 */
public class ParentsLoaderTask extends AsyncTask<LittlePerson, Integer, ArrayList<LittlePerson>> {
    private Listener listener;
    private Context context;
    private DataService dataService;
    private boolean primary = false;

    public ParentsLoaderTask(Listener listener, Context context) {
        this.listener = listener;
        this.context = context;
        dataService = DataService.getInstance();
        dataService.setContext(context);
    }

    public ParentsLoaderTask(Listener listener, Context context, boolean isPrimary) {
        this.listener = listener;
        this.context = context;
        dataService = DataService.getInstance();
        dataService.setContext(context);
        this.primary = isPrimary;
    }

    @Override
    protected ArrayList<LittlePerson> doInBackground(LittlePerson[] persons) {
        Log.d(this.getClass().getSimpleName(), "Starting ParentsLoaderTask.doInBackground "+persons);
        ArrayList<LittlePerson> familyMembers = new ArrayList<>();
        for (LittlePerson person : persons) {
            try {
                List<LittlePerson> people = dataService.getParents(person);
                if (people != null) {
                    if (!primary) {
                        familyMembers.addAll(people);
                    } else {
                        List<LittlePerson> parents = dataService.getParentCouple(person, people.get(0));
                        if (parents != null && parents.size()>0) {
                            familyMembers.addAll(parents);
                        }
                    }
                }
            } catch (Exception e) {
                Log.e(this.getClass().getSimpleName(), "error", e);
            }
        }
        return familyMembers;
    }

    @Override
    protected void onPostExecute(ArrayList<LittlePerson> familyMembers) {
        if (listener!=null) {
            listener.onComplete(familyMembers);
        }
    }

    public interface Listener {
        public void onComplete(ArrayList<LittlePerson> family);
    }
}