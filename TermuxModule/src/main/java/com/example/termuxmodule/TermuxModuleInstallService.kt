package com.example.termuxmodule

import android.content.Intent
import android.util.Log
import com.example.componentframework.SAFInstallService

class TermuxModuleInstallService: SAFInstallService() {
	val VERSION = "0.0.1"

	override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
		try {
			if (intent!!.action == ACTION_SAPPHIRE_MODULE_REGISTER) {
				registerModule(intent!!)
			}
		}catch(exception: Exception){
			Log.e(this.javaClass.name,"There was some kind of error with the install intent")
		}
		return super.onStartCommand(intent, flags, startId)
	}

	override fun registerModule(intent: Intent){
		var returnIntent = Intent(intent)
		// This is supposed to be the default service
		returnIntent.putExtra(MODULE_PACKAGE,this.packageName)
		returnIntent.putExtra(MODULE_CLASS,"com.example.termuxmodule.TermuxService")

		returnIntent.putExtra("ROUTE_NAME","${this.packageName};com.example.termuxmodule.TermuxService")
		returnIntent = registerRouteInformation(returnIntent,"${this.packageName};com.example.termuxmodule.TermuxService")
		returnIntent = registerVersion(returnIntent,VERSION)

		super.registerModule(returnIntent)
	}
}