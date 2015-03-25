package org.finlayfamily.littlefamily.activities.tasks;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import org.finlayfamily.littlefamily.data.DataService;
import org.finlayfamily.littlefamily.data.HeritagePath;
import org.finlayfamily.littlefamily.data.LittlePerson;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by jfinlay on 1/12/2015.
 */
public class HeritageCalculatorTask extends AsyncTask<LittlePerson, Integer, ArrayList<HeritagePath>> {
    private Listener listener;
    private Context context;
    private DataService dataService;

    public HeritageCalculatorTask(Listener listener, Context context) {
        this.listener = listener;
        this.context = context;
        dataService = DataService.getInstance();
        dataService.setContext(context);
    }

    @Override
    protected ArrayList<HeritagePath> doInBackground(LittlePerson[] persons) {
        Log.d(this.getClass().getSimpleName(), "Starting HeritageCalculatorTask.doInBackground "+persons);

        ArrayList<HeritagePath> returnPaths = new ArrayList<>();
        LinkedList<HeritagePath> paths = new LinkedList<>();
        LittlePerson person = persons[0];

        HeritagePath first = new HeritagePath();
        first.setPercent(1.0);
        first.setPlace(person.getBirthPlace());
        first.setTreePath(new ArrayList<LittlePerson>(1));
        first.getTreePath().add(person);
        paths.add(first);

        while(paths.size() > 0) {
            try {
                HeritagePath path = paths.removeFirst();
                List<LittlePerson> people = dataService.getParents(path.getTreePath().get(path.getTreePath().size()-1));
                if (people != null) {

                }
            } catch (Exception e) {
                Log.e(this.getClass().getSimpleName(), "error", e);
            }
        }
        return returnPaths;
    }

    @Override
    protected void onPostExecute(ArrayList<HeritagePath> paths) {
        if (listener!=null) {
            listener.onComplete(paths);
        }
    }

    public interface Listener {
        public void onComplete(ArrayList<HeritagePath> paths);
    }
}