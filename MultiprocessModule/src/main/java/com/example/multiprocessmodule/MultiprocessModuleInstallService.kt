package com.example.multiprocessmodule

import android.content.Intent
import android.os.IBinder
import com.example.componentframework.SAFInstallService

class MultiprocessModuleInstallService: SAFInstallService(){
	val VERSION = "0.0.1"

	fun registerModule(){
		var intent = Intent()
		intent = registerType(intent, MULTIPROCESS)
		intent = registerVersion(intent, VERSION)
		super.registerModule(intent)
	}

	override fun onBind(intent: Intent?): IBinder? {
		TODO("Not yet implemented")
	}
}