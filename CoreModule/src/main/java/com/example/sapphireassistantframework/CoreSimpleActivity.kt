package com.example.sapphireassistantframework

import android.app.Activity
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.util.Log
import android.view.View
import android.widget.EditText
import android.widget.TextView
import androidx.core.content.ContextCompat
import java.io.File
import android.os.Bundle as Bundle


class CoreSimpleActivity: Activity()
{
    private var tables = listOf("registration.tbl","defaultmodules.tbl","background.tbl","routetable.tbl","alias.tbl")
    val GUI_BROADCAST = "assistant.framework.broadcast.GUI_UPDATE"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_core_central)
    }

    override fun onResume() {
        super.onResume()

        // Maybe an optimization issue
        var coreBroadcastReceiver = object : BroadcastReceiver(){
            override fun onReceive(context: Context?, intent: Intent?) {
                updateUI(intent.toString())
                Log.i(this.javaClass.name,"SAF Broadcast receieved")
            }
        }

        var filter = IntentFilter()
        filter.addAction(GUI_BROADCAST)
        this.registerReceiver(coreBroadcastReceiver,filter)
        Log.i(this.javaClass.name,"Receiver registered")
    }

    fun updateUI(string: String){
        var textView: TextView = findViewById(R.id.textView)
        textView.setText(string)
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