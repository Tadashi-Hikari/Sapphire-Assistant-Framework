package com.example.sapphireassistantframework

import android.app.Activity
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.util.Log
import android.view.View
import android.widget.ScrollView
import android.widget.TextView
import androidx.core.content.ContextCompat
import java.io.File
import android.os.Bundle as Bundle


class CoreSimpleActivity: Activity()
{
    // This needs to be loaded from a config table
    private var tables = listOf("registration.tbl","defaultmodules.tbl","background.tbl","routetable.tbl","alias.tbl")
    val GUI_BROADCAST = "assistant.framework.broadcast.GUI_UPDATE"
    val MESSAGE="assistant.framework.protocol.MESSAGE"
    lateinit var coreBroadcastReceiver: BroadcastReceiver

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.core_activity)
        var textView = findViewById<TextView>(R.id.textView)
        // For some reason this won't work when set in the XML
        textView.setHorizontallyScrolling(true)
    }

    override fun onResume() {
        super.onResume()

        // Maybe an optimization issue
        coreBroadcastReceiver = object : BroadcastReceiver(){
            override fun onReceive(context: Context?, intent: Intent?) {
                var test = intent?.getStringExtra(MESSAGE)
                updateUI(test!!)
                Log.i(this.javaClass.name,"SAF Broadcast receieved")
            }
        }

        var filter = IntentFilter()
        filter.addAction(GUI_BROADCAST)
        this.registerReceiver(coreBroadcastReceiver,filter)
        Log.i(this.javaClass.name,"Receiver registered")
    }

    override fun onPause() {
        super.onPause()
        unregisterReceiver(coreBroadcastReceiver)
    }

    fun updateUI(string: String){
        var textView: TextView = findViewById(R.id.textView)
        textView.append("\n${string}")
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
        checkForPermissions()
        // This needs to be formalized and moved over
        intent.setAction("INIT")
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