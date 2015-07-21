package com.yellowforktech.littlefamilytree.activities.tasks;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.yellowforktech.littlefamilytree.data.DataService;
import com.yellowforktech.littlefamilytree.data.LittlePerson;
import com.yellowforktech.littlefamilytree.data.Media;

import java.util.ArrayList;

/**
 * Created by jfinlay on 1/12/2015.
 */
public class MemoriesLoaderTask extends AsyncTask<LittlePerson, Integer, ArrayList<Media>> {
    private Listener listener;
    private Context context;
    private DataService dataService;

    public MemoriesLoaderTask(Listener listener, Context context) {
        this.listener = listener;
        this.context = context;
        dataService = DataService.getInstance();
        dataService.setContext(context);
    }

    @Override
    protected ArrayList<Media> doInBackground(LittlePerson[] params) {
        Log.d(this.getClass().getSimpleName(), "Starting MemoriesLoaderTask.doInBackground "+params);
        ArrayList<Media> mediaList = new ArrayList<>();
        for(LittlePerson person : params) {
            try {
                mediaList.addAll(dataService.getMediaForPerson(person));
            } catch (Exception e) {
                Log.e(this.getClass().getSimpleName(), "error", e);
            }
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