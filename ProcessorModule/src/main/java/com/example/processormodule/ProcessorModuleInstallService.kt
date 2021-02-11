package com.example.processormodule

import android.content.Intent
import android.os.IBinder
import android.util.Log
import com.example.componentframework.SAFInstallService
import com.example.componentframework.SAFService
import java.lang.Exception

class ProcessorModuleInstallService: SAFInstallService(){
	val VERSION = "0.0.1"

	override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
		try {
			if (intent!!.action == ACTION_SAPPHIRE_MODULE_REGISTER) {
				registerModule()
			}
		}catch(exception: Exception){
			Log.i("VoskModuleInstallService","There was some kind of error with the install intent")
		}
		return super.onStartCommand(intent, flags, startId)
	}

	// Hmm, I don't know if I like how super.registerModule works
	fun registerModule(){
		var intent = Intent()
		intent = registerVersion(intent, VERSION)
		intent = registerPackageName(intent, this.packageName)
		super.registerModule(intent)
	}
}