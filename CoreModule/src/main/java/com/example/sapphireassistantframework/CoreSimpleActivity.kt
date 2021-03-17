package com.example.sapphireassistantframework

import android.app.Activity
import android.app.Service
import android.app.role.RoleManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.Build
import android.os.IBinder
import android.service.voice.VoiceInteractionSession
import android.util.Log
import android.view.View
import android.widget.ScrollView
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.lang.Exception
import android.os.Bundle as Bundle


class CoreSimpleActivity: Activity()
{
    inner class assistantActivity: VoiceInteractionSession(this){

    }
    // This needs to be loaded from a config table
    private var tables = listOf("registration.tbl","defaultmodules.tbl","background.tbl","routetable.tbl","alias.tbl")
    val GUI_BROADCAST = "assistant.framework.broadcast.GUI_UPDATE"
    val MESSAGE="assistant.framework.protocol.MESSAGE"
    lateinit var coreBroadcastReceiver: BroadcastReceiver
    lateinit var coreDirectoryPicker: Service

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        checkForPermissions()
        setContentView(R.layout.core_activity)
        var textView = findViewById<TextView>(R.id.textView)
        // For some reason this won't work when set in the XML
        textView.setHorizontallyScrolling(true)

        var assistIntent = Intent()
        assistIntent.setClassName(this,"com.example.sapphireassistantframework.voiceassistant.CoreVoiceInteractionService")
        assistIntent.setAction(Intent.ACTION_ASSIST)
        startService(assistIntent)
    }


    override fun onResume() {
        super.onResume()

        // Maybe an optimization issue
        coreBroadcastReceiver = object : BroadcastReceiver(){
            override fun onReceive(context: Context?, intent: Intent?) {
                var test = intent?.getStringExtra(MESSAGE)
                updateUI(test!!)
            }
        }

        var filter = IntentFilter()
        filter.addAction(GUI_BROADCAST)
        this.registerReceiver(coreBroadcastReceiver,filter)
        Log.i(this.javaClass.name,"Receiver registered")
    }

    fun switchToSettings(view: View){
        var intent = Intent().setClassName(this,"com.example.sapphireassistantframework.CoreSettingsActivity")
        startActivity(intent)
    }

    fun export(){
        var jsonExport = JSONObject()
        if(jsonExport.getBoolean("export")){
            //writeFileToDir(EXPORT)
        }
    }

    override fun onPause() {
        super.onPause()
        unregisterReceiver(coreBroadcastReceiver)
    }

    fun updateUI(string: String){
        var textView: TextView = findViewById(R.id.textView)
        textView.append("\n${string}")
        var scrollView = findViewById<ScrollView>(R.id.verticalScrollView)
        scrollView.fullScroll(View.FOCUS_DOWN)
    }

    // This will likely need to be more dynamic. This is just checking for permissions
    fun checkForPermissions(){
        when{
            ContextCompat.checkSelfPermission(
                this,
                "android.permission.RECORD_AUDIO"
            ) == PackageManager.PERMISSION_DENIED -> {
                requestPermissions(
                    arrayOf("android.permission.RECORD_AUDIO"),
                    PackageManager.PERMISSION_GRANTED
                )
            }
        }
    }

    fun startCoreService(view: View){
        Log.v(this.localClassName,"starting ${this.packageName}, ${this.packageName}.CoreService")
        var intent: Intent = Intent().setClassName(this.packageName,"${this.packageName}.CoreService")
        // This needs to be formalized and moved over
        val ACTION_SAPPHIRE_INITIALIZE="assistant.framework.processor.action.INITIALIZE"
        intent.setAction(ACTION_SAPPHIRE_INITIALIZE)
        // This doesn't do anything in particular to indicate that it is starting SF for the first time
        startService(intent)
    }

    // Gracefully handle denied permissions
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if((grantResults.isNotEmpty())){
            Log.v("CoreCentralActivity","Permission granted")
        }else{
            Log.e("CoreCentralActivity","Permission must be granted for use")
        }
    }

    fun reset(view: View){
        var resetIntent = Intent().setClassName(this,"com.example.processormodule.ProcessorCentralService")
        resetIntent.setAction("DELETE_CLASSIFIER")
        startService(resetIntent)

        for(table in tables){
            var file = File(filesDir,table)
            file.delete()
        }

        var foregroundService = Intent().setClassName(this,"com.example.sapphireassistantframework.CoreService")
        stopService(foregroundService)
    }
}