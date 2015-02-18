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
public class PersonLoaderTask extends AsyncTask<String, Integer, LittlePerson> {
    private Integer id;
    private Listener listener;
    private Context context;
    private DataService dataService;

    public PersonLoaderTask(Integer id, Listener listener, Context context) {
        this.id = id;
        this.listener = listener;
        this.context = context;
        dataService = DataService.getInstance();
        dataService.setContext(context);
    }

    @Override
    protected LittlePerson doInBackground(String[] params) {
        LittlePerson person = null;
        try {
            if (id==null) {
                person = dataService.getDefaultPerson();
            } else {
                person = dataService.getPersonById(id);
            }
        } catch(Exception e) {
            Log.e(this.getClass().getSimpleName(), "error", e);
            Toast.makeText(context, e.getMessage(), Toast.LENGTH_LONG).show();
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