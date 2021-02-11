package com.example.multiprocessmodule

import android.content.Intent
import android.os.IBinder
import android.util.Log
import com.example.componentframework.SAFInstallService

class MultiprocessModuleInstallService: SAFInstallService(){
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

	fun registerModule(){
		var intent = Intent()
		intent = registerPackageName(intent,this.packageName)
		intent = registerVersion(intent, VERSION)
		super.registerModule(intent)
	}
}