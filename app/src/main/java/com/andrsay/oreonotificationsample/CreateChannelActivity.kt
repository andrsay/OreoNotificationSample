package com.andrsay.oreonotificationsample

import android.app.*
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.os.SystemClock
import android.support.annotation.ColorRes
import android.support.v7.app.AppCompatActivity
import android.text.TextUtils
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.*
import yuku.ambilwarna.AmbilWarnaDialog

/**
 * Activity to create a notification channel.After a channel created,
 * back to last activity to reflect the created one.
 * @author lei.zhang
 */
/**As a key return to who want to create a channel.*/
class CreateChannelActivity : AppCompatActivity() {
    companion object {
        val REQUEST_RETURN_KEY = "return_channel"
    }
    /**Restore created group*/
    private val mGroupDataList = ArrayList<NotificationChannelGroup>()
    /**Adapter of group spinner.*/
    private var mGroupAdapter: GroupSpinnerAdapter? = null
    private var mGroupView: Spinner? = null
    private var mImportanceArray: Array<String>? = null
    private var mImportanceView: Spinner? = null
    private var mLightSwitch: Switch? = null
    private var mChooseLightColorButton: View? = null
    private var mNotificationManager: NotificationManager? = null
    private @ColorRes var mChosenLightColor: Int = android.R.color.white
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_channel)
        mNotificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        // Retrieve group view and attach data to it.
        mGroupView = findViewById<Spinner>(R.id.group_spinner);
        loadChannelGroupData()
        mGroupAdapter = GroupSpinnerAdapter(this@CreateChannelActivity, android.R.layout.simple_list_item_1, mGroupDataList)
        mGroupView?.adapter = mGroupAdapter
        findViewById<View>(R.id.add_group_btn).setOnClickListener{
            showCreateGroupDialog()
        }
        mImportanceView = findViewById<Spinner>(R.id.importance_spinner)
        mImportanceArray = resources.getStringArray(R.array.channel_importance)
        mImportanceView?.adapter = ArrayAdapter(this@CreateChannelActivity, android.R.layout.simple_list_item_1, android.R.id.text1, mImportanceArray)
        // If light switch is off, hide choose light button.
        mLightSwitch = findViewById<Switch>(R.id.light_switch)
        mLightSwitch?.setOnCheckedChangeListener(CompoundButton.OnCheckedChangeListener{_, isChecked ->
            mChooseLightColorButton?.visibility = if(isChecked) View.VISIBLE else View.INVISIBLE
        })
        // Show a color picker when choosing a light color.
        mChooseLightColorButton = findViewById<View>(R.id.choose_light_color_btn)
        mChooseLightColorButton?.setOnClickListener{view ->
            AmbilWarnaDialog(this, R.color.colorPrimary, object : AmbilWarnaDialog.OnAmbilWarnaListener {
                override fun onCancel(dialog: AmbilWarnaDialog) {

                }

                override fun onOk(dialog: AmbilWarnaDialog, color: Int) {
                    mChosenLightColor = color
                    view.setBackgroundColor(color)
                }
            }).show()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menu?.addSubMenu(Menu.NONE, R.id.menu_save_notification_channel, Menu.NONE, getString(R.string.save))
        menu?.findItem(R.id.menu_save_notification_channel)?.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when(item?.itemId){
            R.id.menu_save_notification_channel -> {
                saveNotificationChannel()
                return true;
            }
        }
        return super.onOptionsItemSelected(item)
    }

    /**
     * Initialization to load group data from repository.
     * @see ChannelRepository
     */
    private fun loadChannelGroupData(){
        mGroupDataList.clear()
        // Set first position to empty express user has not chosen any group.
        val invalidGroup = NotificationChannelGroup("", "无")
        mGroupDataList.add(invalidGroup)
        val existGroupList = mNotificationManager?.notificationChannelGroups
        if(existGroupList != null){
            mGroupDataList += existGroupList
        }
    }

    /**Show an AlertDialog for input group name*/
    private fun showCreateGroupDialog() {
        // Create a EditText as the dialog content view.
        val nameView = EditText(this@CreateChannelActivity)
        nameView.hint = getString(R.string.please_input_channel_group_name)
        AlertDialog.Builder(this@CreateChannelActivity)
                .setMessage(R.string.add_channel)
                .setView(nameView)
                .setNegativeButton(R.string.cancel, null)
                .setPositiveButton(R.string.add, DialogInterface.OnClickListener{ dialog, which ->
                    if(TextUtils.isEmpty(nameView.text)){
                        Toast.makeText(this@CreateChannelActivity, R.string.channel_group_name_cant_be_empty, Toast.LENGTH_SHORT).show()
                    }else{
                        // Create the group with incremental id and input name.
                        val group = NotificationChannelGroup((mGroupDataList.size + 1).toString(), nameView.text.toString())
                        mNotificationManager?.createNotificationChannelGroup(group)
                        mGroupDataList.add(group)
                        mGroupAdapter?.notifyDataSetChanged()
                        // Set spinner select created
                        mGroupView?.setSelection(mGroupDataList.size - 1)
                    }
                })
                .show()
    }

    /**
     * Create notification channel and init it by user input.
     */
    private fun saveNotificationChannel() {
        val nameView = findViewById<TextView>(R.id.channel_name_et)
        if(nameView?.text.isNullOrBlank()){
            nameView?.error = getString(R.string.please_input_channel_name)
            return
        }
        val importanceView = findViewById<Spinner>(R.id.importance_spinner)
        val groupView = findViewById<Spinner>(R.id.group_spinner)
        val resultChannel = NotificationChannel(SystemClock.currentThreadTimeMillis().toString(), nameView.text.toString(), importanceView.selectedItemPosition + 1)
        // Channel description.
        val descriptionView = findViewById<TextView>(R.id.channel_description_tv)
        resultChannel.description = descriptionView?.text?.toString()
        // Exclude first group due to setting it as invalid when initialization.
        if(groupView.selectedItemPosition > 0){
            val selectedGroup = groupView.selectedItem as NotificationChannelGroup?
            resultChannel.group = selectedGroup?.id
        }
        // Set light color if light enabled.
        resultChannel.enableLights(mLightSwitch != null && mLightSwitch!!.isChecked)
        resultChannel.lightColor = mChosenLightColor
        // Set vibration enabled.
        val vibrationSwitch = findViewById<Switch>(R.id.vibration_switch)
        resultChannel.enableVibration(vibrationSwitch != null && vibrationSwitch.isChecked)
//        resultChannel.vibrationPattern = longArrayOf(100, 200, 300, 400, 500, 400, 300, 200, 400)
        mNotificationManager?.createNotificationChannel(resultChannel)
        // Back and give a result containing created channel.
        setResult(Activity.RESULT_OK, Intent().putExtra(REQUEST_RETURN_KEY, resultChannel))
        finish()
    }

    /**Adapter for group spinner*/
    internal inner class GroupSpinnerAdapter(context: Context, resource: Int, list: List<NotificationChannelGroup>) : ArrayAdapter<NotificationChannelGroup>(context, resource, list) {
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
}
