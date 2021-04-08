package com.example.sapphireassistantframework

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.*
import android.os.Build
import android.os.IBinder
import android.widget.Toast
import androidx.core.app.NotificationCompat
import androidx.core.content.FileProvider
import com.example.componentframework.SapphireCoreService
import org.json.JSONObject
import java.io.File
import java.lang.Exception
import java.util.*

class CoreService: SapphireCoreService(){
	val LOCAL_VERSION = "0.2.0"

	// The bound connection. The core attaches to each service as a client, tying them to cores lifecycle
	inner class Connection() : ServiceConnection {
		override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
			Log.i(this.javaClass.name, "Service connected")
			if(service != null){
				// This will be moved to a permission style check, to increase user control and prevent rouge background services
				Toast.makeText(applicationContext,"This service: ${name?.shortClassName} didn't return a null binder, is that ok?",Toast.LENGTH_LONG)
			}
		}

		override fun onServiceDisconnected(name: ComponentName?) {
			Log.i(this.javaClass.name, "Service disconnected")
		}
	}

	//State variables
	//var initialized = false
	var initialized = true
	// This should probably be looked at more
	private var connections: LinkedList<Pair<String, Connection>> = LinkedList()
	// and this. Though this is kind of a 'fake' connection
	var connection = Connection()
	private lateinit var notificationManager: NotificationManager
	private val CHANNEL_ID = "SAF"
	private val NAME = "Sapphire Assistant Framework"
	private val SERVICE_TEXT = "Sapphire Assistant Framework"

	val CONFIG = "core.conf"
	val CONFIG_VAL_DATA_TABLES = "datatables.tbl"
	val DEFAULT_MODULES_TABLE = "defaultmodules.tbl"
	val BACKGROUND_TABLE = "background.tbl"
	val ROUTE_TABLE = "routetable.tbl"
	val ALIAS_TABLE = "alias.tbl"

	override fun onCreate() {
		super.onCreate()
		buildForegroundNotification()
	}

	override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
		if(validateIntent(intent)){
			sortMail(intent!!)
		}else{
			Log.i(this.javaClass.simpleName,"There was an issue with an incoming intent. Did it say where it was FROM?")
		}
		// This may need to be moved, if I am to do things in the background
		return super.onStartCommand(intent, flags, startId)
	}
	// Check if it exists, and has the minimum information needed to go further
	fun validateIntent(intent: Intent?): Boolean{
		when{
			intent?.action == ACTION_SAPPHIRE_INITIALIZE -> return true
			intent?.action == ACTION_SAPPHIRE_MODULE_REGISTER -> return true
			intent?.action == ACTION_SAPPHIRE_CORE_REGISTRATION_COMPLETE -> return true
			intent?.action == "ACTION_SAPPHIRE_REQUEST_FILE" -> return true
			// I think this might now work easy for requesting modules vs installing modules
			intent?.action == ACTION_MANIPULATE_FILE_DATA -> return true
			intent?.hasExtra(FROM) == true -> return true
			intent?.hasExtra(ROUTE) == true -> return true
			else -> return false
		}
	}

	fun buildForegroundNotification() {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
			val importance = NotificationManager.IMPORTANCE_HIGH
			val channel = NotificationChannel(CHANNEL_ID, NAME, importance).apply {
				description = SERVICE_TEXT
			}

			notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
			notificationManager.createNotificationChannel(channel)
		}

		var notification = NotificationCompat.Builder(this, CHANNEL_ID)
			.setSmallIcon(R.drawable.assistant)
			.setContentTitle("Sapphire Assistant")
			.setContentText("Thank you for trying out the Sapphire Framework")
			.setOngoing(true)
			.setPriority(NotificationCompat.PRIORITY_HIGH)
			.build()

		startForeground(1337, notification)
	}

	// What is the nervous systems function called
	fun sortMail(intent: Intent){
		// Looking for a better mental abstraction. These actions are more akin to heartbeats, digestion, etc. Autonomous actions, but unchangeable
		// Handle actions here
		when(initialized){
			true ->	when(intent.action){
				ACTION_SAPPHIRE_CORE_BIND -> onBind(intent)
				ACTION_REQUEST_FILE_DATA -> handleRoute(intent)
				ACTION_MANIPULATE_FILE_DATA -> serveFile(intent)
				// Generic action
				else -> handleRoute(intent)
			}
			false -> when(intent.action){
				ACTION_SAPPHIRE_INITIALIZE -> startRegistrationService()
				ACTION_SAPPHIRE_CORE_REGISTRATION_COMPLETE -> initialize()
				ACTION_SAPPHIRE_MODULE_REGISTER -> forwardRegistration(intent)
			}
		}
	}

	// This is meant to bridge, or serve internal. This is not at all
	fun serveFileProper(intent: Intent) {
		TODO("This whole thing needs to be implemented")
		// I'm just going to assume a chatty protocol
		// get file list from the module, in DATA_KEYS? <- yes
		when (intent.extras != null) {
			// a flag for acting as a bridge
			intent.hasExtra("FILE_PROVIDER") -> requestBridge(intent)
			// a flag for transferring, based on the config. Needs renaming, but the base logic is there
			intent.hasExtra("TRANSFER") -> serveFile(intent)
			// Send out the Uris if they exist
			else -> checkForLocal(intent)
		}
	}

	fun checkForLocal(intent: Intent){
		// Not sure I need this, but why not
		var outgoingIntent = Intent(intent)
		if(outgoingIntent.clipData == null){
		}
		// Get them filenames...
		var filenames = intent.getStringArrayListExtra(DATA_KEYS)!!
		for(filename in filenames){
			var file = File(filesDir,filename)
			var uri = FileProvider.getUriForFile(this.applicationContext,"com.example.sapphireassistantframework.fileprovider",file)
			// This is an easy enough shortcut. Just put the *last* one in the data slot. The order *really* matters here though, pay attention!
			when(file.exists()){
				// There was some kind of error
				filenames.size <= 0 -> Log.d(CLASS_NAME,"There was a checkForLocal error")
				// if there is only one or it's the last one, make it the uri
				filenames.size == 1 -> outgoingIntent.setData(uri)
				// No clipData exists, and it will need one
				outgoingIntent.clipData == null -> outgoingIntent.clipData = ClipData.newRawUri("FILEDATA",uri)
				// Clip data exists, and there is more to add on!
				filenames.size > 1 -> outgoingIntent.clipData!!.addItem(ClipData.Item(uri))
				// This should be shorthand for requesting transfer OR bridge. doesn't need to be serveFile
				false -> serveFile(intent)
			}
			// Throw it out. Each loop the count should update though, so it should still be good
			filenames.removeFirst()
		}
		// give permission to access these URIS. The should be readable, not editable
		outgoingIntent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
		startSapphireService(connection,outgoingIntent)
	}

	fun requestTransfer(intent: Intent){
		TODO("This request to make a copy of a file in the core FileProvider")
	}

	fun requestBridge(intent: Intent){
		// Only reason I am not doing a socket, is because of network permissions
		TODO("This will create a temp file, to pass the uri along. the temp file acts as a pipe")
	}

	fun serveFile(intent: Intent){
		try{
			var manipulateIntent = Intent()
			//intent.getStringExtra(FROM)
			manipulateIntent.setClassName("com.example.calendarskill","com.example.calendarskill.CalendarModuleInstallServiceRefined")
			manipulateIntent.setAction(ACTION_MANIPULATE_FILE_DATA)
			var counter = 0

			for(key in intent.getStringArrayListExtra(DATA_KEYS)!!){
				Log.v(this.javaClass.name,"Generating Core file for ${key} from ${intent.getStringExtra(FROM)}")
				var file = File(filesDir,key)
				// I don't know if this is needed...
				file.createNewFile()
				var uri = FileProvider.getUriForFile(this.applicationContext,"com.example.sapphireassistantframework.fileprovider",file)
				// Add the filedata to the intent
				if(counter == 0) {
					manipulateIntent.setDataAndType(uri, contentResolver.getType(uri))
					manipulateIntent.setFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
				}else {
					var item = ClipData.Item(uri)
					when(manipulateIntent.clipData){
						null -> manipulateIntent.clipData = ClipData.newRawUri(DATA_KEYS,uri)
						else -> manipulateIntent.clipData!!.addItem(item)
					}
				}
				counter++
			}

			// I have to figre out what to do with this intent
			startSapphireService(connection,manipulateIntent)
		}catch(exception: Exception){
			Log.d(this.javaClass.name,"There was an error generating the coreFiles and sending the URIs")
		}
	}

	// Is this when redundant w/ validate?
	fun handleRoute(intent: Intent){
		when(intent.hasExtra(ROUTE)){
			true -> nextModule(intent)
			false -> handleNewInput(intent)
		}
	}

	// Can this be wrapped in to nextModule or handleNewInput
	fun forwardRegistration(intent: Intent){
		// I don't think the incoming intent can propagate
		var outgoingIntent = Intent(intent)
		when(outgoingIntent.getStringExtra(FROM)){
			"${this.packageName};${this.packageName}.CoreRegistrationService" -> {
				outgoingIntent.setAction(ACTION_SAPPHIRE_MODULE_REGISTER)
				outgoingIntent.setClassName(intent.getStringExtra(MODULE_PACKAGE)!!,intent.getStringExtra(MODULE_CLASS)!!)
				startService(outgoingIntent)
			}
			else -> {
				outgoingIntent.setClassName("${this.packageName}","${this.packageName}.CoreRegistrationService")
				startService(outgoingIntent)
			}
		}
	}

	fun nextModule(intent: Intent){
		// This gets the next module in line
		var route = intent.getStringExtra(ROUTE)!!
		var cleanedRoute = expandRoute(route).split(",")
		Log.d("MODULE", intent.getStringExtra(ROUTE)!!)
		var module = cleanedRoute.get(0).split(";")
		// pop the module from the route

		intent.putExtra(ROUTE,route.toString())

		intent.setClassName(module.component1(),module.component2())
		dispatchSapphireServiceFromCore(intent)
	}

	fun handleNewInput(intent: Intent){
		// Does this load from the proper place (such as a local file)?
		var routeTable = loadTable(ROUTE_TABLE)
		when{
			routeTable.has(intent.getStringExtra(FROM)) -> {
				intent.putExtra(ROUTE,routeTable.getString(intent.getStringExtra(FROM)))
				nextModule(intent)
			}
			else -> defaultPath(intent)
		}
	}

	fun defaultPath(intent: Intent){
		Log.e(this.javaClass.name, "There is no default path")
	}

	/*
	 a state between non-consiousness and consciousness. I think this will change when I introduce
	 sleep-based data processing (when charging)
	 */
	fun initialize(){
		startBackgroundServices()
		initialized = true
	}

	fun startBackgroundServices(){
		var jsonBackgroundTable = loadTable(BACKGROUND_TABLE)
		Log.i(this.javaClass.name, jsonBackgroundTable.toString())
		for(recordName in jsonBackgroundTable.keys()){
			var record = JSONObject(jsonBackgroundTable.getString(recordName))

			var startupIntent = Intent()
			startupIntent.putExtra(ROUTE,record.getString(STARTUP_ROUTE))
			startupIntent.putExtra(POSTAGE,loadTable(DEFAULT_MODULES_TABLE).toString())
			Log.v(this.javaClass.name,startupIntent.getStringExtra(ROUTE)!!)
			var module = startupIntent.getStringExtra(ROUTE)!!.split(",").get(0).split(";")
			startupIntent.setClassName(module.component1(),module.component2())
			when(record.getBoolean("bound")){
				true -> startBoundService(startupIntent)
				else -> startService(startupIntent)
			}
		}
	}

	fun startBoundService(boundIntent: Intent){
		var connection = Connection()

		if (packageManager.resolveService(boundIntent,0) != null) {
			bindService(boundIntent, connection, Context.BIND_AUTO_CREATE)
		} else {
			Log.e(this.javaClass.name, "PackageManager says the service doesn't exist")
		}
		// This appends it to the class variable, so I can shut it down later
		// I don't like how it's a pair
		connections.plus(Pair("${boundIntent.`package`};${boundIntent.component}",connection))
	}

	// Run through the registration process
	fun startRegistrationService(){
		var registrationIntent = Intent().setClassName(this.packageName,"${this.packageName}.CoreRegistrationService")
		registrationIntent.setAction(ACTION_SAPPHIRE_INITIALIZE)
		Log.v(this.javaClass.name,"starting service ${"${this.packageName}.CoreRegistrationService"}")
		startService(registrationIntent)
	}

	override fun onDestroy() {
		notificationManager.cancel(1337)
		super.onDestroy()
	}
}