package com.example.sapphireassistantframework.workspace

import android.content.Intent
import android.os.IBinder
import android.os.SystemClock
import android.util.Log
import androidx.core.app.JobIntentService
import com.example.componentframework.SAFService
import com.example.sapphireassistantframework.CoreModuleInstallService
import org.json.JSONObject
import java.io.File
import java.util.*

class trialFunctionsCanvas: SAFService(){
    var DATABASE = "databae.db"
    var HOOKLOG = "hooklog"

    var stack = LinkedList<Intent>()
    var goodToGo = false
    fun stackAndSleepInit(intent: Intent){
        stack.push(intent)
            // Sleep for five seconds, and check again
        while(!goodToGo) {
            SystemClock.sleep(500)
        }
        while(stack.isNotEmpty()) {
            if (intent.action == ACTION_SAPPHIRE_MODULE_REGISTER) {

            } else if (intent.action == ACTION_SAPPHIRE_CORE_BIND) {

            } else if (intent.action == ACTION_SAPPHIRE_CORE_REQUEST_DATA) {

            } else {
                //sortMail()
            }
        }
    }

    // I need this in case any routes call env_variables (very likely)
    // This is for installing defaults if they don't exits
    fun checkDefaultModuleRegistrations(moduleType: String, packageName: String) {
        var defaultsTableFilename = "defaultModules.tbl"
        var jsonDefaultModules = loadJSONTable(defaultsTableFilename)
        if (jsonDefaultModules.has(packageName) == false) {
            jsonDefaultModules.put(moduleType, packageName)
        }
        saveJSONTable(defaultsTableFilename, jsonDefaultModules)
    }


    //var ROUTE = "JSONROUTE"

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