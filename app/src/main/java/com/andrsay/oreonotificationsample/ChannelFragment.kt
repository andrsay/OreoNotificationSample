package com.andrsay.oreonotificationsample


import android.app.Activity
import android.app.AlertDialog
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.res.ColorStateList
import android.os.Bundle
import android.provider.Settings
import android.support.v4.app.Fragment
import android.support.v7.widget.CardView
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.*
import android.widget.TextView


/**
 * Fragment to list all notification channel created.Also, you can delete or update the
 * notification inside of list.
 * @see SimpleNotificationFragment
 * @author lei.zhang
 */
class ChannelFragment : Fragment() {
    /**
     * Request code for creating notification channel.
     * @see navigateToCreateChannel
     */
    companion object {
        private val REQUEST_CREATE_CHANNEL: Int = 0xc001
    }
    private var mRecyclerView: RecyclerView? = null
    private var mNotificationManager: NotificationManager? = null
    private var mOperationCallback: OnChannelOperateCallback? = null
    override fun onAttach(context: Context?) {
        super.onAttach(context)
        mNotificationManager = context?.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        mOperationCallback = context as OnChannelOperateCallback?
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        // Set options menu visible.
        setHasOptionsMenu(true)
        return inflater?.inflate(R.layout.fragment_channel, container, false)
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mRecyclerView = view?.findViewById<RecyclerView>(R.id.recycler_view)
        mRecyclerView?.layoutManager = LinearLayoutManager(context)
        mRecyclerView?.adapter = LocalAdapter()
        checkDataEmpty()
    }

    override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater?) {
        super.onCreateOptionsMenu(menu, inflater)
        /**Create a menu to add a notification channel.*/
        menu?.addSubMenu(Menu.NONE, R.id.menu_add_notification_channel, Menu.NONE, "")
        menu?.findItem(R.id.menu_add_notification_channel)?.setIcon(R.drawable.ic_action_add_channel)
        menu?.findItem(R.id.menu_add_notification_channel)?.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when(item?.itemId){
            /**Process the add-channel click event.*/
            R.id.menu_add_notification_channel -> {
                navigateToCreateChannel()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }
    /**
     * Start activity to create channel {@link }, this is generally invoked
     * when user clicked add-channel menu.
     * @see CreateChannelActivity
     */
    private fun navigateToCreateChannel(){
        startActivityForResult(Intent(context, CreateChannelActivity ::class.java), REQUEST_CREATE_CHANNEL)
    }

    /**Delete notification channel by given channel*/
    private fun deleteChannel(channel: NotificationChannel) {
        mNotificationManager?.deleteNotificationChannel(channel.id)
        checkDataEmpty()
        mOperationCallback?.onDelete(channel)
    }

    /**Delete whether should show empty view depend on list data.*/
    private fun checkDataEmpty(){
        val emptyView = view?.findViewById<View>(R.id.empty_tv)
        val hasData = mNotificationManager?.notificationChannels?.isNotEmpty()
        mRecyclerView?.visibility = if(hasData != null && hasData) View.VISIBLE else View.GONE
        emptyView?.visibility = if(hasData != null && hasData) View.GONE else View.VISIBLE
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(Activity.RESULT_OK == resultCode){
            // Get the channel created and reflect it to list.
            if(REQUEST_CREATE_CHANNEL == requestCode){
                val resultChannel = data?.getParcelableExtra<NotificationChannel>(CreateChannelActivity.REQUEST_RETURN_KEY)
                if(resultChannel != null){
                    mRecyclerView?.adapter?.notifyItemInserted(mNotificationManager?.notificationChannels?.size!! - 1)
                    checkDataEmpty()
                    mOperationCallback?.onSave(resultChannel)
                }
            }
        }
    }

    internal inner class LocalAdapter : RecyclerView.Adapter<LocalAdapter.ViewHolder>() {
        private var importanceArray: Array<String> = resources.getStringArray(R.array.channel_importance)
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder? {
            return ViewHolder(LayoutInflater.from(context).inflate(R.layout.listitem_notification_channel, parent, false))
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val item = mNotificationManager?.notificationChannels!![position]
            holder.idView?.text = getString(R.string.channel_id_with_colon_exp, item.id)
            holder.nameView?.text = getString(R.string.channel_name_with_colon_exp, item.name)
            // Convert importance value to its meaning.
            if(item.importance >= NotificationManager.IMPORTANCE_MIN && item.importance <= NotificationManager.IMPORTANCE_HIGH){
                holder.importanceView?.text = getString(R.string.importance_with_colon_exp, importanceArray[item.importance - 1])
            }
            holder.groupView?.text = getString(R.string.group_with_colon_exp, mNotificationManager?.notificationChannelGroups?.firstOrNull {  it.id == item.group}?.name)
            holder.deleteView?.setOnClickListener{
                AlertDialog.Builder(context).setMessage(R.string.confirm_to_delete_notification)
                        .setNegativeButton(R.string.cancel, null)
                        .setPositiveButton(R.string.ok, DialogInterface.OnClickListener{ _, _ ->
                            deleteChannel(item)
                            mRecyclerView?.adapter?.notifyItemRemoved(position)
                        }).show()
            }
            // Set the item background to light color.
//            Log.i("####", "$item.lightColor")
            val cardView = holder.itemView as CardView?
            cardView?.cardBackgroundColor = ColorStateList.valueOf(item.lightColor)
            // Click item navigate to special channel settings.
            holder.itemView.setOnClickListener{_ ->
                startActivity(Intent(Settings.ACTION_CHANNEL_NOTIFICATION_SETTINGS)
                        .putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
                        .putExtra(Settings.EXTRA_CHANNEL_ID, item?.id))
            }
        }

        override fun getItemCount(): Int {
            return mNotificationManager?.notificationChannels?.size!!
        }

        internal inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView){
            var idView: TextView? = null
            var nameView: TextView? = null
            var importanceView: TextView? = null
            var groupView: TextView? = null
            var deleteView: View? = null
            init {
                idView = itemView.findViewById<TextView>(R.id.id_tv)
                nameView = itemView.findViewById<TextView>(R.id.name_tv)
                importanceView = itemView.findViewById<TextView>(R.id.importance_tv)
                groupView = itemView.findViewById<TextView>(R.id.group_tv)
                deleteView = itemView.findViewById<View>(R.id.delete_ib)
            }
        }
    }

    /**For others can listen channel changes.*/
    internal interface OnChannelOperateCallback {
        fun onSave(channel: NotificationChannel)
        fun onDelete(channel: NotificationChannel)
    }
}
