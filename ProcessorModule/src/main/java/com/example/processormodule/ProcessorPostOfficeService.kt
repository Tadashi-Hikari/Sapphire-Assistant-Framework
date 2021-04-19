package com.example.processormodule

import android.content.Intent
import com.example.componentframework.SapphireFrameworkRegistrationService
import com.example.componentframework.depreciated.SAFInstallService
import java.lang.Exception

class ProcessorPostOfficeService: SapphireFrameworkRegistrationService(){
	val VERSION = "0.0.1"

	override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
		try {
			when(intent!!.action) {
				ACTION_SAPPHIRE_MODULE_REGISTER -> registerModule(intent)
				"ACTION_PROCESSOR_TRAIN" -> passthrough(intent,"ProcessorTrainingService")
				"ACTION_PROCESSOR_CLASSIFY" -> passthrough(intent,"ProcessorCentralService")
				else -> Log.i(CLASS_NAME,"There was some kind of error with the PostOfficeService")
			}
		}catch(exception: Exception){
			Log.i(CLASS_NAME,"There was some kind of error with the install intent")
		}
		return super.onStartCommand(intent, flags, startId)
	}

	override fun registerModule(intent: Intent){
		var returnIntent = Intent(intent)
		// This will need to be edited w/ PendingIntent
		var route = "${this.packageName};com.example.processormodule.ProcessorCentralService"
		returnIntent.putExtra("ROUTE_NAME",route)
		returnIntent = registerRouteInformation(returnIntent, route)
		returnIntent = registerVersion(returnIntent, VERSION)
		registerModuleType(returnIntent,PROCESSOR)
		super.registerModule(returnIntent)
	}
}