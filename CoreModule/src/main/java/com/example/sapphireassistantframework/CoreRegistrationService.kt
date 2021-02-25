package com.example.sapphireassistantframework

import android.content.Intent
import android.content.pm.PackageManager.GET_RESOLVED_FILTER
import android.content.pm.ResolveInfo
import android.os.IBinder
import android.util.Log
import com.example.componentframework.SAFService
import org.json.JSONObject
import java.io.File
import java.lang.Exception
import java.util.*

/*
I still need to implement the write to file mechanism
 */

class CoreRegistrationService: SAFService(){
	private val CONFIG = "core.conf"
	//Why does this exist?
	private val DATABASE = "core.db"

	/*
	I can probably do a lot of these programmatically
	 */

	// These are table names
	private var REGISTRATION_TABLE = "registration.tbl"
	private val DEFAULT_MODULES_TABLE = "defaultmodules.tbl"
	private val BACKGROUND_STARTUP_TABLE = "background.tbl"
	private val ROUTE_TABLE = "routetable.tbl"
	private val ALIAS_TABLE = "alias.tbl"

	// These are the config modules
	var jsonRegistrationTable = JSONObject()
	var jsonDefaultModules = JSONObject()
	var jsonBackgroundStartup = JSONObject()
	var jsonAliasTable = JSONObject()
	var jsonRouteTable = JSONObject()

	// I don't think I need to define these, cause they'll be defined on install. I just need to check if they're populated
	// These are the DEFAULT module package;class
	private val DEFAULT_CORE = "com.example.sapphireassistantframework;com.example.sapphireassistantframework.PostOffice"
	private val DEFAULT_PROCESSOR = "com.example.processormodule;com.example.processormodule.ProcessorService"
	private val DEFAULT_MULTIPROCESS = "com.example.multiprocessmodule;com.example.multiprocessmodule.MultiprocessService"
	// I don't like that this is hardcoded
	val DEFAULT_MODULES = listOf(DEFAULT_CORE,DEFAULT_PROCESSOR,DEFAULT_MULTIPROCESS)


	// This is loaded from a file
	var defaultsTable = JSONObject()
	var sapphireModuleStack = LinkedList<Intent>()

	lateinit var installPackageName: String
	lateinit var installClassName: String
	// I am expecting this will be in the background long enough for this whole process, but it could be a source of a bug
	var initializing = false

	override fun onCreate() {
		var configJSON = parseConfigFile(CONFIG)
		readConfigJSON(configJSON)
		super.onCreate()
	}

