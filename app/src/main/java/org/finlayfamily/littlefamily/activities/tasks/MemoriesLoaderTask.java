package org.finlayfamily.littlefamily.activities.tasks;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import org.finlayfamily.littlefamily.data.DataHelper;
import org.finlayfamily.littlefamily.data.DataService;
import org.finlayfamily.littlefamily.data.LittlePerson;
import org.finlayfamily.littlefamily.data.Media;
import org.finlayfamily.littlefamily.familysearch.FamilySearchException;
import org.finlayfamily.littlefamily.familysearch.FamilySearchService;
import org.gedcomx.conclusion.Person;
import org.gedcomx.conclusion.Relationship;
import org.gedcomx.links.Link;
import org.gedcomx.source.SourceDescription;

import java.util.ArrayList;
import java.util.List;

import javax.xml.transform.Source;

/**
 * Created by jfinlay on 1/12/2015.
 */
public class MemoriesLoaderTask extends AsyncTask<String, Integer, ArrayList<Media>> {
    private LittlePerson person;
    private Listener listener;
    private Context context;
    private DataService dataService;

    public MemoriesLoaderTask(LittlePerson person, Listener listener, Context context) {
        this.person = person;
        this.listener = listener;
        this.context = context;
        dataService = DataService.getInstance();
        dataService.setContext(context);
    }

    @Override
    protected ArrayList<Media> doInBackground(String[] params) {
        ArrayList<Media> mediaList = new ArrayList<>();
        try {
            mediaList.addAll(dataService.getMediaForPerson(person));
        } catch (Exception e) {
            Log.e(this.getClass().getSimpleName(), "error", e);
        }
        return mediaList;
    }

    @Override
    protected void onPostExecute(ArrayList<Media> photos) {
        if (listener!=null) {
            listener.onComplete(photos);
        }
    }

    public interface Listener {
        public void onComplete(ArrayList<Media> photos);
    }
}