package com.example.sapphireassistantframework

import android.content.Intent
import android.util.Log
import com.example.componentframework.SAFInstallService

class CoreModuleInstallService: SAFInstallService(){
    val VERSION = "0.0.1"

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        try {
            if (intent!!.action == ACTION_SAPPHIRE_MODULE_REGISTER) {
                registerModule(intent!!)
            }
        }catch(exception: Exception){
            Log.i("VoskModuleInstallService","There was some kind of error with the install intent")
        }
        return super.onStartCommand(intent, flags, startId)
    }

    override fun registerModule(intent: Intent){
        var returnIntent = Intent(intent)
        returnIntent.putExtra(MODULE_PACKAGE,this.packageName)
        // Not needed, cause it's set in the CoreRegistrationService. This will be an issue w/ multiple entries though
        //returnIntent.putExtra(MODULE_CLASS,"${this.packageName}.CoreService")
        registerModuleType(returnIntent,CORE)
        registerVersion(returnIntent,VERSION)

        super.registerModule(returnIntent)
    }
}