package com.andrsay.oreonotificationsample

/**
 * For child fragments to register a listener{@link MainActivity.OnChannelChangedListener} to its
 * activity.As a result, if the notification has been changed such as added or deleted, they can be
 * notified.
 * Created by zhanglei on 2018/1/7.
 */
interface OnChannelListenerRegister{
    fun registerListener(listener: MainActivity.OnChannelChangedListener)
}