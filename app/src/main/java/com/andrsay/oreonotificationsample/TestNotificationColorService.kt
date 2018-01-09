package com.andrsay.oreonotificationsample

import android.app.Notification
import android.app.Service
import android.content.Intent
import android.os.Handler
import android.os.IBinder

class TestNotificationColorService : Service() {

    private var mHandler: Handler? = null
    companion object {
        val ARG_NOTIFICATION = "__notification__"
        val DURATION_NOTIFICATION_DISMISS = 5000L
    }

    override fun onCreate() {
        super.onCreate()
        mHandler = Handler()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val notification = intent?.getParcelableExtra<Notification>(ARG_NOTIFICATION)
//        if(notification != null){
//            startForeground(1, notification)
//            mHandler?.postDelayed(Runnable {
//                stopForeground(true)
//            }, TestNotificationColorService.DURATION_NOTIFICATION_DISMISS)
//        }
        startForeground(1, notification)
        return super.onStartCommand(intent, flags, startId)
    }

    override fun onBind(intent: Intent): IBinder? {
        // TODO: Return the communication channel to the service.
        throw UnsupportedOperationException("Not yet implemented")
    }

    override fun onDestroy() {
        super.onDestroy()
        if(mHandler != null){
            mHandler!!.removeCallbacksAndMessages(null)
            mHandler = null
        }
    }
}
