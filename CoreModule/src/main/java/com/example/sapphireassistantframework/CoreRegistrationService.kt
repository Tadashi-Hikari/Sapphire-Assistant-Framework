package com.example.sapphireassistantframework

import android.content.Intent
import android.content.pm.PackageManager.GET_RESOLVED_FILTER
import android.content.pm.ResolveInfo
import android.os.IBinder
import com.example.componentframework.SapphireCoreService
import java.lang.Exception

class CoreRegistrationService: SapphireCoreService(){

	// I don't like that this is hardcoded. Make this populate in a default config, and read from the config
	val DEFAULT_MODULES = listOf(CORE,PROCESSOR,MULTIPROCESS)

	var sapphireModuleStack = mutableListOf<Intent>()

	override fun onCreate() {
		super.onCreate()
		Log.i(CLASS_NAME,"Starting registration service")
	}

	override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
		when(intent?.action){
			ACTION_SAPPHIRE_INITIALIZE -> scanModules()
			ACTION_SAPPHIRE_MODULE_REGISTER -> registerModule(intent)
			else -> Log.e(this.javaClass.name, "There was an issue with the registration intent. Dispatching remaining intents ")
		}
		dispatchRemainingIntents()
		return super.onStartCommand(intent, flags, startId)
	}

	fun dispatchRemainingIntents(){
		if(sapphireModuleStack.isNotEmpty()){
			// Pop it from the stack, and dispatch it.
			// Do I need to redirect this to core? ugh, I think I do
			Log.i(CLASS_NAME,"Dispatching ${sapphireModuleStack.last().getStringExtra(MODULE_CLASS)!!}")
			returnSapphireService(sapphireModuleStack.removeLast())
		}else{
			Log.i(CLASS_NAME,"All modules registered")
		}
	}

	fun scanModules(){
		var templateIntent = Intent().setAction(ACTION_SAPPHIRE_MODULE_REGISTER)
		var availableSapphireModules = this.packageManager.queryIntentServices(templateIntent,GET_RESOLVED_FILTER)
		Log.i(CLASS_NAME,"${availableSapphireModules.size} modules found")

		for(module in availableSapphireModules){
			try{
				var packageName = module.serviceInfo.packageName
				var className = module.serviceInfo.name
				// This will get pushed to a list, and popped off to register all intents
				var registrationIntent = Intent(templateIntent)
				// Do I explicitly need this?
				registrationIntent.putExtra(MODULE_PACKAGE,packageName)
				registrationIntent.putExtra(MODULE_CLASS,className)
				// Add it to the stack (yes, I know it's not a literal stack)
				sapphireModuleStack.add(registrationIntent)
			}catch(exception: Exception){
				Log.d(CLASS_NAME,exception.toString())
				continue
			}
		}
	}

	fun registerModule(intent: Intent?){
		Log.i(CLASS_NAME,"Registering intent")
		if(newVersion()){
			registerRoute()
			registerDefaults(null)
			registerFilenames()
			registerBackgroundService()
		}
	}

	fun newVersion(): Boolean{
		return true
	}

	fun registerDefaults(intent: Intent?){
	}

	fun registerBackgroundService(){

	}

	fun registerRoute(){

	}

	fun registerFilenames(){

	}
}