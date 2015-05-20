package org.finlayfamily.littlefamily.activities.tasks;

import android.os.AsyncTask;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by kids on 5/19/15.
 */
public class WaitTask extends AsyncTask<Long, Integer, Integer> {

    private List<WaitTaskListener> listeners;

    public WaitTask(WaitTaskListener... listeners) {
        this.listeners = new ArrayList<>(listeners.length);
        for(WaitTaskListener l : listeners) this.listeners.add(l);
    }

    @Override
    protected Integer doInBackground(Long... params) {
        Long waittime = params[0];
        Long waitstep = waittime / 100;
        int c = 0;
        for(c=0; c<100; c++) {
            try {
                Thread.sleep(waitstep);
                this.publishProgress(c);
            } catch (Exception e) { }
        }
        return c;
    }

    @Override
    protected void onProgressUpdate(Integer... values) {
        super.onProgressUpdate(values);
        for(WaitTaskListener l : listeners) {
            l.onProgressUpdate(values[0]);
        }
    }

    @Override
    protected void onPostExecute(Integer integer) {
        super.onPostExecute(integer);
        for(WaitTaskListener l : listeners) {
            l.onComplete(integer);
        }
    }

    public interface WaitTaskListener {
        public void onProgressUpdate(Integer progress);
        public void onComplete(Integer progress);
    }
}
