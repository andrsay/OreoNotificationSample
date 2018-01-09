package com.andrsay.oreonotificationsample

import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.util.Log



/**
 * Print logs while notification changed.
 * Created by zhanglei on 2018/1/9.
 *
 */
class NLService : NotificationListenerService(){

    companion object {
        val TAG = "NLService"
    }
    override fun onListenerConnected() {
        Log.i(TAG, "onListenerConnected")
        super.onListenerConnected()
    }

    override fun onListenerDisconnected() {
        Log.i(TAG, "onListenerDisconnected")
        super.onListenerDisconnected()
    }

    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        super.onNotificationPosted(sbn)
        if(packageName == sbn?.packageName)
            Log.i(TAG, "onNotificationPosted" + sbn.toString())
    }

    override fun onNotificationRemoved(sbn: StatusBarNotification?) {
        super.onNotificationRemoved(sbn)
        if(packageName == sbn?.packageName)
            Log.i(TAG, "onNotificationRemoved" + sbn.toString())
    }
}