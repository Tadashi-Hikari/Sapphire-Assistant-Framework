package com.example.sapphireassistantframework.workspace

import android.app.Service
import android.content.Intent
import android.os.IBinder
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.util.jar.JarOutputStream

class functionCanvas: Service() {
    var DATABASE = "databae.db"
    var HOOKLOG = "hooklog"
    var CORE = "CORE.MODULE"

    var POSTAGE = "ENVIRONMENT_VARIABLES"
    var ROUTE = "JSONROUTE"

    fun getCoreModule(intent: Intent):String{
        var postage = intent.getStringExtra(POSTAGE)
        var envVarJSON = JSONObject(postage)
        return envVarJSON.getString(CORE)!!
    }

    // This is pretty straightforward.
    // I think that this needs to be done other than onCreate.
    fun installRegisterModule(packageClass: String){
        var databaseFile = File(filesDir,DATABASE)
        var database = JSONObject()

        if(database.has(packageClass)){
            return
        }else{
            var module = JSONObject()
            module.put("packageClass",packageClass)
            databaseFile.writeText(module.toString())
        }
    }

    fun routeTest(){
        var database = JSONObject()

        var route = database.getJSONArray("modulename.filename")
        for(index in 0 until route.length()){
            var moduleData = route.getJSONObject(index)
            parseModuleData(moduleData)
        }
    }

    // This should all go in postage. 
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

    fun loadRoutes(){

    }

    /**
     * What I want to happen here, is basically update a TextView in the CoreActivity.
     * Do I need to make it *not* hardcoded?
     *
     * It is looking a lot like an outgoing Multiprocess
     */
    fun notifyHooks(intent: Intent){
        // I am thinking that checkHookLog is basically a shadowRoute
        if(checkHookLog(intent)){
            var hookIntent = Intent().setClassName(this,"hook.intent.service")
            hookIntent.setAction("SECOND ROUTE")
        }
    }

    fun checkHookLog(intent: Intent): Boolean{
        var databaseFile = File(filesDir,DATABASE)
        var databaseString = databaseFile.readText()
        var database = JSONObject(databaseString)
        var hooklog = database.getJSONObject(HOOKLOG)
        if(intent.getStringExtra(HOOKLOG) != null){
            print("YAAAAAY")
            return true
        }

        return false
    }

    /**
     * This is for taking a Mycroft style .intent and preparing it for Stanford CoreNLP
     */
    fun prepareTrainingData(){
        var moduleName = "com.example.calendarmodule"
        var filename = "get.intent"
        var trainingData = listOf<String>()
        var formattedTrainingData = mutableListOf<String>()

        for(line in trainingData){
            /**
             * This does the tab-separation as required by Stanford CoreNLP.
             * Should I change how it's named? I am not expecting SAF to do the direct entry,
             * the filename might be an -action as well, or it could be a separate path... decisions
             */
            formattedTrainingData.add("${moduleName}.${filename}\t${line}")
        }
    }

    fun readConfigFile(){
        var files = loadAssetNames()
        for(file in files){
            if(file.endsWith(".conf")) {
                parseConfigFile()
            }
        }
    }

    fun parseConfigFile(filename: String): JSONObject{
        return JSONObject()
    }

    fun loadAssetNames(): List<File>{
        // This gives a list of the root directory in assets
        var assetsList = assets.list("")
    }

    // I think I am moving this to its own module
    fun checkConditionals(){

    }

    override fun onBind(intent: Intent?): IBinder? {
        TODO("Not yet implemented")
    }
}