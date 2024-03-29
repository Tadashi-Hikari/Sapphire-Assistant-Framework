package com.example.processormodule

import android.content.Intent
import com.example.componentframework.SapphireFrameworkRegistrationService
import java.lang.Exception

class ProcessorPostOfficeService: SapphireFrameworkRegistrationService(){
	val VERSION = "0.0.1"

	override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
		Log.v("Processor started")
		Log.v(intent?.extras.toString())
		try {
			when{
				intent!!.action == ACTION_SAPPHIRE_MODULE_REGISTER -> registerModule(intent)
				// Pass it right on through to the extra (temporary
				intent.hasExtra("PROCESSOR_EXTRA") -> passthrough(intent,intent.getStringExtra("PROCESSOR_EXTRA")!!)
				// This is temporary, just an example
				else -> passthrough(intent, "com.example.processormodule.ProcessorCentralService")
			}
		}catch(exception: Exception){
			Log.i("There was some kind of error with the install intent")
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
		returnIntent = registerModuleType(returnIntent,PROCESSOR)
		super.registerModule(returnIntent)
	}
}