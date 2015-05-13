package org.finlayfamily.littlefamily.activities.tasks;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import org.finlayfamily.littlefamily.data.DataHelper;
import org.finlayfamily.littlefamily.data.DataService;
import org.finlayfamily.littlefamily.remote.RemoteServiceSearchException;

/**
 * Created by jfinlay on 1/12/2015.
 */
public class FileDownloaderTask extends AsyncTask<String, Integer, String> {
    private Listener listener;
    private Context context;
    private String folderName;

    public FileDownloaderTask(String folderName, Listener listener, Context context) {

        this.folderName = folderName;
        this.listener = listener;
        this.context = context;
    }

    @Override
    protected String doInBackground(String[] params) {
        Log.d(this.getClass().getSimpleName(), "Starting FileDownloaderTask.doInBackground "+params);
        String imgPath = null;
        for (String href : params) {
            try {
                DataService service = DataService.getInstance();
                imgPath = DataHelper.downloadFile(href, folderName, DataHelper.lastPath(href), service.getRemoteService(), context);
            } catch (RemoteServiceSearchException e) {
                e.printStackTrace();
            }
        }
        return imgPath;
    }

    @Override
    protected void onPostExecute(String path) {
        if (listener!=null) {
            listener.onComplete(path);
        }
    }

    public interface Listener {
        public void onComplete(String photos);
    }
}