package com.example.multiprocessmodule

import android.content.Intent
import com.example.componentframework.SapphireFrameworkRegistrationService
import com.example.componentframework.depreciated.SAFInstallService

class MultiprocessModuleInstallService: SapphireFrameworkRegistrationService(){
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
		var registerIntent = Intent(intent)
		registerIntent.putExtra(MODULE_PACKAGE,this.packageName)
		registerIntent.putExtra(MODULE_CLASS,"${this.packageName}.MultiprocessService")
		registerModuleType(registerIntent,MULTIPROCESS)
		registerIntent = registerVersion(registerIntent, VERSION)
		super.registerModule(registerIntent)
	}
}