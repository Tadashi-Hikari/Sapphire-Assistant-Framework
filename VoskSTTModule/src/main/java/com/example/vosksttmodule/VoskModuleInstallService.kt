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
		// I don't like this, It seems clunky.
		var routeData = "${this.packageName};com.example.vosksttmodule.KaldiService"

		var returnIntent = Intent(intent)
		registerBackgroundServices(intent,routeData)
		registerModuleType(intent,INPUT)
		registerVersion(intent, VERSION)
		super.registerModule(returnIntent)
	}
}