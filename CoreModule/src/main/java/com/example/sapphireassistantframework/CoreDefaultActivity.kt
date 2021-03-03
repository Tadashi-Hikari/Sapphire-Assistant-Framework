package com.example.sapphireassistantframework

import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.core.content.ContextCompat
import java.io.File


class CoreCentralActivity: Activity(){

    private var tables = listOf("registration.tbl","defaultmodules.tbl","background.tbl","routetable.tbl","alias.tbl")


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_core_central)
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