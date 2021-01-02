package com.example.processormodule

/**
 * This module exists to install the processor in the Core Module, not to handle install
 * processes of other services that want to pass data
 */

import android.app.Service
import android.content.Intent
import android.os.Bundle
import android.os.IBinder
import android.util.Log
import java.lang.Exception

class InstallService: Service() {
    override fun onBind(intent: Intent?): IBinder? {
        TODO("Not yet implemented")
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        registerModule()

        return super.onStartCommand(intent, flags, startId)
    }

    fun registerModule(){
        try {
            var installBundle = Bundle()
            var installIntent = Intent()

            // This is registering the name of service, and its classification
            installBundle.putString(
                "PROCESSOR",
                "com.example.sapphireassistantframework.ProcessorCentralService"
            )
            installIntent.addCategory("assistant.framework.PROCESSOR_MODULE")

            startService(installIntent)
        }catch(exception: Exception){
            Log.e("InstallService(ProcessorModule)","")
        }
    }
}