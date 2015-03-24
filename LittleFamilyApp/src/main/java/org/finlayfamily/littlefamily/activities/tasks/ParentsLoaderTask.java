package org.finlayfamily.littlefamily.activities.tasks;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import org.finlayfamily.littlefamily.data.DataService;
import org.finlayfamily.littlefamily.data.LittlePerson;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by jfinlay on 1/12/2015.
 */
public class ParentsLoaderTask extends AsyncTask<LittlePerson, Integer, ArrayList<LittlePerson>> {
    private Listener listener;
    private Context context;
    private DataService dataService;

    public ParentsLoaderTask(Listener listener, Context context) {
        this.listener = listener;
        this.context = context;
        dataService = DataService.getInstance();
        dataService.setContext(context);
    }

    @Override
    protected ArrayList<LittlePerson> doInBackground(LittlePerson[] persons) {
        Log.d(this.getClass().getSimpleName(), "Starting ParentsLoaderTask.doInBackground "+persons);
        ArrayList<LittlePerson> familyMembers = new ArrayList<>();
        for (LittlePerson person : persons) {
            try {
                List<LittlePerson> people = dataService.getParents(person);
                if (people != null) {
                    familyMembers.addAll(people);
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