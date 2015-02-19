package org.finlayfamily.littlefamily.activities.tasks;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import org.finlayfamily.littlefamily.data.DataService;
import org.finlayfamily.littlefamily.data.LittlePerson;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by jfinlay on 1/12/2015.
 */
public class FamilyLoaderTask extends AsyncTask<String, Integer, ArrayList<LittlePerson>> {
    private LittlePerson person;
    private Listener listener;
    private Context context;
    private DataService dataService;

    public FamilyLoaderTask(LittlePerson person, Listener listener, Context context) {
        this.person = person;
        this.listener = listener;
        this.context = context;
        dataService = DataService.getInstance();
        dataService.setContext(context);
    }

    @Override
    protected ArrayList<LittlePerson> doInBackground(String[] params) {
        ArrayList<LittlePerson> familyMembers = new ArrayList<>();
        try {
            List<LittlePerson> people = dataService.getFamilyMembers(person);
            if (people!=null) {
                familyMembers.addAll(people);
            }
        } catch(Exception e) {
            Log.e(this.getClass().getSimpleName(), "error", e);
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