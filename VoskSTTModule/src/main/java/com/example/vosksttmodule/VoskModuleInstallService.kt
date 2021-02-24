package com.example.vosksttmodule

import android.content.Intent
import android.os.IBinder
import android.util.Log
import com.example.componentframework.SAFInstallService
import com.example.componentframework.SAFService
import org.json.JSONObject
import java.lang.Exception

// This could be a SAFInstallService
class VoskModuleInstallService: SAFInstallService(){
	val VERSION = "0.0.1"

	override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
		try {
			if (intent!!.action == ACTION_SAPPHIRE_MODULE_REGISTER) {
				registerModule(intent!!)
			}
		}catch(exception: Exception){
			Log.i("VoskModuleInstallService","There was some kind of error with the install intent")
		}
		return super.onStartCommand(intent, flags, startId)
	}

	override fun registerModule(intent: Intent){
		var startupRoute = "${this.packageName};com.example.vosksttmodule.KaldiService"
		// I don't like this, It seems clunky.
		var backgroundData = JSONObject()
		backgroundData.put(ROUTE,startupRoute)
		backgroundData.put("bound",true)
		backgroundData.put("registration_id","VoskModule")

		// This is supposed to be a variable
		var routeData = PROCESSOR

		var returnIntent = Intent(intent)
		registerBackgroundServices(intent,backgroundData.toString())
		// Do I need to do this?
		intent.putExtra("ROUTE_NAME","${this.packageName};${this.packageName}.KaldiService")
		registerRouteInformation(intent, routeData)
		registerModuleType(intent,INPUT)
		registerVersion(intent, VERSION)
		super.registerModule(returnIntent)
	}
}