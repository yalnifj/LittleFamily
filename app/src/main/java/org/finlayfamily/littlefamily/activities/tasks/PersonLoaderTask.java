package org.finlayfamily.littlefamily.activities.tasks;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import org.finlayfamily.littlefamily.data.DataHelper;
import org.finlayfamily.littlefamily.data.DataService;
import org.finlayfamily.littlefamily.data.LittlePerson;
import org.finlayfamily.littlefamily.familysearch.FamilySearchException;
import org.finlayfamily.littlefamily.familysearch.FamilySearchService;
import org.gedcomx.conclusion.Person;
import org.gedcomx.conclusion.Relationship;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by jfinlay on 1/12/2015.
 */
public class PersonLoaderTask extends AsyncTask<Integer, Integer, LittlePerson> {
    private Listener listener;
    private Context context;
    private DataService dataService;

    public PersonLoaderTask(Listener listener, Context context) {
        this.listener = listener;
        this.context = context;
        dataService = DataService.getInstance();
        dataService.setContext(context);
    }

    @Override
    protected LittlePerson doInBackground(Integer[] id) {
        Log.d(this.getClass().getSimpleName(), "Starting PersonLoaderTask.doInBackground "+id);
        LittlePerson person = null;
        try {
            if (id==null || id.length==0) {
                person = dataService.getDefaultPerson();
            } else {
                person = dataService.getPersonById(id[0]);
            }
        } catch(Exception e) {
            Log.e(this.getClass().getSimpleName(), "error", e);
        }
        return person;
    }

    @Override
    protected void onPostExecute(LittlePerson person) {
        if (listener!=null) {
            listener.onComplete(person);
        }
    }

    public interface Listener {
        public void onComplete(LittlePerson person);
    }
}