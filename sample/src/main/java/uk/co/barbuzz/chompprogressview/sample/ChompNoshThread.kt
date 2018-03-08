package uk.co.barbuzz.chompprogressview.sample

import android.os.Handler
import android.os.Looper
import android.os.Message

/**
 * Created by andy.barber on 08/03/2018.
 */

class ChompNoshThread(private val chompNoshListener: ChompNoshListener) : Thread() {
    @Volatile
    private var running = true

    private val mHandlerThreadFinished = object : Handler(Looper.getMainLooper()) {
        override fun handleMessage(msg: Message) {
            val progress = msg.obj as Int
            when (msg.what) {
                MSG_PROGRESS -> chompNoshListener.onChompNoshProgress(progress)
                MSG_FINISH -> chompNoshListener.onChompNoshFinished()
            }
        }
    }

    interface ChompNoshListener {
        fun onChompNoshProgress(progress: Int)
        fun onChompNoshFinished()
    }

    override fun run() {
        super.run()

        for (i in 0..99) {
            if (running) {
                publishProgress(i)
                try {
                    Thread.sleep(SLEEP_TIME.toLong())
                } catch (e: InterruptedException) {

                }

            } else {
                break
            }
        }
        publishProgress(PROGRESS_FINISHED)
    }

    /**
     * stop the Thread from running
     */
    fun terminate() {
        running = false
    }

    /**
     * send progress integer to main thread or say we are finished
     * @param progress
     */
    private fun publishProgress(progress: Int) {
        when (progress) {
            PROGRESS_FINISHED -> callBackToUiThread(MSG_FINISH, progress)
            else -> callBackToUiThread(MSG_PROGRESS, progress)
        }
    }

    /**
     * send message to handler which will run on UI Thread
     * @param msgType
     * @param obj
     */
    private fun callBackToUiThread(msgType: Int, obj: Any) {
        val msg = Message.obtain(mHandlerThreadFinished, msgType, obj)
        msg.sendToTarget()
    }

    companion object {

        private val SLEEP_TIME = 80
        private val MSG_FINISH = 10
        private val MSG_PROGRESS = 11
        private val PROGRESS_FINISHED = 101
    }
}
