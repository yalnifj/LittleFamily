package org.finlayfamily.littlefamily.activities.tasks;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import org.finlayfamily.littlefamily.data.DataService;
import org.finlayfamily.littlefamily.data.HeritagePath;
import org.finlayfamily.littlefamily.data.LittlePerson;
import org.finlayfamily.littlefamily.util.PlaceHelper;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by jfinlay on 1/12/2015.
 */
public class HeritageCalculatorTask extends AsyncTask<LittlePerson, Integer, ArrayList<HeritagePath>> {
    private static final int MAX_PATHS=13;
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

        String origin = PlaceHelper.getPlaceCountry(person.getBirthPlace());

        HeritagePath first = new HeritagePath();
        first.setPercent(1.0);
        first.setPlace(origin);
        first.setTreePath(new ArrayList<LittlePerson>(1));
        first.getTreePath().add(person);
        paths.add(first);

        while(paths.size() > 0) {
            try {
                HeritagePath path = paths.removeFirst();
                if (path.getTreePath().size()>=MAX_PATHS || (!path.getPlace().equals(origin) && !path.getPlace().equals(PlaceHelper.UNKNOWN))) {
                    returnPaths.add(path);
                }
                else {
                    //List<LittlePerson> parents = dataService.getParents(path.getTreePath().get(path.getTreePath().size() - 1));
                    //-- only use the database for speed
                    LittlePerson pathPerson = path.getTreePath().get(path.getTreePath().size() - 1);
                    List<LittlePerson> parents = dataService.getDBHelper().getParentsForPerson(pathPerson.getId());
                    if (parents != null && parents.size()>0) {
                        for (LittlePerson parent : parents) {
                            HeritagePath ppath = new HeritagePath();
                            ppath.setPercent(path.getPercent() / parents.size());
                            if (parent.getNationality()!=null) {
                                ppath.setPlace(parent.getNationality());
                            } else {
                                String place = PlaceHelper.getPlaceCountry(parent.getBirthPlace());
                                ppath.setPlace(place);
                            }
                            ppath.setTreePath(new ArrayList<LittlePerson>(path.getTreePath()));
                            ppath.getTreePath().add(parent);
                            paths.add(ppath);
                        }
                    } else {
                        //-- if we don't know if this person has parents, then sync them to pick up the parents next time
                        if (pathPerson.isHasParents()==null) {
                            dataService.addToSyncQ(pathPerson, 1);
                        }
                        returnPaths.add(path);
                    }
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