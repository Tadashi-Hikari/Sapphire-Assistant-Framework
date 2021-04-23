package com.example.sapphireassistantframework

import android.content.Intent
import com.example.componentframework.SapphireFrameworkRegistrationService
import com.example.componentframework.depreciated.SAFInstallService

class CoreModuleInstallService: SapphireFrameworkRegistrationService(){
    val VERSION = "0.0.1"

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        try {
            if (intent!!.action == ACTION_SAPPHIRE_MODULE_REGISTER) {
                registerModule(intent!!)
            }
        }catch(exception: Exception){
             Log.e("There was some kind of error with the install intent")
        }
        return super.onStartCommand(intent, flags, startId)
    }

    override fun registerModule(intent: Intent){
        var returnIntent = Intent(intent)
        // This could throw an error, since the install service is separate from the PostOffice
        returnIntent = registerModuleType(returnIntent,CORE)
        returnIntent = registerVersion(returnIntent,VERSION)

        super.registerModule(returnIntent)
    }
}