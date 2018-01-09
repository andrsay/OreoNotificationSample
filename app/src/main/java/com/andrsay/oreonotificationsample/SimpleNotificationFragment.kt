package com.andrsay.oreonotificationsample


import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.app.NotificationCompat
import android.text.TextUtils
import android.view.*
import android.widget.*
import yuku.ambilwarna.AmbilWarnaDialog


/**
 * A fragment shows how to create a simple notification.
 */
class SimpleNotificationFragment : Fragment(), MainActivity.OnChannelChangedListener {

    private var mChannelView: Spinner? = null
    private var mChannelPropView: TextView? = null
    private var mNotificationManager: NotificationManager? = null
    private var mChannelChangedRegister: OnChannelListenerRegister? = null
    private var mImportanceArray: Array<String>? = null
    private var mChosenBackgroundColor: Int = 0
    override fun onAttach(context: Context?) {
        super.onAttach(context)
        mImportanceArray = resources.getStringArray(R.array.channel_importance)
        mNotificationManager = context?.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        mChannelChangedRegister = context as OnChannelListenerRegister?
        mChannelChangedRegister?.registerListener(this)
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        setHasOptionsMenu(true)
        return inflater?.inflate(R.layout.fragment_simple_notification, container, false)
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mChannelView = view?.findViewById(R.id.channel_spinner)
        // Init channels dropdown data.
        mChannelView?.adapter = ChannelSpinnerAdapter(context, android.R.layout.simple_dropdown_item_1line)
        // Show channel properties when channel's spinner selection changed.
        mChannelView?.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View, position: Int, id: Long) {
                val channel = mChannelView?.selectedItem as NotificationChannel
                mChannelPropView?.text = getString(R.string.channel_prop_exp,
                        channel.name, if(channel.importance >0 )mImportanceArray?.get(channel.importance - 1) else "无"
                        , if(channel.shouldShowLights()) getString(R.string.has_light) else getString(R.string.no_light)
                        , if(channel.shouldVibrate()) getString(R.string.has_vibration) else getString(R.string.no_vibration))
            }
            override fun onNothingSelected(parent: AdapterView<*>) {

            }
        }
        mChannelPropView = view?.findViewById<TextView>(R.id.channel_prop_tv)
        val chooseBackgroundView = view?.findViewById<View>(R.id.choose_background_btn)
        chooseBackgroundView?.setOnClickListener{_ ->
            AmbilWarnaDialog(context, R.color.colorPrimary, object : AmbilWarnaDialog.OnAmbilWarnaListener {
                override fun onCancel(dialog: AmbilWarnaDialog) {
                    mChosenBackgroundColor = 0
                    chooseBackgroundView.setBackgroundColor(android.R.drawable.btn_default)
                }

                override fun onOk(dialog: AmbilWarnaDialog, color: Int) {
                    mChosenBackgroundColor = color
                    chooseBackgroundView.setBackgroundColor(color)
                }
            }).show()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater?) {
        menu?.addSubMenu(Menu.NONE, R.id.menu_submit_notification, Menu.NONE, R.string.submit)
        menu?.findItem(R.id.menu_submit_notification)?.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when(item?.itemId){
            R.id.menu_submit_notification -> {
                saveNotification()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun saveNotification() {
        // Validate parameters.
        val notificationTitleView = view?.findViewById<TextView>(R.id.title_et)
        if(TextUtils.isEmpty(notificationTitleView?.text)){
            notificationTitleView?.error = notificationTitleView?.hint
            return
        }
        val notificationTextView = view?.findViewById<TextView>(R.id.content_et)
        if(TextUtils.isEmpty(notificationTextView?.text)){
            notificationTextView?.error = notificationTextView?.hint
            return
        }
        // In Oreo and higher, creating a notification require relate to a channel.
        val selectedChannel = mChannelView?.selectedItem as NotificationChannel?
        if(selectedChannel == null){
            Toast.makeText(context, R.string.please_choose_notification_channel, Toast.LENGTH_SHORT).show()
            return
        }
        // If selected channel already removed by notification manager, show a tip and refresh spinner.
        if(mNotificationManager?.notificationChannels?.firstOrNull{it.id == selectedChannel.id} == null){
            Toast.makeText(context, R.string.invalid_notification_channel, Toast.LENGTH_SHORT).show()
            (mChannelView?.adapter as BaseAdapter?)?.notifyDataSetChanged()
            return
        }
        // Create a notification builder.
        val builder = NotificationCompat.Builder(context, selectedChannel.id)
                .setSmallIcon(R.drawable.ic_notification_logo)
                .setContentTitle(notificationTitleView?.text.toString())
                .setContentText(notificationTextView?.text.toString())
                .addAction(R.drawable.ic_action_add_channel, getString(R.string.reply), null)
                .setColor(mChosenBackgroundColor)
        // If Showing message style notification, create some test data.
        val showMessageStyleView = view?.findViewById<CheckBox>(R.id.message_style_ck)
        if(showMessageStyleView != null && showMessageStyleView.isChecked){
            builder.setStyle(NotificationCompat.MessagingStyle(getString(R.string.test_display_name))
                    .setConversationTitle(getString(R.string.test_conversation_title))
                    .addMessage(getString(R.string.test_message_chat1), System.currentTimeMillis(), getString(R.string.test_sender))
                    .addMessage(getString(R.string.test_message_chat2), System.currentTimeMillis(), getString(R.string.test_sender)))
        }
        // If user has assigned notification background.Start a foreground service and pass the notification,
        // Then show the notification inside of the service.
        if(mChosenBackgroundColor != 0){
            builder.setColorized(true)
            context.startService(Intent(context, TestNotificationColorService:: class.java)
                    .putExtra(TestNotificationColorService.ARG_NOTIFICATION, builder.build()))
        }else{
            // Set a timeout for notification which is not ongoing.
            builder.setTimeoutAfter(TestNotificationColorService.DURATION_NOTIFICATION_DISMISS)
            mNotificationManager?.notify(1, builder.build())
        }
    }

    override fun onChannelChanged(channel: NotificationChannel) {
        mChannelPropView?.text = null
        mChannelView?.adapter = ChannelSpinnerAdapter(context, android.R.layout.simple_dropdown_item_1line)
    }
}
