package uk.co.barbuzz.chompprogressview.sample;

import android.os.AsyncTask;

import uk.co.barbuzz.chompprogressview.ChompProgressImageView;

/**
 * Old school Async to update progress view gradually
 *
 */
public class ChompNoshTask extends AsyncTask<Boolean, Integer, Boolean> {

    private static final int SLEEP_TIME = 80;
    private final ChompProgressImageView mChompProgressImageView;
    private final OnTaskFinishedListener mOnTaskFinishedListener;

    public interface OnTaskFinishedListener {
        void onTaskFinished();
    }

    public ChompNoshTask(ChompProgressImageView chompProgressImageView,
                         OnTaskFinishedListener onTaskFinishedListener) {
        mOnTaskFinishedListener = onTaskFinishedListener;
        mChompProgressImageView = chompProgressImageView;
        mChompProgressImageView.setTotalNumberOfBitesTaken(0);
    }

    @Override
    protected void onCancelled() {
        super.onCancelled();
        mChompProgressImageView.removeBites();
        mOnTaskFinishedListener.onTaskFinished();
    }

    @Override
    protected void onProgressUpdate(Integer... values) {
        super.onProgressUpdate(values);
        if (values[0]==101) {
            mChompProgressImageView.removeBites();
            mOnTaskFinishedListener.onTaskFinished();
        } else {
            mChompProgressImageView.setChompProgress(values[0]);
        }
    }

    @Override
    protected Boolean doInBackground(Boolean... params) {
        for (int i = 0; i < 100; i++) {
            publishProgress(i);
            try {
                Thread.sleep(SLEEP_TIME);
            } catch (InterruptedException e) {

            }
        }
        //reset image and remove bites
        publishProgress(101);
        return true;
    }

}
