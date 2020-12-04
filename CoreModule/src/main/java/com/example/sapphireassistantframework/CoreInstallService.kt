package com.example.sapphireassistantframework

import android.app.Service
import android.content.Intent
import android.os.Bundle
import android.os.IBinder
import android.os.Parcel
import android.os.Parcelable

class CoreInstallService: Service(){
    override fun onBind(intent: Intent?): IBinder? {
        TODO("Not yet implemented")
    }

    fun installModule(modulePackage: String, className: String){
        var installerIntent = Intent()
        installerIntent.setAction("INSTALL")
        installerIntent.setClassName(modulePackage, className)
        startService(installerIntent)
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        if(intent.action == "INSTALL"){
            install(intent)
        }
        return super.onStartCommand(intent, flags, startId)
    }

    fun install(intent: Intent){
    }
}