package com.example.sapphireassistantframework

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.net.Uri
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import androidx.core.content.FileProvider
import androidx.core.net.toUri
import com.example.componentframework.SapphireCoreService
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.lang.Exception
import java.net.Socket
import java.util.*

class CoreService: SapphireCoreService(){
	val LOCAL_VERSION = "0.2.0"

	// The bound connection. The core attaches to each service as a client, tying them to cores lifecycle
	inner class Connection() : ServiceConnection {
		override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
			Log.i(this.javaClass.name, "Service connected")
		}

		override fun onServiceDisconnected(name: ComponentName?) {
			Log.i(this.javaClass.name, "Service disconnected")
		}
	}

	//State variables
	//var initialized = false
	var initialized = true
	private var connections: LinkedList<Pair<String, Connection>> = LinkedList()
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

	override fun onBind(intent: Intent?): IBinder? {
		TODO("Not yet implemented")
	}

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
			intent?.hasExtra(FROM)!! -> return true
			intent?.hasExtra(ROUTE)!! -> return true
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
				// This is being replaced w/ REQUEST_FILE
				//ACTION_SAPPHIRE_CORE_REQUEST_DATA -> handleRoute(intent)
				"ACTION_SAPPHIRE_REQUEST_FILE" -> serveFile(intent)
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

	fun serveFile(intent: Intent){
		Log.v(this.javaClass.name,"The file does not yet exist. Requesting...")
		// This would be happening in the background, and I need to wait until it's finished. Multiprocess Module can handle this for me...
		var fileRequestIntent = Intent()
		fileRequestIntent.setClassName(this,"com.example.calendarskill.CalendarModuleInstallServiceRefined")
		fileRequestIntent.setAction("ACTION_SAPPHIRE_REQUEST_FILE")

		//This is added for demo purposes
		var testFile = File(filesDir,"testfile")
		testFile.createNewFile()
		//var uri = FileProvider.getUriForFile(this,"com.example.sapphireassistantframework.fileprovider",testFile)
		fileRequestIntent.data = testFile.toUri()
		fileRequestIntent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
		fileRequestIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)

		var CALENDAR = "(${this.packageName};com..CalendarModuleInstallServiceRefined)"
		// This is a placeholder, but this is what it would look like. Maybe ( ) should auto affix MULTIPROCESS
		fileRequestIntent.putExtra(ROUTE,"${MULTIPROCESS},${CALENDAR},${MULTIPROCESS},${PROCESSOR}")
		startService(fileRequestIntent)
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