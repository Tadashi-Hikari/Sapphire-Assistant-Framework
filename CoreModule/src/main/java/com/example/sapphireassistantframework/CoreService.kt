package com.example.sapphireassistantframework

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.componentframework.SAFService
import org.json.JSONObject
import java.util.*

class CoreService: SAFService(){

    private var connections: LinkedList<Pair<String, Connection>> = LinkedList()
    private lateinit var notificationManager: NotificationManager
    private val CHANNEL_ID = "SAF"
    private val NAME = "Sapphire Assistant Framework"
    private val SERVICE_TEXT = "Sapphire Assistant Framework"
    private lateinit var sapphire_apps: LinkedList<Pair<String, String>>

    /*
    These could be static, to be shared between this and CoreRegistrationService
     */
    private val CONFIG = "core.conf"
    private val DATABASE = "core.db"

    // These are table names
    private val DEFAULT_MODULES_TABLE = "defaultmodules.tbl"
    private val BACKGROUND_TABLE = "background.tbl"
    private val ROUTE_TABLE = "routetable.tbl"
    private val ALIAS_TABLE = "alias.tbl"

    // I don't think I need to define these, cause they'll be defined on install. I just need to check if they're populated
    // These are the DEFAULT module package;class
    private val DEFAULT_CORE = "com.example.sapphireassistantframework;com.example.sapphireassistantframework.PostOffice"
    private val DEFAULT_PROCESSOR = "com.example.processormodule;com.example.processormodule.ProcessorService"
    private val DEFAULT_MULTIPROCESS = "com.example.multiprocessmodule;com.example.multiprocessmodule.MultiprocessService"

    // These are the config modules
    var jsonDefaultModules = JSONObject()
    var jsonBackgroundTable = JSONObject()
    var jsonRouteTable = JSONObject()
    var jsonAliasTable = JSONObject()

    var readyToGoSemaphore = false
    var SapphireModuleStack = LinkedList<Intent>()

    override fun onCreate() {
        // Do all startup tasks
        var configJSON = parseConfigFile(CONFIG)
        coreReadConfigJSON(configJSON)
        buildForegroundNotification()
        // This is where it is checking modules
        startRegistrationService()
        super.onCreate()
    }

    // Run through the registration process
    fun startRegistrationService(){
        var registrationIntent = Intent().setClassName(this,"${this}.CoreRegistrationService")
        startService(registrationIntent)
    }


    fun coreReadConfigJSON(jsonConfig: JSONObject){
        // These should be global vars, so I really just need to load them.
        // I need to account for just directly loading these, not having them in a separate config
        jsonDefaultModules = loadJSONTable(jsonConfig.getString(DEFAULT_MODULES_TABLE))
        jsonBackgroundTable = loadJSONTable(jsonConfig.getString(BACKGROUND_TABLE))
        jsonRouteTable = loadJSONTable(ROUTE_TABLE)
        jsonAliasTable = loadJSONTable(ALIAS_TABLE)
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

        // This is the notification for the foreground service. Maybe have it lead into other bound services
        var notification = NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.assistant)
                .setContentTitle("Sapphire Assistant Framework")
                .setContentText("SAF is running")
                .setOngoing(true)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .build()

