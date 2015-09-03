package com.yellowforktech.littlefamilytree.activities.tasks;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by jfinlay on 9/3/2015.
 */
public class BitmapSequenceLoader extends AsyncTask<List<Integer>, Integer, ArrayList<Bitmap>> {
    private Resources resources;
    private Listener listener;

    public BitmapSequenceLoader(Resources resources, Listener listener) {
        this.resources = resources;
        this.listener = listener;
    }

    @Override
    protected ArrayList<Bitmap> doInBackground(List<Integer>... params) {
        List<Integer> ids = params[0];
        ArrayList<Bitmap> bitmaps = new ArrayList<>(ids.size());
        for (Integer rid : ids) {
            bitmaps.add(BitmapFactory.decodeResource(resources, rid));
        }
        return bitmaps;
    }

    @Override
    protected void onPostExecute(ArrayList<Bitmap> bitmaps) {
        listener.onComplete(bitmaps);
    }

    public interface Listener {
        public void onComplete(ArrayList<Bitmap> bitmaps);
    }
}
