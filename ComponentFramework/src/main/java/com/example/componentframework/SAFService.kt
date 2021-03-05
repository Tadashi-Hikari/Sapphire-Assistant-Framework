package com.example.componentframework

import android.app.Service
import android.content.Intent
import android.util.Log
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.io.InputStreamReader
import java.lang.Exception

abstract class SAFService: Service(){

    // This is just to override
    inner class LogOverride{

        fun broadcastStatus(name: String, message:String) {
            var notifyIntent = Intent()
            notifyIntent.putExtra(MESSAGE, "${name}: ${message}")
            notifyIntent.setAction(GUI_BROADCAST)
            sendBroadcast(notifyIntent)
        }

        fun i(name: String, message: String){
            android.util.Log.i(name,message)
            broadcastStatus(name,message)
        }

        fun d(name: String, message: String){
            android.util.Log.d(name,message)
            broadcastStatus(name,message)
        }

        fun e(name: String, message: String){
            android.util.Log.e(name,message)
            broadcastStatus(name,message)
        }

        fun v(name: String, message: String){
            android.util.Log.v(name,message)
            broadcastStatus(name,message)
        }

        fun w(name: String, message: String){
            android.util.Log.w(name,message)
            broadcastStatus(name,message)
        }
    }

    var Log = LogOverride()

    // Standard extras
    val MESSAGE="assistant.framework.protocol.MESSAGE"
    val STDERR="assistant.framework.protocol.STDERR"
    // This is going to be for ENV_VARIABLES
    val POSTAGE="assistant.framework.protocol.POSTAGE"
    val ROUTE="assistant.framework.protocol.ROUTE"
    val ID = "assistant.framework.module.protocol.ID"

    // Maybe this should be used elsewhere...
    var STARTUP_ROUTE = "assistant.framework.protocol.STARTUP_ROUTE"

    val MODULE_PACKAGE = "assistant.framework.module.protocol.PACKAGE"
    val MODULE_CLASS = "assitant.framework.module.protocol.CLASS"
    var MODULE_TYPE = "assistant.framework.module.protocol.TYPE"
    val MODULE_VERSION = "assistant.framework.module.protocol.VERSION"

    /**
     * I don't know that I need to list all of these explicitly, and I think I'll
     * let the user override them anyway. This is just for initial install purposes
     */
    val CORE="assistant.framework.module.type.CORE"
    val PROCESSOR="assistant.framework.module.type.PROCESSOR"
    val MULTIPROCESS="assistant.framework.module.type.MULTIPROCESS"
    // These are the ones I don't think are essential
    val INPUT="assistant.framework.module.type.INPUT"
    val TERMINAL="assistant.framework.module.type.TERMINAL"
    val GENERIC="assistant.framework.module.type.GENERIC"

    // Module specific extras
    val PROCESSOR_ENGINE="assistant.framework.processor.protocol.ENGINE"
    val PROCESSOR_VERSION="assistant.framework.processor.protocol.VERSION"
    val DATA_KEYS="assistant.framework.module.protocol.DATA_KEYS"

    // Actions
    val ACTION_SAPPHIRE_CORE_BIND="assistant.framework.core.action.BIND"
    // This is sent to the CORE from the module, so the core can handle the registration process
    // This is for a module to request *all* data from the core (implicit intent style)
    val ACTION_SAPPHIRE_ROUTE_LOOKUP = "assistant.framework.core.action.ROUTE_LOOKUP"
    val ACTION_SAPPHIRE_CORE_REGISTRATION_COMPLETE = "assistant.framework.core.action.REGISTRATION_COMPLETE"
    val ACTION_SAPPHIRE_CORE_REQUEST_DATA="assistant.framework.core.action.REQUEST_DATA"
    val ACTION_SAPPHIRE_UPDATE_ENV = "action.framework.module.action.UPDATE_ENV"
    val ACTION_SAPPHIRE_MODULE_REGISTER = "assistant.framework.module.action.REGISTER"
    // this is -V on the command line
    val ACTION_SAPPHIRE_MODULE_VERSION = "assistant.framework.module.action.VERSION"
    // This is for core to request data from a specific module
    val ACTION_SAPPHIRE_TRAIN="assistant.framework.processor.action.TRAIN"
    val GUI_BROADCAST = "assistant.framework.broadcast.GUI_UPDATE"

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int{
        // I don't know why I can't copy or send out the incoming intent. I suppose its the broadcast propogation thing
        var notifyIntent = Intent()
        notifyIntent.putExtra(MESSAGE,"${this.packageName};${this.javaClass.name}")
        try {
            notifyIntent.setAction(GUI_BROADCAST)
            sendBroadcast(notifyIntent)
            Log.i(this.javaClass.name,"SAF broadcast sent")
        }catch (exception: Exception){
            Log.e(this.javaClass.name,"SAF broadcast error")
        }
        return super.onStartCommand(intent, flags, startId)
    }