        startForeground(1337, notification)
    }

    // This is where the actual mail sorting happens
    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        Log.i("CoreService","An intent was received")
        try {
            if (intent.action == ACTION_SAPPHIRE_CORE_BIND) {
                // Do the binding
            }else if(intent.action == ACTION_SAPPHIRE_MODULE_REGISTER) {
                /*
                When a registration intent is received, move it to a background process, so that it
                doesn't block up other intents that may be received, just in case this is done during
                normal useage, and not init
                 */
                intent.setClassName(this,"${this.packageName}.CoreRegistrationService")
                // just forward the prior intent right along
                startService(intent)
            // This will be triggered until the stack is empty, at which point it will allow the rest of the init
            }else if(intent.action == ACTION_SAPPHIRE_CORE_REGISTRATION_COMPLETE && readyToGoSemaphore == false) {
                    startBackgroundServicesConfigurable()
                    Log.i("CoreService", "All startup tasks finished")
                    // This could be the static initializing var
                    readyToGoSemaphore = true
                /*
                I need to wait for the initial install, because a env_variable is likely to be used
                in a route
                 */
            // This is not going to trigger when complete
            }else if(readyToGoSemaphore == false){
                // Don't do other stuff until it's initialized
                return super.onStartCommand(intent, flags, startId)
            }else if(intent.action == ACTION_SAPPHIRE_CORE_REQUEST_DATA){
                // I need to use the CoreService install process somehow, without duplicating code
                var queryIntent = Intent(ACTION_SAPPHIRE_MODULE_REQUEST_DATA)
                var modulesWithData = packageManager.queryIntentServices(queryIntent, 0)
                Log.i("PostOffice","Query results ${modulesWithData}")
                var multiprocessRoute = "("
                for(dataModule in modulesWithData.take(1)) {
                    try{
                        var packageName = dataModule.serviceInfo.packageName
                        var className = dataModule.serviceInfo.name
                        // Should I check if it's registered?
                        // This is making a multiprocess route
                        multiprocessRoute+="${packageName};${className},"
                    }catch(exception: Exception){
                        continue
                    }
                }
                // Janky, but should do. I just made a (multiprocess route)
                multiprocessRoute = multiprocessRoute.subSequence(0,multiprocessRoute.length-1) as String
                multiprocessRoute+=")"
                Log.i("PostOffice","Multiprocess route: ${multiprocessRoute}")
                // Is there a reason to switch intents and not just use the original?
                var multiprocessIntent = Intent(intent).setClassName(this,"com.example.multiprocessmodule.MultiprocessService")
                Log.i("PostOffice","Tacking ${intent.getStringExtra(ROUTE)!!} on to ROUTE")
                multiprocessIntent.putExtra(ROUTE,"${multiprocessRoute},${intent.getStringExtra(ROUTE)}")
                // -a means aggregate. I'm using a unix like flag for an example
                // This DEF needs to be changed
                multiprocessIntent.putExtra(POSTAGE,"-a")
                Log.i("PostOffice","Requesting data keys ${multiprocessIntent.getStringArrayListExtra(DATA_KEYS)}" )
                startService(multiprocessIntent)
            }else {
                sortMail(intent)
            }
        }catch(exception: Exception){
            Log.e("PostOffice","Some intent error")
        }
        return super.onStartCommand(intent, flags, startId)
    }

    // It really just pulls the alias from Route.
    // I need to have a way to ensure it triggers from a module
    fun startBackgroundServicesConfigurable(){
        for(recordName in jsonBackgroundTable.keys()){
            var startupRecord = jsonBackgroundTable.getJSONObject(recordName)
            var startupIntent = Intent()
            // 2nd route is actually alias. Do I to change something?
            startupIntent.putExtra(ROUTE, getRoute(startupRecord.getString(ROUTE)))

            // This is ripped right out of sortMail, so I could probably make it a function
            var routeData = getRoute(startupRecord.getString("route"))
            var moduleList: List<String> = parseRoute(routeData)
            var startingModule = moduleList.first().split(";")
            // This is ugly, but it'll do what I want
            startupIntent.setClassName(startingModule.get(0),startingModule.get(1))

            // I can add in
            if(startupRecord.has("bound") and startupRecord.getBoolean("bound")) {
                startBoundService(startupIntent)
            }else{
                startService(startupIntent)
            }
        }
    }

    fun stopBackgroundServices(sapphire_apps: List<Pair<String, String>>, connections: LinkedList<Pair<String, Connection>>) {
        for (connection in connections) {
            var service = connection.second
            unbindService(service)
        }
        for (info_pair: Pair<String, String> in sapphire_apps) {
            // Do I need to return a pair? Seems annoying.
            Log.i("CoreService", "launching ${info_pair.first}, ${info_pair.second}")
            var intent = Intent().setClassName(info_pair.first, info_pair.second)
            stopService(intent)
        }
    }

    // It's gonna work like this. Whatever is the LAST thing in the pipeline, core will read and upload pipeline data for.
    fun sortMail(intent: Intent){
        var routeRequest = ""

        // It reads from the ROUTE, assuming it is the requested info! This is the only module that works different
        if(intent.hasExtra(ROUTE)){
            routeRequest = intent.getStringExtra(ROUTE)!!
            // This is just to let me know what is going on
            Log.i("PostOffice","pipelineRequest: ${routeRequest}")
        }else{
            Log.i("PostOffice","Nothing was found, sending it the default way")
            // currently, the default is to do nothing
            return
        }

        var routeData = getRoute(routeRequest)
        var route = parseRoute(routeData)
        // It's going to be the first in the pipeline, right?
        var packageClass = route.first().split(";")

        // the packageName, and the className
        intent.setClassName(packageClass.component1(),packageClass.component2())
        intent.putExtra(MESSAGE,intent.getStringExtra(MESSAGE))
        intent.putExtra(ROUTE,routeData)

        startService(intent)
    }

    fun getRoute(key: String): String{
        // Load the route data
        var uncheckedRoute = jsonRouteTable.getString(key)
        var finalizedRoute = checkRouteForVariables(uncheckedRoute)
        return finalizedRoute
    }

    // I don't like how convoluted this is, but it works for now (brute force)
    fun checkRouteForVariables(uncheckedRoute: String):String{
        var linkedList = parseRoute(uncheckedRoute) as LinkedList<String>
        var tempLinkedList = LinkedList<String>()
        var finalizedModules= LinkedList<String>()
        var finalizedRoute = ""
        var newRoute = ""
        do{
            var currentModule = linkedList.pop()
            if(jsonDefaultModules.has(currentModule)){
                newRoute = jsonDefaultModules.getString(currentModule)
                tempLinkedList = parseRoute(newRoute) as LinkedList<String>
                tempLinkedList.addAll(linkedList)
                linkedList = tempLinkedList
            }else if(jsonAliasTable.has(currentModule)){
                newRoute = jsonAliasTable.getString(currentModule)
                tempLinkedList = parseRoute(newRoute) as LinkedList<String>
                tempLinkedList.addAll(linkedList)
                linkedList = tempLinkedList
            }else{
                finalizedModules.add(currentModule)
            }
        }while(linkedList.isNotEmpty())

        for(module in finalizedModules){
            if(finalizedRoute == ""){
                finalizedRoute = module
            }else{
                finalizedRoute+=",${module}"
            }
        }
        return finalizedRoute
    }

    fun startBoundService(boundIntent: Intent){
        Log.i("CoreService", "binding ${boundIntent.component}")
        // This should probably append flags and the like
        var connection = Connection()
        // This isn't even used, I just need it to remind me to change it later
        var coreService: Intent = Intent().setAction("mycroft.BIND")
        if (coreService.resolveActivity(packageManager) != null) {
            bindService(coreService, connection, Context.BIND_AUTO_CREATE)
        } else {
            Log.e("CoreDaemon", "PackageManager says the service doesn't exist")
        }
        // This appends it to the class variable, so I can shut it down later
        // I don't like how it's a pair
        connections.plus(Pair("${boundIntent.`package`};${boundIntent.component}",connection))
    }

    override fun onBind(intent: Intent?): IBinder? {
        TODO("Not yet implemented")
    }

    // The bound connection. The core attaches to each service as a client, tying them to cores lifecycle
    inner class Connection() : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            Log.i("CoreService", "Service connected")
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            Log.i("CoreService", "Service disconnected")
        }
    }


    override fun onDestroy() {
        stopBackgroundServices(sapphire_apps, connections)
        notificationManager.cancel(1337)
        super.onDestroy()
    }
}