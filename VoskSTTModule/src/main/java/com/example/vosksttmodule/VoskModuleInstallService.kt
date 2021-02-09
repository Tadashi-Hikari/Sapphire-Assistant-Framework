package com.example.vosksttmodule

import android.content.Intent
import android.os.IBinder
import com.example.componentframework.SAFInstallService
import com.example.componentframework.SAFService

// This could be a SAFInstallService
class VoskModuleInstallService: SAFInstallService(){
	val VERSION = "0.0.1"

	// Hmm, I don't know if I like how super.registerModule works
	fun registerModule(){
		var intent = Intent()
		intent = registerVersion(intent, VERSION)
		intent = registerType(intent, INPUT)
		super.registerModule(intent)
	}

	override fun onBind(intent: Intent?): IBinder? {
		TODO("Not yet implemented")
	}
}