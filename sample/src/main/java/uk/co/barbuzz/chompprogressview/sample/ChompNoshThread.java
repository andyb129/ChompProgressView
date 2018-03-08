package uk.co.barbuzz.chompprogressview.sample;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;

/**
 * Created by andy.barber on 08/03/2018.
 */

public class ChompNoshThread extends Thread {

    private static final int SLEEP_TIME = 80;
    private static final int MSG_FINISH = 10;
    private static final int MSG_PROGRESS = 11;
    private static final int PROGRESS_FINISHED = 101;
    private volatile boolean running = true;

    private ChompNoshListener chompNoshListener;

    public interface ChompNoshListener {
        void onChompNoshProgress(int progress);
        void onChompNoshFinished();
    }

    private Handler mHandlerThreadFinished = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(Message msg) {
            int progress = (int) msg.obj;
            switch (msg.what) {
                case MSG_PROGRESS:
                    chompNoshListener.onChompNoshProgress(progress);
                    break;
                case MSG_FINISH:
                    chompNoshListener.onChompNoshFinished();
                    break;
            }
        }
    };

    public ChompNoshThread(ChompNoshListener chompNoshListener) {
        this.chompNoshListener = chompNoshListener;
    }

    @Override
    public void run() {
        super.run();

        for (int i = 0; i < 100; i++) {
            if (running) {
                publishProgress(i);
                try {
                    Thread.sleep(SLEEP_TIME);
                } catch (InterruptedException e) {

                }
            } else {
                break;
            }
        }
        publishProgress(PROGRESS_FINISHED);
    }

    /**
     * stop the Thread from running
     */
    public void terminate() {
        running = false;
    }

    /**
     * send progress integer to main thread or say we are finished
     * @param progress
     */
    private void publishProgress(int progress) {
        switch (progress) {
            case PROGRESS_FINISHED:
                callBackToUiThread(MSG_FINISH, progress);
                break;
            default:
                callBackToUiThread(MSG_PROGRESS, progress);
        }
    }

    /**
     * send message to handler which will run on UI Thread
     * @param msgType
     * @param obj
     */
    private void callBackToUiThread(int msgType, Object obj) {
        Message msg = Message.obtain(mHandlerThreadFinished, msgType, obj);
        msg.sendToTarget();
    }
}
