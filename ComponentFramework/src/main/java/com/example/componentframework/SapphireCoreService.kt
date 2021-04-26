package com.example.componentframework

import android.content.Intent
import android.content.ServiceConnection
import android.os.SystemClock
import org.json.JSONObject
import java.lang.Exception

abstract class SapphireCoreService: SapphireFrameworkService(){

	private val CONFIG = "sample-core-config.conf"

	private var REGISTRATION_TABLE = "registration.tbl"
	private val DEFAULT_MODULES_TABLE = "defaultmodules.tbl"
	private val STARTUP_TABLE = "background.tbl"
	private val ROUTE_TABLE = "routetable.tbl"
	private val ALIAS_TABLE = "alias.tbl"

	// Hmm.... This is close to just passthrough
	fun passthroughService(intent: Intent){
		var postage = intent.getStringExtra(POSTAGE)!!
		postage = validatePostage(postage)
		intent.putExtra(POSTAGE,postage)
	}

	fun cleanRoute(intent: Intent):Intent{
		if(intent.hasExtra(ROUTE)) {
			Log.v("Cleaning ROUTE: ${intent.getStringExtra(ROUTE)}")
			var route = intent.getStringExtra(ROUTE)
			while ((route?.first() == ',') or (route?.first() == ' ')) {
				route!!.removeRange(0, 0)
			}
			while ((route?.last() == ',') or (route?.last() == ' ')) {
				route!!.removeRange(route.length,route.length)
			}
			var newRoute = ""
			var tempRoute = route!!.split(",")
			for(module in tempRoute.withIndex()){
				Log.v("Checking module: ${module.value}")
				if(module.value == "null"){
					continue
				}else {
					when (module.index) {
						0 -> newRoute += module.value
						else -> newRoute += ",${module.value}"
					}
				}
			}

			intent.putExtra(ROUTE, newRoute)
			Log.v("Cleaned ROUTE: ${route}")
		}
		return intent
	}

	fun validatePostage(postage: String):String{
		var jsonDefaultModules = JSONObject()
		var jsonPostageTable = JSONObject()

		for(key in jsonDefaultModules.keys()){
			jsonPostageTable.put(key,jsonDefaultModules.getString(key))
		}
		Log.v("Postage is ${jsonPostageTable.toString()}")
		return jsonPostageTable.toString()
	}

	fun expandRoute(route: String): String{
		var routeList = route.split(",").toMutableList()
		// I don't like that this'll load each time. Can I avoid this somehow?
		var variables = loadTable(DEFAULT_MODULES_TABLE)
		Log.v("Defaults tables: ${variables}")
		Log.v("Expanding route ${routeList}")
		// This also doesn't seem to be working
		for(module in routeList.withIndex()){
			Log.v("Checking module ${module.value}")
			if(variables.has(module.value)){
				Log.v("This is a match!")
				routeList.set(module.index,variables.getString(module.value))
			}
		}
		return routeList.joinToString { String -> String }
	}

	// This is only meant to be used by core
	fun startRegistrationService(connection: ServiceConnection, intent: Intent){
		bindService(intent,connection, BIND_AUTO_CREATE)
		// I just need enough time for the service to init, and be non-background
		SystemClock.sleep(700)
		startService(intent)
	}

	fun startPendingService(){
	}
}