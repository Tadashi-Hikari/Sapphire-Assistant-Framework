package com.example.sapphireassistantframework

import android.content.Intent
import android.os.IBinder
import com.example.componentframework.SapphireCoreService

class CoreRegistrationServiceRefined: SapphireCoreService(){

	override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
		when(intent?.action){
			ACTION_SAPPHIRE_INITIALIZE -> scanModules()
			ACTION_SAPPHIRE_MODULE_REGISTER -> registerModule()
			else -> Log.e(this.javaClass.name, "There was an issue with the registration intent. Dispatching remaining intents ")
		}
		dispatchRemainingIntents()
		return super.onStartCommand(intent, flags, startId)
	}

	fun scanModules(){
	}

	fun dispatchRemainingIntents(){
	}

	fun registerModule(){
		if(newVersion()){
			registerRoute()
			registerDefaults()
			registerFilenames()
			registerBackgroundService()
		}
	}

	fun newVersion(): Boolean{
		return true
	}

	fun registerDefaults(){

	}

	fun registerBackgroundService(){

	}

	fun registerRoute(){

	}

	fun registerFilenames(){

	}

	override fun onBind(intent: Intent?): IBinder? {
		TODO("Not yet implemented")
	}
}