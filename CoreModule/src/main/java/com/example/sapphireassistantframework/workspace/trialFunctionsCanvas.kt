package com.example.sapphireassistantframework.workspace

import android.content.Intent
import android.os.IBinder
import android.util.Log
import com.example.componentframework.SAFService
import com.example.sapphireassistantframework.CoreModuleInstallService
import org.json.JSONObject
import java.io.File

class trialFunctionsCanvas: SAFService() {
    var DATABASE = "databae.db"
    var HOOKLOG = "hooklog"

    /**
     * These are sample settings I am thinking of implementing in CORE
     * absolute_terminating_module=string
     * alternate_modes=string (a mode table, for context switching)
     * logfile=string
     * error_handle_module=string (should this not be a module, but just a thing to be looked up in route?
     * default_handler_module=string (should this not be a module, but just a thing to be looked up in route?
     *
     * //---These may need to be moved to Vosk STT---
     * hotword_mode=boolean
     * hotword_list=string (multiple hotwords to listen for)
     * listen_on_screen_only=boolean (only listen when the screen is on)
     * listen_when_charging_only=boolean
     * always_listen=boolean
     * recognizer_depth=int (number of independant recognitions that can take place at the same time)
     */

    //var POSTAGE = "ENVIRONMENT_VARIABLES"
    //var ROUTE = "JSONROUTE"

    // Pretty straightforward
    fun installSelf(){
        var coreInstallIntent = Intent()
        coreInstallIntent.setClassName(this,"com.example.sapphireassistantframework.CoreModuleInstallService")
        startService(coreInstallIntent)
    }

    // There could be an issue here, since it isn't waiting for a callback. I may need to run this through the multiprocess module
    fun scanInstalledModules() {

        var intent = Intent().setAction(ACTION_SAPPHIRE_MODULE_REGISTER)

        // This is important, and the order is important
        var jsonModuleDefaults = JSONObject()
        if(jsonModuleDefaults.isNull(CORE)) {
            installSelf()
            return
            // multiprocess must be used so CORE waits until all modules are installed before proceeding
        }else if(jsonModuleDefaults.isNull(MULTIPROCESS)){
            intent.addCategory(MULTIPROCESS)
            // It has to use the first one, then it can reach out to the rest
            var multiprocessModules = packageManager.queryIntentServices(intent, 0)
            // This runs the install process for the first multiprocessModule
            intent.setClassName(multiprocessModules.first().serviceInfo.packageName,multiprocessModules.first().serviceInfo.name)
            startService(intent)
            return
        }

        intent = intent.setAction(ACTION_SAPPHIRE_MODULE_REGISTER)
        var validSapphireModules = packageManager.queryIntentServices(intent,0)

        for (module in validSapphireModules.take(1)) {
            try {
                var packageName = module.serviceInfo.packageName
                var className = module.serviceInfo.name
                // This is called twice. Why?
                Log.i("CoreService", "Found a module. Checking if it's registered: ${packageName};${className}")
                // If its registered, check version. Else, register it
                if (checkModuleRegistration(packageName, className)) {

                } else {
                    // No, I need to wait and dispatch them all in the multiprocess module
                    // append this to a thing, pack them in a (route) and send to multiprocess.
                    // I can just queue (linkedlist) for file access privilege on installed files
                    installRegisterModule(packageName, className)
                }
            } catch (exception: Exception) {
                continue
            }
        }
    }

    fun defaultModuleType(){

    }

    // I think this is supposed to be a loadRoutes
    fun routeTest(){
        var database = JSONObject()

        var route = database.getJSONArray("modulename.filename")
        for(index in 0 until route.length()){
            var moduleData = route.getJSONObject(index)
            parseModuleData(moduleData)
        }
    }

    // What was I making this for? Oh! It's for replacing parseRoute so I can include flags
    fun parseModuleData(json: JSONObject){
        // I don't see any reason why it shouldn't just be these, predefined
        json.get("PACKAGE")
        json.get("CLASSNAME")
        // Should this be sub-parsed?
        var flags = json.getJSONObject("FLAGS")
        for(index in 0 until flags.length()){
            // add flags, as extras?
        }
    }

    // This is for getting asset filenames
    fun loadAssetNames(): List<File>{
        // This gives a list of the root directory in assets
        var assetsList = assets.list("")
        return assetsList as List<File>
    }

    override fun onBind(intent: Intent?): IBinder? {
        TODO("Not yet implemented")
    }
}