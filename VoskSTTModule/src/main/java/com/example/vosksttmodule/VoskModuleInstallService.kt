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
		returnIntent = registerBackgroundServices(returnIntent,backgroundData.toString())
		// What is this even doing?
		returnIntent.putExtra("ROUTE_NAME","${this.packageName};${this.packageName}.KaldiService")
		returnIntent.putExtra(MODULE_PACKAGE,this.packageName)
		// Not needed, cause it's set in the CoreRegistrationService. This will be an issue w/ multiple entries though
		returnIntent.putExtra(MODULE_CLASS,"${this.packageName}.KaldiService")
		registerRouteInformation(returnIntent, routeData)
		registerModuleType(returnIntent,INPUT)
		registerVersion(returnIntent, VERSION)
		super.registerModule(returnIntent)
	}
}