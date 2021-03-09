package com.example.vosksttmodule

import android.content.Intent
import android.util.Log
import com.example.componentframework.SAFInstallService
import org.json.JSONObject
import java.lang.Exception

// This could be a SAFInstallService
class VoskModuleInstallService: SAFInstallService(){
	val VERSION = "0.0.1"

	override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
		try {
			if (intent!!.action == ACTION_SAPPHIRE_MODULE_REGISTER) {
				registerModule(intent!!)
			}
		}catch(exception: Exception){
			Log.e(this.javaClass.name,"There was some kind of error with the install intent")
		}
		return super.onStartCommand(intent, flags, startId)
	}

	override fun registerModule(intent: Intent){
		var startupRoute = "${this.packageName};com.example.vosksttmodule.KaldiService"
		// I don't like this, It seems clunky.
		var backgroundData = JSONObject()

		backgroundData.put(STARTUP_ROUTE,startupRoute)
		backgroundData.put("bound",true)
		// This is a default ID for when it is acting as an input module
		// Shit. will this conflict w/ default names?
		backgroundData.put("registration_id","VoskModule")

		var returnIntent = Intent(intent)
		returnIntent = registerBackgroundServices(returnIntent,backgroundData.toString())

		returnIntent.putExtra(MODULE_PACKAGE,this.packageName)
		// Not needed, cause it's set in the CoreRegistrationService. This will be an issue w/ multiple entries though
		returnIntent.putExtra(MODULE_CLASS,"com.example.vosksttmodule.KaldiService")
		returnIntent = registerVersion(returnIntent, VERSION)

		// This is the unique ROUTE_NAME, so that it can be looked up
		// Currently, this is the default. This is not currently used..., and it needs to be paired w/ route data
		returnIntent.putExtra("ROUTE_NAME","${this.packageName};com.example.vosksttmodule.KaldiService")
		// This is supposed to be a variable
		var routeData = PROCESSOR
		returnIntent = registerRouteInformation(returnIntent, routeData)
		returnIntent = registerModuleType(returnIntent,INPUT)
		//returnIntent = registerVersion(returnIntent, VERSION)
		super.registerModule(returnIntent)
	}
}