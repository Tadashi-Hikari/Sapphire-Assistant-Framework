package com.example.sapphireassistantframework

import android.content.Intent
import android.util.Log
import com.example.componentframework.SAFInstallService

class CoreModuleInstallService: SAFInstallService(){
    val VERSION = "0.0.1"

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        try {
            if (intent!!.action == ACTION_SAPPHIRE_MODULE_REGISTER) {
                registerModule()
            }
        }catch(exception: Exception){
            Log.i("VoskModuleInstallService","There was some kind of error with the install intent")
        }
        return super.onStartCommand(intent, flags, startId)
    }

    fun registerModule(){
        var intent = Intent()
        intent = registerVersion(intent,VERSION)
        intent = registerPackageName(intent,this.packageName)

        super.registerModule(intent)
    }
}