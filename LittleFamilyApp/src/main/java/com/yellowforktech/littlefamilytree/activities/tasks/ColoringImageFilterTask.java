package com.yellowforktech.littlefamilytree.activities.tasks;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.util.Log;

import com.yellowforktech.littlefamilytree.filters.GPUImageAlphaMaskFilter;
import com.yellowforktech.littlefamilytree.filters.GPUImageAlphaSobelEdgeDetection;

import jp.co.cyberagent.android.gpuimage.GPUImage;
import jp.co.cyberagent.android.gpuimage.GPUImageFilterGroup;

/**
 * Created by jfinlay on 3/23/2015.
 */
public class ColoringImageFilterTask extends AsyncTask<Bitmap, Integer, Bitmap> {

    private Listener listener;
    private Context context;

    public ColoringImageFilterTask(Listener listener, Context context) {
        this.listener = listener;
        this.context = context;
    }

    @Override
    protected Bitmap doInBackground(Bitmap... params) {
        Log.d(this.getClass().getSimpleName(), "Starting ColoringImageFilterTask.doInBackground " + params);
        GPUImageFilterGroup filterGroup = new GPUImageFilterGroup();
        filterGroup.addFilter(new GPUImageAlphaSobelEdgeDetection());
        filterGroup.addFilter(new GPUImageAlphaMaskFilter(0.7f, new float[]{0f, 0f, 0f}));

        Bitmap orig = params[0];
        Bitmap outlineBitmap = null;
        if (orig!=null && !orig.isRecycled()) {
            synchronized (orig) {
                long starttime = System.currentTimeMillis();
                GPUImage outlineImage = new GPUImage(context);
                outlineImage.setFilter(filterGroup);
                outlineBitmap = outlineImage.getBitmapWithFilterApplied(orig);
                long endtime = System.currentTimeMillis();
                Log.d(this.getClass().getSimpleName(), "Creating outline image took "+(endtime-starttime)+"ms");
            }
        }
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
