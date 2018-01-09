package com.andrsay.oreonotificationsample

import android.app.Activity
import android.app.NotificationChannel
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.support.annotation.StringRes
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentPagerAdapter
import android.support.v4.view.ViewPager
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity

/**
 * Activity containing fragments to display the oreo features about notification.
 * Due to about oreo features, this application can only be run on android 8.0 and higher.
 * @author lei.zhang
 */
class MainActivity : AppCompatActivity() , ChannelFragment.OnChannelOperateCallback, OnChannelListenerRegister {

    companion object {
        val REQUEST_ACCESS_NOTIFICATION = 0xc001
    }
    private var mChannelChangedListeners: ArrayList<OnChannelChangedListener> = ArrayList<OnChannelChangedListener>()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        // Retrieve PagerTabStrip and ViewPager.
        val viewPager = findViewById<ViewPager>(R.id.view_pager)
        viewPager.adapter = LocalAdapter(supportFragmentManager)
        // Check if app has permission to access notification.If checked,start a service to listen notification removed.
        // Otherwise, navigation to settings to open the accessibility.
        if(checkNotificationAccessEnabled()){
            startService(Intent(this@MainActivity, NLService:: class.java))
        }else{
            AlertDialog.Builder(this@MainActivity).setMessage(R.string.tip_open_notification_access)
                    .setNegativeButton(R.string.cancel, null)
                    .setPositiveButton(getString(R.string.go_settings), DialogInterface.OnClickListener{_, _ ->
                        startActivityForResult(Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS), REQUEST_ACCESS_NOTIFICATION)
                    }).show()
        }
    }

    /**Check if app has permission to access notification.*/
    private fun checkNotificationAccessEnabled(): Boolean{
        val enabledListeners = Settings.Secure.getString(contentResolver,
                "enabled_notification_listeners")
        return enabledListeners.contains(packageName)
    }

    override fun onSave(channel: NotificationChannel) {
        for(listener in mChannelChangedListeners){
            listener.onChannelChanged(channel)
        }
    }

    override fun onDelete(channel: NotificationChannel) {
        for(listener in mChannelChangedListeners){
            listener.onChannelChanged(channel)
        }
    }

    override fun registerListener(listener: OnChannelChangedListener) {
        mChannelChangedListeners.add(listener)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(Activity.RESULT_OK == resultCode && REQUEST_ACCESS_NOTIFICATION == requestCode){
            if (checkNotificationAccessEnabled()){
                startService(Intent(this@MainActivity, NLService:: class.java))
            }
        }
    }

    internal inner class LocalAdapter(fm: FragmentManager) : FragmentPagerAdapter(fm){
        /**Titles string resource array.*/
        private @StringRes val titles = intArrayOf(R.string.notification_channel, R.string.simple_notification)
        override fun getItem(position: Int): Fragment? {
            when (position) {
                0 -> return ChannelFragment()
                1 -> return SimpleNotificationFragment()
            }
            throw IllegalArgumentException("Invalid position $position(total count : ${titles.size})")
        }

        override fun getPageTitle(position: Int): CharSequence {
            return getString(titles[position])
        }

        override fun getCount(): Int {
            return titles.size
        }
    }

    /**
     * @see OnChannelListenerRegister
     */
    interface OnChannelChangedListener{
        fun onChannelChanged(channel: NotificationChannel)
    }
}
