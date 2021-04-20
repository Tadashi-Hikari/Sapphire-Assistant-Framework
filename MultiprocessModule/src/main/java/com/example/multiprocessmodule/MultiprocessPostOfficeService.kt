package com.example.multiprocessmodule

import android.content.Intent
import com.example.componentframework.SapphireFrameworkRegistrationService
import com.example.componentframework.depreciated.SAFInstallService

class MultiprocessPostOfficeService: SapphireFrameworkRegistrationService(){
	val VERSION = "0.0.1"

	override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
		try {
			when(intent!!.action) {
				ACTION_SAPPHIRE_MODULE_REGISTER -> registerModule(intent)
				// The default is to just forward it along
				else -> passthrough(intent,"MultiprocessService")
			}
		}catch(exception: Exception){
			Log.i(CLASS_NAME,"There was some kind of error with the install intent")
		}
		return super.onStartCommand(intent, flags, startId)
	}

	override fun registerModule(intent: Intent){
		Log.v(CLASS_NAME,"Registering ${PACKAGE_NAME}")
		var registerIntent = Intent(intent)
		registerIntent = registerModuleType(registerIntent,MULTIPROCESS)
		registerIntent = registerVersion(registerIntent, VERSION)
		super.registerModule(registerIntent)
	}
}