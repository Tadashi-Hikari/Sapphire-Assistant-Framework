package com.example.componentframework

import android.app.Service
import android.content.Intent
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.io.InputStreamReader
import java.net.DatagramSocketImpl
import java.net.InetAddress
import java.net.Socket

abstract class SapphireFrameworkService: Service() {
	inner class LogOverride{
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

	val MESSAGE="assistant.framework.protocol.MESSAGE"
	val STDERR="assistant.framework.protocol.STDERR"
	// This is going to be for ENV_VARIABLES
	val POSTAGE="assistant.framework.protocol.POSTAGE"
	val ROUTE="assistant.framework.protocol.ROUTE"
	val FROM= "assistant.framework.protocol.FROM"
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
	val ACTION_SAPPHIRE_EXPORT_CONFIG = "assistant.framework.module.action.EXPORT_CONFIG"
	// This is for core to request data from a specific module
	val ACTION_SAPPHIRE_TRAIN="assistant.framework.processor.action.TRAIN"
	val ACTION_SAPPHIRE_INITIALIZE="assistant.framework.processor.action.INITIALIZE"

	// Work in progress
	var ACTION_SAPPHIRE_DATA_TRANSFER = "ACTION_SAPPHIRE_DATA_TRANSFER"
	var ACTION_MANIPULATE_FILE_DATA = "action.framework.module.MANIPULATE_FILE_DATA"
	var ACTION_REQUEST_FILE_DATA = "action.framework.module.REQUEST_FILE_DATA"
	val GUI_BROADCAST = "assistant.framework.broadcast.GUI_UPDATE"

	var jsonPostage = JSONObject()

	fun loadTable(tablename: String): JSONObject{
		var moduleJsonDataTable = JSONObject()

		var file = File(filesDir,tablename)
		moduleJsonDataTable = JSONObject(file.readText())

		return moduleJsonDataTable
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

	fun broadcastStatus(name: String, message:String) {
		var notifyIntent = Intent()
		notifyIntent.putExtra(MESSAGE, "${name}: ${message}")
		notifyIntent.setAction(GUI_BROADCAST)
		sendBroadcast(notifyIntent)
	}

	fun dispatchSapphireServiceFromCore(intent: Intent){
		var updatedIntent = Intent(intent)
		updatedIntent = validatePostage(intent)
		startService(updatedIntent)
	}

	// The name is a little overkill, but w/e
	fun dispatchSapphireServiceToCore(intent:Intent){
		intent.setClassName(this,"${this.packageName}.CoreService")
		startService(intent)
	}

	fun validatePostage(intent: Intent): Intent{
		var jsonDefaultModules = JSONObject()
		for(key in jsonDefaultModules.keys()){
			jsonPostage!!.put(key,jsonDefaultModules.getString(key))
		}
		Log.v(this.javaClass.name,"Postage is ${jsonPostage.toString()}")
		intent.putExtra(POSTAGE,jsonPostage!!.toString())

		return intent
	}

	// This is for having a SAF compontent pass along the route w/o a callback to core
	fun parseRoute(string: String): List<String>{
		var route = string.split(",")
		return route
	}
}