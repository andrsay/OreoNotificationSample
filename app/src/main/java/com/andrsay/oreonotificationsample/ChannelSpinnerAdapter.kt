package com.andrsay.oreonotificationsample

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView

/**
 * An adapter for channel spinner view.
 * Created by zhanglei on 2018/1/7.
 */
class ChannelSpinnerAdapter (context: Context, resource: Int) : ArrayAdapter<NotificationChannel>(context, resource) {
    private var mNotificationManager: NotificationManager? = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager?

    override fun getCount(): Int {
        return mNotificationManager?.notificationChannels?.size!!
    }

    override fun getItem(position: Int): NotificationChannel? {
        return mNotificationManager?.notificationChannels?.get(position)
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val view =  super.getView(position, convertView, parent) as TextView
        val item = getItem(position)
        view.text = if(item != null){item.name}else{"无"}
        return view
    }

    override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view =  super.getDropDownView(position, convertView, parent) as TextView
        val item = getItem(position)
        view.text = if(item != null){item.name}else{"无"}
        return view
    }
}