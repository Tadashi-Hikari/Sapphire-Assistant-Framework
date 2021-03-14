package com.example.sapphireassistantframework

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.example.componentframework.SapphireCoreService
import org.json.JSONArray
import org.json.JSONObject
import java.lang.Exception
import java.net.Socket
import java.util.*

/*
This exists somewhere between the brain and body. This movement to me, is akain to the nervous system
and sending information from local spinal column/muscular reflexes, to more complex thinking tasks.
This would be indicitive of the nervous system as a whole
 */

class CoreService: SapphireCoreService(){
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
	var initialized = false
	private var connections: LinkedList<Pair<String, Connection>> = LinkedList()
	private lateinit var notificationManager: NotificationManager
	private val CHANNEL_ID = "SAF"
	private val NAME = "Sapphire Assistant Framework"
	private val SERVICE_TEXT = "Sapphire Assistant Framework"

	val CONFIG = "core.conf"
	val CONFIG_VAL_DATA_TABLES = "DATA_TABLES"
	val DEFAULT_MODULES_TABLE = "defaultmodules.tbl"
	val BACKGROUND_TABLE = "background.tbl"
	val ROUTE_TABLE = "routetable.tbl"
	val ALIAS_TABLE = "alias.tbl"
	lateinit var tables: JSONObject

	// I already don't like how this is built
	fun loadConfig(){
		// I don't like the name getTextData, It's ambiguous
		var dataTableInfo = getTextData(CONFIG_VAL_DATA_TABLES)
		tables = loadTables(dataTableInfo)
	}

	var moduleJsonDataTables = JSONObject()

	fun loadTables(tables: String): JSONObject{
		var jsonConfigData = JSONObject(tables)

		// Hmm..... I suppose they can be null keys
		for(tablename in jsonConfigData.keys()){
			var table = getTextData(tablename)
			moduleJsonDataTables.put(tablename,table)
		}
		return moduleJsonDataTables
	}

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
			intent?.hasExtra(FROM)!! -> return true
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
			true ->	when(intent.action) {
				ACTION_SAPPHIRE_CORE_BIND -> onBind(intent)
				ACTION_SAPPHIRE_CORE_REGISTRATION_COMPLETE -> initialize()
				ACTION_SAPPHIRE_CORE_REQUEST_DATA -> handleRoute(intent)
				ACTION_SAPPHIRE_DATA_TRANSFER -> dataTransfer(intent)
				ACTION_SAPPHIRE_SOCKET_READY -> startTransfer(intent)
				else -> handleRoute(intent)
			}
			false -> when(intent.action){
				ACTION_SAPPHIRE_INITIALIZE -> startRegistrationService()
				ACTION_SAPPHIRE_MODULE_REGISTER -> forwardRegistration(intent)
			}
		}
	}

	fun handleRoute(signal: Intent){
		when(signal.hasExtra(ROUTE)){
			true -> nextModule(signal)
			false -> handleNewInput(signal)
		}
	}

	var ACTION_SAPPHIRE_DATA_TRANSFER = "ACTION_SAPPHIRE_DATA_TRANSFER"
	lateinit var socket: Socket
	fun dataTransfer(intent: Intent){
		var socket = Socket(localSocket.localAddress,9001)
		var startTransferIntent = Intent()
		startTransferIntent.setClassName(this.packageName,"com.example.calendarskill.CalendarModuleInstallService")
		startService(startTransferIntent)
	}

	var ACTION_SAPPHIRE_SOCKET_READY = "ACTION_SAPPHIRE_SOCKET_READY"
	fun startTransfer(intent: Intent){

	}

	// Can this be wrapped in to nextModule or handleNewInput
	fun forwardRegistration(intent: Intent){
		when(intent.getStringExtra(FROM)){
			"${this.packageName};${this.packageName}.CoreRegistrationService" -> {
				intent.setClassName(intent.getStringExtra(MODULE_PACKAGE)!!,intent.getStringExtra(MODULE_CLASS)!!)
				startService(intent)
			}
			else -> {
				intent.setClassName("${this.packageName}","${this.packageName}.CoreRegistrationService")
				startService(intent)
			}
		}
	}

	fun nextModule(intent: Intent){
		// This gets the next module in line
		var route = JSONArray(intent.getStringExtra(ROUTE))
		var module = route.getString(0).split(";")
		// pop the module from the route
		route.remove(0)
		intent.putExtra(ROUTE,route.toString())

		intent.setClassName(module.component1(),module.component2())
		dispatchSapphireServiceFromCore(intent)
	}

	fun handleNewInput(intent: Intent){
		// Does this load from the proper place (such as a local file)?
		var routeTable = moduleJsonDataTables.getJSONObject(ROUTE_TABLE)
		when{
			// This is long and ugly. Can I fix it?
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
		loadConfig()
		startBackgroundServices()
		initialized = true
	}

	fun startBackgroundServices(){
		var jsonBackgroundTable = tables.getJSONObject(BACKGROUND_TABLE)
		for(recordName in jsonBackgroundTable.keys()){
			var record = jsonBackgroundTable.getJSONObject(recordName)

			var startupIntent = Intent()
			startupIntent.putExtra(ROUTE,record.getString(STARTUP_ROUTE))
			startupIntent.putExtra(POSTAGE,tables.getString(DEFAULT_MODULES_TABLE))
			var module = startupIntent.getStringExtra(ROUTE)!!.split(";").get(0).split(",")
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