    // This is for having a SAF compontent pass along the route w/o a callback to core
    fun parseRoute(string: String): List<String>{
        var route = emptyList<String>()
        route = string.split(",")
        return route
    }

    // This doesn't actually impact the list, which I would have to return... Should it take intent?
    fun getNextAlongRoute(route: List<String>): String{
        return route[0]
    }

    fun loadConfig(config: String): JSONObject{
        var configJSON = JSONObject()
        if(configFileExists(config)){
            configJSON = loadFileConfig(config)
        }else{
            configJSON = loadDefaultConfig(config)
        }
        return configJSON
    }

    fun loadDefaultConfig(config: String): JSONObject{
        var configFile: File = convertStreamToFile(config)
        var configJSON = JSONObject(configFile.readText())
        return configJSON
    }

    fun loadFileConfig(config: String): JSONObject{
        var configFile = File(filesDir,config)
        var configJSON = JSONObject(configFile.readText())
        return configJSON
    }

    fun configFileExists(config: String): Boolean{
        if(File(filesDir,config).exists()){
            return true
        }
        return false
    }

    fun defaultToFile(configJSON: JSONObject, config:String){
        var configFile = File(filesDir,config)
        configFile.writeText(configJSON.toString())
    }

    fun convertStreamToFile(filename: String): File {
        var suffix = ".temp"
        // This file needs to be tab separated columns
        var asset = assets.open(filename)
        var fileReader = InputStreamReader(asset)

        var tempFile = File.createTempFile(filename, suffix)
        var tempFileWriter = FileOutputStream(tempFile)
        // This is ugly AF
        var data = fileReader.read()
        while (data != -1) {
            tempFileWriter.write(data)
            data = fileReader.read()
        }
        // Do a little clean up
        asset.close()
        tempFileWriter.close()

        return tempFile
    }

    fun saveJSONTable(filename: String, jsonDatabase: JSONObject){
        var databaseFile = File(filesDir, filename)
        databaseFile.writeText(jsonDatabase.toString())
    }

    fun loadJSONTable(filename: String): JSONObject{
        if(File(filesDir,filename).exists() == false){
            return JSONObject()
        }
        var databaseFile = File(filesDir,filename)
        var jsonDatabase = JSONObject(databaseFile.readText())
        return jsonDatabase
    }

    // Used to get the env variable about whichever is currently core
    fun getCoreModule(intent: Intent):String{
        var postageData = intent.getStringExtra(POSTAGE)
        var postageJSON = JSONObject(postageData)
        return postageJSON.getString(CORE)!!
    }

    // This *def* needs to be error checked. Basically a copycat function
    fun getEnvVariable(intent: Intent,envVariable: String): String{
        var postageData = intent.getStringExtra(POSTAGE)
        var postageJSON = JSONObject(postageData)
        return postageJSON.getString(envVariable)
    }

    fun parseConfigFile(filename: String): JSONObject{
        Log.v(this.javaClass.name,"Parsing config file")
        return JSONObject()
    }

    fun startSAFService(intent: Intent){
        var SAFIntent = Intent(intent)
        SAFIntent = checkRouteForVariables(intent)
        startService(SAFIntent)
    }

    fun checkRouteForVariables(intent: Intent): Intent{
        var defaultIntent = Intent(intent)
        try {
            var routeData = intent.getStringExtra(ROUTE)!!
            var routeModuleMutableList = parseRoute(routeData).toMutableList()
            //var environmentalVaribles = intent.getStringExtra(POSTAGE)
            //var jsonDefaultModules = JSONObject(environmentalVaribles)
            var postage = JSONObject(intent.getStringExtra(POSTAGE)!!)

            Log.v(this.javaClass.name,"routeData before checking ${routeData}")
            for(module in routeModuleMutableList.withIndex()) {
                var temp = module.copy()
                Log.v(this.javaClass.name,"Checking ${module.value} in route...")
                if (postage.has(module.value)) {
                    Log.v(this.javaClass.name,"Matched key ${module.value} at index ${module.index} with an ENV_VAR")
                    Log.v(this.javaClass.name,"Postages value is ${postage.optString(module.value,null)}")
                    routeModuleMutableList.set(module.index,postage.optString(module.value))
                }
            }

            var finalizedRoute = ""
            Log.i(this.javaClass.name,"Finalizing route...")
            for (module in routeModuleMutableList.withIndex()) {
                if (module.index == 0) {
                    finalizedRoute = module.value
                } else {
                    finalizedRoute += ",${module.value}"
                }
            }
            intent.putExtra(ROUTE, finalizedRoute)
            return intent
        }catch(exception: Exception){
            Log.e(this.javaClass.name,"Error checking route for variables, returning defaultIntent")
            Log.e(this.javaClass.name,exception.toString())
            return defaultIntent
        }
    }
}