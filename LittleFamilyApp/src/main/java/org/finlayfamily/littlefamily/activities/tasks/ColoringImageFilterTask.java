package org.finlayfamily.littlefamily.activities.tasks;

import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.util.Log;

import com.jabistudio.androidjhlabs.filter.EdgeFilter;
import com.jabistudio.androidjhlabs.filter.util.AndroidUtils;

import org.finlayfamily.littlefamily.views.AlphaFilter;

/**
 * Created by jfinlay on 3/23/2015.
 */
public class ColoringImageFilterTask extends AsyncTask<Bitmap, Integer, Bitmap> {

    private Listener listener;

    public ColoringImageFilterTask(Listener listener) {
        this.listener = listener;
    }

    @Override
    protected Bitmap doInBackground(Bitmap... params) {
        Log.d(this.getClass().getSimpleName(), "Starting ColoringImageFilterTask.doInBackground "+params);
        EdgeFilter filter = new EdgeFilter();
        AlphaFilter alphaFilter = new AlphaFilter();
        Bitmap orig = params[0];
        //-- scale down
        Bitmap bm = Bitmap.createScaledBitmap(orig, Math.min(400, orig.getWidth()), Math.min(400, orig.getHeight()), true);
        int[] src = AndroidUtils.bitmapToIntArray(bm);
        long starttime = System.currentTimeMillis();
        int[] dst = filter.filter(src, bm.getWidth(), bm.getHeight());
        long endtime = System.currentTimeMillis();
        Log.d(this.getClass().getSimpleName(), "EdgeFilter ("+bm.getWidth()+","+bm.getHeight()+") took " + (endtime - starttime) + "ms");
        dst = alphaFilter.filter(dst, bm.getWidth(), bm.getHeight());
        long endtime2 = System.currentTimeMillis();
        Log.d(this.getClass().getSimpleName(), "AlphaFilter took "+ (endtime2-endtime)+"ms");
        Bitmap outlineBitmap = Bitmap.createBitmap(dst, bm.getWidth(), bm.getHeight(), Bitmap.Config.ARGB_8888);
        //-- scale back up
        outlineBitmap = Bitmap.createScaledBitmap(outlineBitmap, orig.getWidth(), orig.getHeight(), true);
        return outlineBitmap;
    }

    public interface Listener {
        public void onComplete(Bitmap result);
    }

    @Override
    protected void onPostExecute(Bitmap response) {
        if (listener!=null) {
            listener.onComplete(response);
        }
    }
}
