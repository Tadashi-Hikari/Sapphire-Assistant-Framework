package com.example.sapphireassistantframework

import android.content.Intent
import android.content.pm.PackageManager.GET_RESOLVED_FILTER
import android.os.IBinder
import com.example.componentframework.SAFService
import org.json.JSONObject
import java.io.File
import java.lang.Exception
import java.util.*

/*
I still need to implement the write to file mechanism
 */

class CoreRegistrationService: SAFService(){
	private val CONFIG = "sample-core-config.conf"
	//Why does this exist?
	private val DATABASE = "core.db"

	// These are table names
	private var REGISTRATION_TABLE = "registration.tbl"
	private val DEFAULT_MODULES_TABLE = "defaultmodules.tbl"
	private val STARTUP_TABLE = "background.tbl"
	private val ROUTE_TABLE = "routetable.tbl"
	private val ALIAS_TABLE = "alias.tbl"
	val CONFIG_VAL_DATA_TABLES = "datatables.tbl"

	// These are the config modules
	var jsonRegistrationTable = JSONObject()
	var jsonDefaultModules = JSONObject()
	var jsonBackgroundStartup = JSONObject()
	var jsonAliasTable = JSONObject()
	var jsonRouteTable = JSONObject()

	// I don't like that this is hardcoded. Make this populate in a default config, and read from the config
	val DEFAULT_MODULES = listOf(CORE,PROCESSOR,MULTIPROCESS)

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

