package com.example.componentframework

import android.content.Intent
import org.json.JSONObject
import java.lang.Exception

abstract class SapphireCoreService: SapphireFrameworkService(){

	private val CONFIG = "sample-core-config.conf"

	private var REGISTRATION_TABLE = "registration.tbl"
	private val DEFAULT_MODULES_TABLE = "defaultmodules.tbl"
	private val STARTUP_TABLE = "background.tbl"
	private val ROUTE_TABLE = "routetable.tbl"
	private val ALIAS_TABLE = "alias.tbl"

	fun passthroughService(intent: Intent){
		var postage = intent.getStringExtra(POSTAGE)!!
		postage = validatePostage(postage)
		intent.putExtra(POSTAGE,postage)
	}

	fun validatePostage(postage: String):String{
		var jsonDefaultModules = JSONObject()
		var jsonPostageTable = JSONObject()

		for(key in jsonDefaultModules.keys()){
			jsonPostageTable.put(key,jsonDefaultModules.getString(key))
		}
		Log.v(this.javaClass.name,"Postage is ${jsonPostageTable.toString()}")
		return jsonPostageTable.toString()
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