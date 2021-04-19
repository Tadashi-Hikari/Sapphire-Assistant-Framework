package com.example.sapphireassistantframework

import android.app.PendingIntent
import android.content.Intent
import android.content.pm.PackageManager.GET_RESOLVED_FILTER
import com.example.componentframework.SapphireCoreService
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.lang.Exception

class CoreRegistrationService: SapphireCoreService(){

	// I don't like that this is hardcoded. Make this populate in a default config, and read from the config
	val DEFAULT_MODULES = listOf(CORE,PROCESSOR,MULTIPROCESS)

	var sapphireModuleStack = mutableListOf<Intent>()
	var dataKey = mutableListOf<String>()
	var pendingIntentLedger = Intent()

	/*
	These lists should be loaded/generated on the fly, and in a stack/list
	 */
	// These are table names
	private var REGISTRATION_TABLE = "registration.tbl"
	private val DEFAULT_MODULES_TABLE = "defaultmodules.tbl"
	private val STARTUP_TABLE = "background.tbl"
	private val ROUTE_TABLE = "routetable.tbl"
	private val ALIAS_TABLE = "alias.tbl"
	private val FILENAME_TABLE = "filenames.tbl"
	//val CONFIG_VAL_DATA_TABLES = "datatables.tbl"

	// The tables in use. May be trimmed later
	var registrationTable = JSONObject()
	var defaultModulesTable = JSONObject()
	var backgroundStartupTable = JSONObject()
	var aliasTable = JSONObject()
	var routeTable = JSONObject()
	// This is just temporary
	var filenameTable = JSONObject()

	override fun onCreate() {
		super.onCreate()
		Log.i(CLASS_NAME,"Starting registration service")
		loadAllTables()
	}

