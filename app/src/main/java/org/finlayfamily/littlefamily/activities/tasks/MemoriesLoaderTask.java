package org.finlayfamily.littlefamily.activities.tasks;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import org.finlayfamily.littlefamily.data.DataHelper;
import org.finlayfamily.littlefamily.data.LittlePerson;
import org.finlayfamily.littlefamily.familysearch.FamilySearchException;
import org.finlayfamily.littlefamily.familysearch.FamilySearchService;
import org.gedcomx.conclusion.Person;
import org.gedcomx.conclusion.Relationship;
import org.gedcomx.links.Link;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by jfinlay on 1/12/2015.
 */
public class MemoriesLoaderTask extends AsyncTask<String, Integer, ArrayList<Link>> {
    private Person person;
    private Listener listener;
    private boolean download;
    private Context context;

    public MemoriesLoaderTask(Person person, boolean download, Listener listener, Context context) {
        this.person = person;
        this.listener = listener;
        this.context = context;
        this.download = download;
    }

    @Override
    protected ArrayList<Link> doInBackground(String[] params) {
        ArrayList<Link> photos = new ArrayList<>();
        FamilySearchService service = FamilySearchService.getInstance();
        try {
            List<Link> links = service.getPersonMemories(person.getId());
            photos.addAll(links);
            if (download){
                for(Link link : photos) {
                    DataHelper.downloadFile(link.getHref().toString(), person.getId(), DataHelper.lastPath(link.getHref().toString()), context);
                }
            }
        } catch(FamilySearchException e) {
            Log.e(this.getClass().getSimpleName(), "error", e);
            Toast.makeText(context, "Error communicating with FamilySearch. " + e, Toast.LENGTH_LONG).show();
        }
        return photos;
    }

    @Override
    protected void onPostExecute(ArrayList<Link> photos) {
        if (listener!=null) {
            listener.onComplete(photos);
        }
    }

    public interface Listener {
        public void onComplete(ArrayList<Link> photos);
    }
}