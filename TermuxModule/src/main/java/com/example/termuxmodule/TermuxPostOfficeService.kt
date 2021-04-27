package com.example.termuxmodule

import android.content.Intent
import com.example.componentframework.SapphireFrameworkRegistrationService

class TermuxPostOfficeService: SapphireFrameworkRegistrationService() {
	val VERSION = "0.0.1"

	override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
		try {
			when(intent!!.action){
				ACTION_SAPPHIRE_MODULE_REGISTER -> registerModule(intent!!)
				else -> Log.i("There was some kind of error with the PostOfficeService")
			}
		}catch(exception: Exception){
			Log.e("There was some kind of error with the install intent")
		}
		return super.onStartCommand(intent, flags, startId)
	}

	override fun registerModule(intent: Intent){
		var returnIntent = Intent(intent)
		returnIntent.putExtra("ROUTE_NAME","${this.packageName};com.example.termuxmodule.TermuxService")
		returnIntent = registerRouteInformation(returnIntent,"${this.packageName};com.example.termuxmodule.TermuxService")
		returnIntent = registerVersion(returnIntent,VERSION)

		super.registerModule(returnIntent)
	}
}