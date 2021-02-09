package com.example.sapphireassistantframework.workspace

import android.content.Intent
import android.os.IBinder
import com.example.componentframework.SAFService
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

    fun coreReadConfigJSON(jsonConfig: JSONObject){
        // These should be global vars, so I really just need to load them.
        // I need to account for just directly loading these, not having them in a separate config
        var jsonDefaultModules = loadJSONTable(jsonConfig.getString("default_modules"))
        var jsonBoundStartup = loadJSONTable(jsonConfig.getString("bound_starup"))
        var jsonBackgroundStartup = loadJSONTable(jsonConfig.getString("background_startup"))
        var jsonForegroundStartup = loadJSONTable(jsonConfig.getString("foreground_startup"))
        var jsonHookList = loadJSONTable(jsonConfig.getString("hooks"))
    }

    fun checkDefaultModules(moduleType: String,packageName: String){
        var defaultsTableFilename = "defaultModules.tbl"
        var jsonDefaultModules = loadJSONTable(defaultsTableFilename)
        if(jsonDefaultModules.has(packageName) == false){
            jsonDefaultModules.put(moduleType,packageName)
        }
        saveJSONTable(defaultsTableFilename,jsonDefaultModules)
    }

    // This will likely be called in a different section of the CoreService, upon receipt of an intent
    // How do I transfer packageName and className since they're not part of the intent...?
    fun updateModuleRegistration(intent: Intent){
        // This should be global to CoreService
        var registrationTableFilename = "registration.tbl"
        // This should be global to CoreService
        // Is it a table or database?
        var jsonRegistration = loadJSONTable(registrationTableFilename)
        // This package name isn't right. It needs to come from PackageManager
        var jsonModuleRegistration = JSONObject()
        if(checkModuleRegistration(packageName)){
            jsonModuleRegistration = jsonRegistration.getJSONObject(packageName)
            if(intent.hasExtra(MODULE_VERSION)){
                if(jsonModuleRegistration.getString(MODULE_VERSION) != intent.getStringExtra(MODULE_VERSION)){
                    // Do some kind of update
                    // This mostly applys to data, as far as I can tell
                }
            }
        }else {
            // I am using DATA_KEYS here, can I make it more generic?
            // for(key in intent.getStringArrayListExtra(DATA_KEYS)!!)
            if (intent.hasExtra(MODULE_TYPE)) {
                // What happens if it has multiple module_types?
                // Maybe I should register these in a moduleTypeTable instead of in their own table
                jsonModuleRegistration.put(MODULE_TYPE, intent.getStringExtra(MODULE_TYPE))
                checkDefaultModules(intent.getStringExtra(MODULE_TYPE)!!,packageName)
            } else if (intent.hasExtra(MODULE_VERSION)) {
                jsonModuleRegistration.put(MODULE_VERSION, intent.getStringExtra(MODULE_VERSION))
            }
        }
        jsonRegistration.put(packageName, jsonModuleRegistration.toString())
    }

    // Should this be packageName,className?
    fun checkModuleRegistration(packageName: String): Boolean{
        // This should be global to CoreService
        var registrationTable = "registration.tbl"
        // This should be global to CoreService
        // Is it a table or database?
        var jsonRegistration = loadJSONTable(registrationTable)
        if(jsonRegistration.has(packageName)) {
            //checkVersionInfo
            return true
        }else{
            return false
        }
    }

    fun saveJSONTable(filename: String, jsonDatabase: JSONObject){
        var databaseFile = File(filesDir, filename)
        databaseFile.writeText(jsonDatabase.toString())
    }

    fun loadJSONTable(filename: String): JSONObject{
        /**
         * if(File(filesDir,filename).exists == false){
         *     //It's just directly the info, not a link to another file
         * }
         */
        var databaseFile = File(filesDir,filename)
        var jsonDatabase = JSONObject(databaseFile.readText())
        return jsonDatabase
    }

    fun registerModuleType(intent: Intent){
        if(intent.hasExtra(MODULE_TYPE)){
            var moduleTypeData = intent.getStringExtra(MODULE_TYPE)!!
            var moduleTypes = moduleTypeData.split(',')
            for(moduleType in moduleTypes){
                //register moduleType
                if(defaultModuleType == null){
                    // default = moduleType
                }
            }
        }else{
            //register GENERIC
        }
        //writeFile
    }

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