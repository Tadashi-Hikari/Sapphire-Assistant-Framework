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

	fun expandRoute(route: String): String{
		var routeList = route.split(",").toMutableList()
		// I don't like that this'll load each time. Can I avoid this somehow?
		var variables = loadTable(DEFAULT_MODULES_TABLE)

		for(module in routeList.withIndex()){
			if(variables.has(module.value)){
				routeList.set(module.index,variables.getString(module.value))
			}
		}

		return routeList.joinToString { String -> String }
	}
}