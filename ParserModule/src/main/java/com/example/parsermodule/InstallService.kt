package com.example.parsermodule

import android.app.Service
import android.content.Intent
import android.os.Bundle
import android.os.IBinder

class InstallService: Service() {
    override fun onBind(intent: Intent?): IBinder? {
        TODO("Not yet implemented")
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        if(intent.action == "INSTALL"){
            registerModules()
        }

        return super.onStartCommand(intent, flags, startId)
    }

    // I can have
    fun registerModules(){
        var installBundle = Bundle()
        var installIntent = Intent()

        installBundle.putString("PARSER", "com.example.sapphireassistantframework.UtteranceProcessing")
    }
}