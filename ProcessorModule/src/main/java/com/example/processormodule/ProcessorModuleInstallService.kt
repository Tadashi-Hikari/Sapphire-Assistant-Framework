package com.example.processormodule

import android.content.Intent
import android.os.IBinder
import com.example.componentframework.SAFInstallService
import com.example.componentframework.SAFService

class ProcessorModuleInstallService: SAFInstallService(){
	val VERSION = "0.0.1"

	// Hmm, I don't know if I like how super.registerModule works
	fun registerModule(){
		var intent = Intent()
		intent = registerVersion(intent, VERSION)
		intent = registerType(intent, PROCESSOR)
		//intent = registerData(intent, filenames: List<String>)
		intent.putExtra(MODULE_PACKAGE,this.packageName)
		super.registerModule(intent)
	}

	override fun onBind(intent: Intent?): IBinder? {
		TODO("Not yet implemented")
	}
}