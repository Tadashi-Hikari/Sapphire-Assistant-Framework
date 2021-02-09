package com.example.componentframework

import android.app.Service
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.io.InputStreamReader

abstract class SAFService: Service(){
    // Standard extras
    val MESSAGE="assistant.framework.protocol.MESSAGE"
    val STDERR="assistant.framework.protocol.STDERR"
    // This is going to be for ENV_VARIABLES
    val POSTAGE="assistant.framework.protocol.POSTAGE"
    val ROUTE="assistant.framework.protocol.ROUTE"

    var MODULE_TYPE = "assistant.framework.module.TYPE"
    val MODULE_VERSION = "assistant.framework.module.VERSION"

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
    val ACTION_SAPPHIRE_CORE_REQUEST_DATA="assistant.framework.core.action.REQUEST_DATA"

    val ACTION_SAPPHIRE_MODULE_REGISTER = "assistant.framework.module.action.REGISTER"
    // This is for core to request data from a specific module
    val ACTION_SAPPHIRE_MODULE_REQUEST_DATA="assistant.framework.module.action.REQUEST_DATA"
    val ACTION_SAPPHIRE_TRAIN="assistant.framework.processor.action.TRAIN"

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
}