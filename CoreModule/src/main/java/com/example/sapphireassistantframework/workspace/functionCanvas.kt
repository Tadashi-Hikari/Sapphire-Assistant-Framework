package com.example.sapphireassistantframework.workspace

import android.app.Service
import android.content.Intent
import org.json.JSONArray
import org.json.JSONObject
import java.io.File

class functionCanvas: Service() {
    var DATABASE = "databae.db"
    var HOOKLOG = "hooklog"

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
}