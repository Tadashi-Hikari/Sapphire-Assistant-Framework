package com.example.sapphireassistantframework

import android.app.Service
import android.content.Intent
import android.os.Bundle
import android.os.IBinder
import android.os.Parcel
import android.os.Parcelable

/** This service is supposed to handle the complex install information that the core module may
 * need, but what is that? Which modules are what, and for what differences?
 */

class CoreInstallService: Service(){
    override fun onBind(intent: Intent?): IBinder? {
        TODO("Not yet implemented")
    }

    // This could be merged in to the main coreService I think?
    fun installModule(modulePackage: String, className: String){
        var installerIntent = Intent()
        installerIntent.setAction("assistant.framework.module.INSTALL")
        installerIntent.setClassName(modulePackage, className)
        startService(installerIntent)
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        if(intent.action == "INSTALL"){
            installModule(
                intent.getStringExtra("PACKAGE_NAME")!!,
                intent.getStringExtra("CLASS_NAME")!!
            )
        }
        return super.onStartCommand(intent, flags, startId)
    }

    fun install(intent: Intent){
    }
}