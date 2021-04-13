package com.example.processormodule

import android.content.Intent
import com.example.componentframework.SapphireFrameworkRegistrationService
import com.example.componentframework.depreciated.SAFInstallService
import java.lang.Exception

class ProcessorModuleInstallService: SapphireFrameworkRegistrationService(){
	val VERSION = "0.0.1"

	override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
		try {
			if (intent!!.action == ACTION_SAPPHIRE_MODULE_REGISTER) {
				registerModule(intent)
			}
		}catch(exception: Exception){
			Log.i("VoskModuleInstallService","There was some kind of error with the install intent")
		}
		return super.onStartCommand(intent, flags, startId)
	}

	override fun registerModule(intent: Intent){
		var returnIntent = Intent(intent)
		returnIntent.putExtra(MODULE_PACKAGE,this.packageName)
		returnIntent.putExtra(MODULE_CLASS,"com.example.processormodule.ProcessorCentralService")

		var route = "${this.packageName};com.example.processormodule.ProcessorCentralService"
		returnIntent.putExtra("ROUTE_NAME",route)
		returnIntent = registerRouteInformation(returnIntent, route)
		returnIntent = registerVersion(returnIntent, VERSION)
		registerModuleType(returnIntent,PROCESSOR)
		super.registerModule(returnIntent)
	}
}