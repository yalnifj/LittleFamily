package com.yellowforktech.littlefamilytree.activities.tasks;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.yellowforktech.littlefamilytree.data.DataService;
import com.yellowforktech.littlefamilytree.data.HeritagePath;
import com.yellowforktech.littlefamilytree.data.LittlePerson;
import com.yellowforktech.littlefamilytree.util.PlaceHelper;

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

    public boolean canEndPath(HeritagePath path, String origin) {
        if (path.getTreePath().size()>=MAX_PATHS) return true;
        if (path.getPlace().equals(origin)) return false;
        if (path.getPlace().equals(PlaceHelper.UNKNOWN)) return false;
        if (path.getTreePath().size()<=2) return false;
        if (origin.equalsIgnoreCase("united states") && path.getPlace().equalsIgnoreCase("canada")) return false;
        if (origin.equalsIgnoreCase("canada") && path.getPlace().equalsIgnoreCase("united states")) return false;
        return true;
    }

    @Override
    protected ArrayList<HeritagePath> doInBackground(LittlePerson[] persons) {
        long starttime = System.currentTimeMillis();
        Log.d(this.getClass().getSimpleName(), "Starting HeritageCalculatorTask.doInBackground "+persons);

        ArrayList<HeritagePath> returnPaths = new ArrayList<>();
        LinkedList<HeritagePath> paths = new LinkedList<>();
        LittlePerson person = persons[0];
        if (person==null) return returnPaths;

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
                if (canEndPath(path, origin)) {
                    returnPaths.add(path);
                }
                else {
                    //List<LittlePerson> parents = dataService.getParents(path.getTreePath().get(path.getTreePath().size() - 1));
                    //-- only use the database for speed
                    LittlePerson pathPerson = path.getTreePath().get(path.getTreePath().size() - 1);
                    List<LittlePerson> parents = null;
					if (pathPerson.getTreeLevel() != null && pathPerson.getTreeLevel() < 4) {
						parents = dataService.getParents(pathPerson);
					} else {
						parents = dataService.getDBHelper().getParentsForPerson(pathPerson.getId());
					}
                    if (parents != null && parents.size()>0) {
                        for (LittlePerson parent : parents) {
                            HeritagePath ppath = new HeritagePath();
                            ppath.setPercent(path.getPercent() / parents.size());
                            //-- sometimes people use nationality as a note, try to ignore those
                            if (parent.getNationality()!=null && !parent.getNationality().trim().isEmpty() && parent.getNationality().length()<80) {
                                ppath.setPlace(parent.getNationality());
                            } else {
                                String place = PlaceHelper.getPlaceCountry(parent.getBirthPlace());
                                ppath.setPlace(place);
                            }
                            ppath.setTreePath(new ArrayList<LittlePerson>(path.getTreePath()));
                            ppath.getTreePath().add(parent);
                            paths.add(ppath);
                            if (origin.equals(PlaceHelper.UNKNOWN) && !ppath.getPlace().equals(PlaceHelper.UNKNOWN)) {
                                origin = ppath.getPlace();
                            }
                        }
                    } else {
                        //-- if we don't know if this person has parents, then sync them to pick up the parents next time
                        if (pathPerson.isHasParents()==null && path.getTreePath().size() < MAX_PATHS) {
                            dataService.addToSyncQ(pathPerson, Math.min(5, path.getTreePath().size()));
                        }
                        returnPaths.add(path);
                    }
                }
            } catch (Exception e) {
                Log.e(this.getClass().getSimpleName(), "error", e);
            }
        }
        for (HeritagePath path : returnPaths) {
            try {
                LittlePerson lastInPath = path.getTreePath().get(path.getTreePath().size()-1);
                if (lastInPath.isHasParents()==null && path.getTreePath().size()< 13) {
                    dataService.addToSyncQ(lastInPath, path.getTreePath().size());
                }
            } catch (Exception e) {
                Log.e(this.getClass().getSimpleName(), "error", e);
            }
        }
		//--dont finish before talking complete
        long diff = System.currentTimeMillis() - starttime;
        if (diff < 4000) {
            try {
                Thread.sleep(diff);
            } catch (InterruptedException e) {
                e.printStackTrace();
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