	override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
		when(intent?.action){
			ACTION_SAPPHIRE_INITIALIZE -> scanModules()
			ACTION_SAPPHIRE_MODULE_REGISTER -> registerModule(intent)
			else -> Log.e(this.javaClass.name, "There was an issue with the registration intent. Dispatching remaining intents")
		}
		dispatchRemainingIntents()
		return super.onStartCommand(intent, flags, startId)
	}

	fun dispatchRemainingIntents(){
		if(sapphireModuleStack.isNotEmpty()){
			// Pop it from the stack, and dispatch it.
			// Do I need to redirect this to core? ugh, I think I do
			Log.i(CLASS_NAME,"Dispatching ${sapphireModuleStack.last().getStringExtra(MODULE_CLASS)!!}")
			// Remove the last one in the list
			returnSapphireService(sapphireModuleStack.removeAt(sapphireModuleStack.size-1))
		}else{
			Log.i(CLASS_NAME,"All modules registered")
			var finalIntent = Intent()
			finalIntent.action = ACTION_SAPPHIRE_CORE_REGISTRATION_COMPLETE
			finalIntent.setClassName(this,"com.example.sapphireassistantframework.CoreService")
			// Does this cast it ok?
			var dataKeyArrayList = dataKey.toCollection(ArrayList<String>())
			finalIntent.putExtra(DATA_KEYS,dataKeyArrayList)
			// Hopefull this works fine
			finalIntent.fillIn(pendingIntentLedger,0)
			startService(finalIntent)
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

	fun loadAllTables(){
		// These should be global vars, so I really just need to load them.
		// I need to account for just directly loading these, not having them in a separate config
		registrationTable = loadJSONTable(REGISTRATION_TABLE)
		defaultModulesTable = loadJSONTable(DEFAULT_MODULES_TABLE)
		backgroundStartupTable = loadJSONTable(STARTUP_TABLE)
		routeTable = loadJSONTable(ROUTE_TABLE)
		aliasTable = loadJSONTable(ALIAS_TABLE)
		filenameTable = loadJSONTable(FILENAME_TABLE)
	}

	fun loadJSONTable(filename: String): JSONObject{
		if(File(filesDir,filename).exists() == false){
			return JSONObject()
		}
		var databaseFile = File(filesDir,filename)
		var jsonDatabase = JSONObject(databaseFile.readText())
		return jsonDatabase
	}

	fun registerModule(intent: Intent?){
		Log.i(CLASS_NAME,"Registering intent")
		if(newVersion()){
			registerRoute(intent!!)
			registerDefaults(intent!!)
			registerFilenames(intent!!)
			registerBackgroundService(intent!!)
			registerPendingIntent(intent!!)
			saveTables()
		}
	}

	// Save the PostOfficeService PendingIntent for CoreService
	fun registerPendingIntent(intent: Intent){
		try{
			var pendingIntent = intent.getParcelableExtra<PendingIntent>("PENDING")
			// The move to PendingIntent renders the MODULE_PACKAGE and MODULE_CLASS separation pointless
			var moduleInfo = "${intent.getStringExtra(MODULE_PACKAGE)};${intent.getStringExtra(MODULE_CLASS)}"
			pendingIntentLedger.putExtra(moduleInfo,pendingIntent)
		}catch(exception: Exception){
			Log.d(CLASS_NAME,"There was an error registering the PendingIntent")
			exception.printStackTrace()
		}
	}

	// This is temporary
	fun saveTables(){
		saveJSONTable(REGISTRATION_TABLE,registrationTable)
		saveJSONTable(DEFAULT_MODULES_TABLE,defaultModulesTable)
		saveJSONTable(STARTUP_TABLE,backgroundStartupTable)
		saveJSONTable(ROUTE_TABLE,routeTable)
		saveJSONTable(ALIAS_TABLE,aliasTable)
		// This is temporary, I think
		saveJSONTable(FILENAME_TABLE,filenameTable)
	}

	fun saveJSONTable(filename: String, jsonDatabase: JSONObject){
		var databaseFile = File(filesDir, filename)
		databaseFile.writeText(jsonDatabase.toString())
	}

	// This is specific to the current module
	fun newVersion(): Boolean{
		return true
	}

	// This is for keeping track of what module has what files. It acts as a CENTRAL REGISTRY *shudder*
	fun registerFilenames(intent: Intent){
		if(intent.hasExtra(DATA_KEYS)) {
			Log.i(CLASS_NAME,"This module has DATA_KEYS")
			var module = "${intent.getStringExtra(MODULE_PACKAGE)};${intent.getStringExtra(MODULE_CLASS)}"
			var filenames = JSONArray()
			var data_keys = intent.getStringArrayListExtra(DATA_KEYS)!!
			for(key in data_keys){
				filenames.put(key)
			}
			// This saves the filelist in the table
			filenameTable.put(module,filenames)
		}else{
			Log.i(CLASS_NAME,"This module has no files to share")
		}
	}

	// This is generic to the whole core
	fun registerDefaults(intent: Intent?){
		Log.v(this.javaClass.name,"Checking defaults...")
		var installPackageName = intent!!.getStringExtra(MODULE_PACKAGE)
		var installClassName = intent!!.getStringExtra(MODULE_CLASS)

		var changed = false
		for(key in DEFAULT_MODULES) {
			if(intent!!.hasExtra(MODULE_TYPE)) {
				var type = intent.getStringExtra(MODULE_TYPE)
				Log.v(this.javaClass.name,"testing default data for ${key}: ${installPackageName};${installClassName}...")
				if ((type == key) and (defaultModulesTable.optString(key).isNullOrBlank())
				) {
					Log.i(this.javaClass.name,"Match found for ${key}. Saving default data...")
					defaultModulesTable.put(key, "${installPackageName};${installClassName}")
					changed = true
				}
			}
		}

		if(changed == true){
			var defaultsFile = File(filesDir,DEFAULT_MODULES_TABLE)
			defaultsFile.writeText(defaultModulesTable.toString())
		}
	}

	// This is generic to the whole core
	fun registerBackgroundService(intent: Intent) {
		if (intent.hasExtra("BACKGROUND")) {
			var backgroundInfo = JSONObject(intent.getStringExtra("BACKGROUND"))
			Log.v(this.javaClass.name, "Registering background service...")
			// I suppose I am just outright copying the data here
			backgroundStartupTable.put(
				backgroundInfo.getString("registration_id"),
				backgroundInfo.toString()
			)
		}
	}

	// This is generic to the whole core
	fun registerRoute(intent: Intent){
		// This is telling it to call itself, due to ROUTE being used for background service
		var routeData = intent.getStringExtra(ROUTE)
		// This is being used for the ROUTE id, so it can be looked up.
		var routeName = intent.getStringExtra("ROUTE_NAME")
		Log.v(this.javaClass.name,"Registering ${routeName} as going to route ${routeData}")
		routeTable.put(routeName,routeData)
		var file = File(filesDir,ROUTE_TABLE)
		file.writeText(routeTable.toString())
	}
}