	val ACTION_SAPPHIRE_INITIALIZE="assistant.framework.processor.action.INITIALIZE"
	override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
		try {
			Log.v(this.javaClass.name,"CoreRegistrationIntent received")
			if(intent!!.action == ACTION_SAPPHIRE_INITIALIZE){
				Log.v(this.javaClass.name,"initializing...")
				// This is the Init action
				initializing = true
				scanInstalledModules()
			}else if(intent?.action == ACTION_SAPPHIRE_MODULE_REGISTER){
				Log.v(this.javaClass.name,"Registration action received. Registering...")
				if(intent.hasExtra(MODULE_PACKAGE) and intent.hasExtra(MODULE_CLASS)) {
					Log.v(this.javaClass.name,"Intent has MODULE_PACKAGE and MODULE_CLASS...")
					installPackageName = intent!!.getStringExtra(MODULE_PACKAGE)!!
					installClassName = intent!!.getStringExtra(MODULE_CLASS)!!
				}else{
					Log.v(this.javaClass.name,"Intent doesn't have a labeled package/module!!!")
					Log.v(this.javaClass.name,intent?.extras.toString())
				}
				// Change this to registerModule()? It does registration and re-registration
				startValidationProcess(intent!!)
			}
			// Is this too much writing? I think I'm just reading from routeTable in core
			var registrationFile = File(filesDir,REGISTRATION_TABLE)
			registrationFile.writeText(jsonRegistrationTable.toString())

			//Check if there's an installation stack when finished
			if(sapphireModuleStack.isNotEmpty()){
				Log.v(this.javaClass.name,"Dispatching a new registration service from the registration stack")
				// This will basically trigger itself as a loop, until finished
				startSAFInstallService(sapphireModuleStack.pop())
			}else{
				if(initializing) {
					Log.v(this.javaClass.name,"All services have been registered. Continuing...")
					var finished = Intent().setAction(ACTION_SAPPHIRE_CORE_REGISTRATION_COMPLETE)
					finished.setClassName(this.packageName, "${this.packageName}.CoreService")

					// Added ad-hoc for core
					var dataTable = JSONObject()
					dataTable.put(ROUTE_TABLE,"")
					dataTable.put(STARTUP_TABLE,"")
					dataTable.put(ALIAS_TABLE,"")
					dataTable.put(DEFAULT_MODULES_TABLE,"")
					var file = File(filesDir,CONFIG_VAL_DATA_TABLES)
					file.writeText(dataTable.toString())

					startSAFInstallService(finished)
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
		jsonBackgroundStartup = loadJSONTable(STARTUP_TABLE)
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

		// to check for modules with installers. What am I searching for here?
		for (module in availableSapphireModules) {
			try {
				var packageName = module.serviceInfo.packageName
				var className = module.serviceInfo.name
				//module.filter.actionsIterator().forEach { action -> Log.d(this.javaClass.name,"${className}: ${action.toString()}")}

				// Let CoreRegistrationService handle all the checking.
				var registrationIntent = Intent(intent)
				registrationIntent.setClassName(packageName,className)
				registrationIntent.putExtra(MODULE_PACKAGE, packageName)
				registrationIntent.putExtra(MODULE_CLASS, className)
				// Push it to the stack, so it can be popped up
				Log.v(this.javaClass.name,"registrationIntent for ${packageName};${className} created. Pushing to stack...")
				sapphireModuleStack.push(registrationIntent)
			} catch (exception: Exception) {
				continue
			}
		}

	}

	fun startValidationProcess(intent: Intent){
		Log.v(this.javaClass.name,"Starting validation process...")
		if(jsonRegistrationTable.has("${installPackageName};${installClassName}")){
			Log.v(this.javaClass.name,"Module already registered...")
			checkForUpdates(intent)
		}else{
			Log.v(this.javaClass.name,"Module not yet registered...")
			// In theory, this could check if a module is *no longer* a certain type, but for now it only checks new modules
			checkDefaults(intent)
			var registration = registerModule(intent)
			jsonRegistrationTable.put("${installPackageName};${installClassName}",registration)
			// Why is this not triggering?
			if(intent.hasExtra("BACKGROUND")){
				Log.i(this.javaClass.name,"This intent has a startup service")
				var backgroundInfo = JSONObject(intent.getStringExtra("BACKGROUND"))
				registerBackgroundService(backgroundInfo)
				var backgroundFile = File(filesDir,STARTUP_TABLE)
				backgroundFile.writeText(jsonBackgroundStartup.toString())
			}
			registerRoute(intent)
		}
	}

	fun registerRoute(intent: Intent){
		// This is telling it to call itself, due to ROUTE being used for background service
		var routeData = intent.getStringExtra(ROUTE)
		// This is being used for the ROUTE id, so it can be looked up.
		var routeName = intent.getStringExtra("ROUTE_NAME")
		Log.v(this.javaClass.name,"Registering ${routeName} as going to route ${routeData}")
		jsonRouteTable.put(routeName,routeData)
		var file = File(filesDir,ROUTE_TABLE)
		file.writeText(jsonRouteTable.toString())
	}

	fun registerModule(intent: Intent): JSONObject{
		Log.v(this.javaClass.name,"Registering module...")
		var registration = JSONObject()

		// Just register the keys value. shouldn't be complex data.
		for(key in intent.extras!!.keySet()){
			registration.put(key,intent.getStringExtra(key))
		}
		return registration
	}

	fun registerBackgroundService(backgroundInfo: JSONObject){
		Log.v(this.javaClass.name,"Registering background service...")
		// I suppose I am just outright copying the data here
		jsonBackgroundStartup.put(backgroundInfo.getString("registration_id"),backgroundInfo.toString())
	}

	fun checkForUpdates(intent: Intent){
		Log.v(this.javaClass.name,"checking on updates for ${installClassName}")
		var registration = jsonRegistrationTable.optJSONObject("${installPackageName};${installClassName}")
		if(registration.getString(MODULE_VERSION) != intent.getStringExtra(MODULE_VERSION)){
			registerModule(intent)
		}
	}

	fun checkDefaults(intent: Intent){
		Log.v(this.javaClass.name,"Checking defaults...")
		var changed = false
		for(key in DEFAULT_MODULES) {
			if(intent.hasExtra(MODULE_TYPE)) {
				var type = intent.getStringExtra(MODULE_TYPE)
				Log.v(this.javaClass.name,"testing default data for ${key}: ${installPackageName};${installClassName}...")
				if ((type == key) and (jsonDefaultModules.optString(key).isNullOrBlank())
				) {
					Log.i(this.javaClass.name,"Match found for ${key}. Saving default data...")
					jsonDefaultModules.put(key, "${installPackageName};${installClassName}")
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