	override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
		try {
			Log.v(this.javaClass.name,"CoreRegistrationIntent received")
			if(intent?.action == null){
				Log.v(this.javaClass.name,"No action specified, initializing...")
				// This is the Init action
				initializing = true
				scanInstalledModules()
			}else if(intent?.action == ACTION_SAPPHIRE_MODULE_REGISTER){
				installPackageName = intent!!.getStringExtra(MODULE_PACKAGE)!!
				installClassName = intent!!.getStringExtra(MODULE_CLASS)!!
				// Change this to registerModule()? It does registration and re-registration
				startValidationProcess(intent!!)
			}
			// Is this too much writing?
			var registrationFile = File(filesDir,REGISTRATION_TABLE)
			registrationFile.writeText(jsonRegistrationTable.toString())

			//Check if there's an installation stack when finished
			if(sapphireModuleStack.isNotEmpty()){
				// This will basically trigger itself as a loop, until finished
				startService(sapphireModuleStack.pop())
			}else{
				if(initializing) {
					var finished = Intent().setAction(ACTION_SAPPHIRE_CORE_REGISTRATION_COMPLETE)
					finished.setClassName(this, "${this.packageName}.CoreService")
					startService(finished)
					initializing = false
				}
			}
		}catch (exception:Exception){
			Log.v(this.javaClass.name,exception.toString())
			Log.e("CoreRegistrationService","Error installing module")
		}
		return super.onStartCommand(intent, flags, startId)
	}

	override fun onBind(intent: Intent?): IBinder? {
		TODO("Not yet implemented")
	}

	fun readConfigJSON(jsonConfig: JSONObject){
		// These should be global vars, so I really just need to load them.
		// I need to account for just directly loading these, not having them in a separate config
		jsonRegistrationTable = loadJSONTable(REGISTRATION_TABLE)
		jsonDefaultModules = loadJSONTable(DEFAULT_MODULES_TABLE)
		jsonBackgroundStartup = loadJSONTable(BACKGROUND_STARTUP_TABLE)
		jsonRouteTable = loadJSONTable(ROUTE_TABLE)
		jsonAliasTable = loadJSONTable(ALIAS_TABLE)
	}

	fun scanInstalledModules() {
		Log.v(this.javaClass.name,"Scanning for modules installed on device")
		var intent = Intent().setAction(ACTION_SAPPHIRE_MODULE_REGISTER)
		// This is installing itself
		//intent.setClassName(this.packageName,"${this.packageName}.CoreModuleInstallService")
		var availableSapphireModules = this.packageManager.queryIntentServices(intent,GET_RESOLVED_FILTER)
		Log.v(this.javaClass.name,"${availableSapphireModules.size} modules found")
		Log.d(this.javaClass.name,availableSapphireModules.toString())

		// to check for modules with installers. What am I searching for here?
		for (module in availableSapphireModules) {
			try {
				var packageName = module.serviceInfo.packageName
				var className = module.serviceInfo.name
				module.filter.actionsIterator().forEach { action -> Log.d(this.javaClass.name,"${className}: ${action.toString()}")}

				// Let CoreRegistrationService handle all the checking.
				var registrationIntent = Intent(intent)
				registrationIntent.setClassName(packageName,className)
				registrationIntent.putExtra(MODULE_PACKAGE, packageName)
				registrationIntent.putExtra(MODULE_CLASS, className)
				// Push it to the stack, so it can be popped up
				sapphireModuleStack.push(registrationIntent)
			} catch (exception: Exception) {
				continue
			}
		}

	}

	fun startValidationProcess(intent: Intent){
		if(jsonRegistrationTable.has("${installPackageName}:${installClassName}")){
			checkForUpdates(intent)
		}else{
			// In theory, this could check if a module is *no longer* a certain type, but for now it only checks new modules
			checkDefaults(intent)
			var registration = registerModule(intent)
			jsonRegistrationTable.put("${installPackageName}:${installClassName}",registration)
			// I don't like that this isn't the same level as the rest
			if(intent.hasExtra("BACKGROUND")){
				var backgroundInfo = JSONObject(intent.getStringExtra("BACKGROUND"))
				registerBackgroundService(backgroundInfo)
				var backgroundFile = File(filesDir,BACKGROUND_STARTUP_TABLE)
				backgroundFile.writeText(jsonBackgroundStartup.toString())
			}
			registerRoute(intent)
		}
	}

	fun registerRoute(intent: Intent){
		var routeData = intent.getStringExtra(ROUTE)
		var routeName = intent.getStringExtra("ROUTE_NAME")
		jsonRouteTable.put(routeName,routeData)
	}

	fun registerModule(intent: Intent): JSONObject{
		var registration = JSONObject()

		// Just register the keys value. shouldn't be complex data.
		for(key in intent.extras!!.keySet()){
			registration.put(key,intent.getStringExtra(key))
		}
		return registration
	}

	fun registerBackgroundService(backgroundInfo: JSONObject){
		// I suppose I am just outright copying the data here
		jsonBackgroundStartup.put(backgroundInfo.getString("registration_id"),backgroundInfo)
	}

	fun checkForUpdates(intent: Intent){
		Log.v(this.javaClass.name,"checking on updates for ${installClassName}")
		var registration = jsonRegistrationTable.optJSONObject("${installPackageName}:${installClassName}")
		if(registration.getString(MODULE_VERSION) != intent.getStringExtra(MODULE_VERSION)){
			registerModule(intent)
		}
	}

	// I KNOW this can be more efficient.
	fun checkDefaults(intent: Intent){
		var changed = false
		for(key in DEFAULT_MODULES){
			if(jsonDefaultModules.getString(key).isNullOrBlank()){
				if(key == intent.getStringExtra(MODULE_TYPE)){
					jsonDefaultModules.put(key, "${installPackageName}:${installClassName}")
					changed = true
				}
			}
		}

		if(changed == true){
			var defaultsFile = File(filesDir,DEFAULT_MODULES_TABLE)
			defaultsFile.writeText(jsonDefaultModules.toString())
		}
	}
}