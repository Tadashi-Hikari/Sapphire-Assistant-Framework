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
import java.io.File
import java.util.*

class CoreService: SAFService(){
    private var connections: LinkedList<Pair<String, Connection>> = LinkedList()
    private lateinit var notificationManager: NotificationManager
    private val CHANNEL_ID = "SAF"
    private val NAME = "Sapphire Assistant Framework"
    private val SERVICE_TEXT = "Sapphire Assistant Framework"
    private lateinit var sapphire_apps: LinkedList<Pair<String, String>>
    // This isn't good, because I've now defined it in CoreService and PostOffice
    private val CONFIG = "core.conf"
    private val DATABASE = "core.db"

    // These are table names
    private val DEFAULT_MODULES_TABLE = "defaultmodules.tbl"
    private val BOUND_STARTUP_TABLE = "boundstart.tbl"
    private val BACKGROUND_STARTUP_TABLE = "background.tbl"
    private val FOREGROUND_STARTUP_TABLE = "foreground.tbl"
    private val HOOK_TABLE = "hooks.tbl"

    // I don't think I need to define these, cause they'll be defined on install. I just need to check if they're populated
    // These are the DEFAULT module package;class
    private val DEFAULT_CORE = "com.example.sapphireassistantframework;com.example.sapphireassistantframework.PostOffice"
    private val DEFAULT_PROCESSOR = "com.example.processormodule;com.example.processormodule.ProcessorService"
    private val DEFAULT_MULTIPROCESS = "com.example.multiprocessmodule;com.example.multiprocessmodule.MultiprocessService"

    // These are the config modules
    var jsonDefaultModules = JSONObject()
    var jsonBoundStartup = JSONObject()
    var jsonBackgroundStartup = JSONObject()
    var jsonForegroundStartup = JSONObject()
    var jsonHookList = JSONObject()

    override fun onCreate() {
        // Do all startup tasks
        var configJSON = parseConfigFile(CONFIG)
        coreReadConfigJSON(configJSON)
        buildForegroundNotification()
        scanInstalledModules()
        startBackgroundServices()
        // startBackgroundService()
        Log.i("CoreService", "All startup tasks finished")
        super.onCreate()
    }

    // This is where the actual mail sorting happens
    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        Log.i("CoreService","An intent was received")
        try {
            if (intent.action == ACTION_SAPPHIRE_CORE_BIND) {
                // Do the binding
            }else if(intent.action == ACTION_SAPPHIRE_MODULE_REGISTER) {
                Log.i("PostOffice", "Module registration action received")
                // This needs to be made generic
                if (intent.hasExtra(DATA_KEYS)) {
                    Log.i("PostOffice", "Registration intent contains data keys")
                    var processorIntent = Intent(intent)
                    // This should load from something configurable, and a pipeline <- this
                    processorIntent.setClassName(
                        this,
                        "package com.example.processormodule.ProcessorTrainService"
                    )
                    startService(processorIntent)
                }
                /**
                 * If it is a data request, it sends the message to all modules w/ data, and if the module
                 * has the data, then it responds. Otherwise it ignores the request. I'd make it a
                 * broadcast, but delivery needs to be ensured. I will need multiprocessor to wait until
                 * all modules have responded before continuing along the pipeline
                 */
            }else if(intent.action == ACTION_SAPPHIRE_CORE_REQUEST_DATA){
                // I need to use the CoreService install process somehow, without duplicating code
                var queryIntent = Intent(ACTION_SAPPHIRE_MODULE_REQUEST_DATA)
                var modulesWithData = packageManager.queryIntentServices(queryIntent, 0)
                Log.i("PostOffice","Query results ${modulesWithData}")

                // This sends intents to all modules w/ data. The modules themselves
                // decide if they need to respond
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

    fun coreReadConfigJSON(jsonConfig: JSONObject){
        // These should be global vars, so I really just need to load them.
        // I need to account for just directly loading these, not having them in a separate config
        jsonDefaultModules = loadJSONTable(jsonConfig.getString(DEFAULT_MODULES_TABLE))
        jsonBoundStartup = loadJSONTable(jsonConfig.getString(BOUND_STARTUP_TABLE))
        jsonBackgroundStartup = loadJSONTable(jsonConfig.getString(BACKGROUND_STARTUP_TABLE))
        jsonForegroundStartup = loadJSONTable(jsonConfig.getString(FOREGROUND_STARTUP_TABLE))
        jsonHookList = loadJSONTable(jsonConfig.getString(HOOK_TABLE))
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

    // There could be an issue here, since it isn't waiting for a callback. I may need to run this through the multiprocess module
    fun scanInstalledModules() {
        var intent = Intent().setAction(ACTION_SAPPHIRE_MODULE_REGISTER)
        var installedSapphireModules = packageManager.queryIntentServices(intent, 0)

        // I believe installedSapphireModules is a collection of collections (List & MutableList). I just
        // need one, otherwise it gives me a duplicate thing
        for (module in installedSapphireModules.take(1)) {
            try {
                var packageName = module.serviceInfo.packageName
                var className = module.serviceInfo.name
                // This is called twice. Why?
                Log.i("CoreService", "Found a module. Checking if it's registered: ${packageName};${className}")
                // If its registered, check version. Else, register it
                if (checkModuleRegistration(packageName, className)) {

                } else {
                    installRegisterModule(packageName, className)
                }
            } catch (exception: Exception) {
                continue
            }
        }
    }

    // This will likely be called in a different section of the CoreService, upon receipt of an intent
    // How do I transfer packageName and className since they're not part of the intent...?
    fun updateModuleRegistration(intent: Intent) {
        // This should be global to CoreService
        var registrationTableFilename = "registration.tbl"
        // This should be global to CoreService
        // Is it a table or database?
        var jsonRegistration = loadJSONTable(registrationTableFilename)
        // This package name isn't right. It needs to come from PackageManager
        var jsonModuleRegistration = JSONObject()
        if (checkModuleRegistration(intent.getStringExtra(MODULE_PACKAGE)!!,"fake_class_placeholder")) {
            jsonModuleRegistration = jsonRegistration.getJSONObject(intent.getStringExtra(MODULE_PACKAGE)!!)
            if (intent.hasExtra(MODULE_VERSION)) {
                if (jsonModuleRegistration.getString(MODULE_VERSION) != intent.getStringExtra(MODULE_VERSION)) {
                    // Do some kind of update
                    // This mostly applys to data, as far as I can tell
                }
            }
        } else {
            // I am using DATA_KEYS here, can I make it more generic?
            // for(key in intent.getStringArrayListExtra(DATA_KEYS)!!)
            if (intent.hasExtra(MODULE_TYPE)) {
                // What happens if it has multiple module_types?
                // Maybe I should register these in a moduleTypeTable instead of in their own table
                jsonModuleRegistration.put(MODULE_TYPE, intent.getStringExtra(MODULE_TYPE))
                checkDefaultModules(intent.getStringExtra(MODULE_TYPE)!!, intent.getStringExtra(MODULE_PACKAGE)!!)
            } else if (intent.hasExtra(MODULE_VERSION)) {
                jsonModuleRegistration.put(MODULE_VERSION, intent.getStringExtra(MODULE_VERSION))
            }
        }
        jsonRegistration.put(packageName, jsonModuleRegistration.toString())
    }

    // This takes an incoming INSTALL intent, and registers it in the JSONtable
    fun registerModuleType(intent: Intent) {
        var packageName = intent.getStringExtra(MODULE_PACKAGE)
        if (intent.hasExtra(MODULE_TYPE)) {
            var moduleTypeData = intent.getStringExtra(MODULE_TYPE)!!
            var moduleTypes = moduleTypeData.split(',')
            for (moduleType in moduleTypes) {
                //register moduleType
                if(jsonDefaultModules.has(moduleType) == false) {
                    // I need a way to get the sending package info from the intent
                    // Wow, I already don't linke this storage method
                    jsonDefaultModules.put(moduleType,"${intent.getStringExtra(MODULE_PACKAGE)};${intent.getStringExtra("fake_class_placeholder")}")
                }
            }
        } else {
            //register GENERIC
        }
        //writeFile
    }

    // Should this be packageName,className?
    fun checkModuleRegistration(packageName: String, className: String): Boolean {
        // This should be global to CoreService
        var registrationTable = "registration.tbl"
        // This should be global to CoreService
        // Is it a table or database?
        var jsonRegistration = loadJSONTable(registrationTable)
        if (jsonRegistration.has(packageName)) {
            //checkVersionInfo
            return true
        } else {
            return false
        }
    }

    // This is pretty straightforward.
    // I think that this needs to be done other than onCreate.
    fun installRegisterModule(packageClass: String, className: String) {
        var databaseFile = File(filesDir, DATABASE)
        var database = JSONObject()

        if (database.has(packageClass)) {
            return
        } else {
            var module = JSONObject()
            module.put("packageClass", packageClass)
            databaseFile.writeText(module.toString())
        }
    }

    fun checkDefaultModules(moduleType: String, packageName: String) {
        var defaultsTableFilename = "defaultModules.tbl"
        var jsonDefaultModules = loadJSONTable(defaultsTableFilename)
        if (jsonDefaultModules.has(packageName) == false) {
            jsonDefaultModules.put(moduleType, packageName)
        }
        saveJSONTable(defaultsTableFilename, jsonDefaultModules)
    }

    fun startBackgroundServices(){
        var speechToText = Pair(
        "com.example.sapphireassistantframework",
        //"com.example.sapphireassistantframework.CoreKaldiService")
        "com.example.vosksttmodule.KaldiService")
        var startupApps = listOf(speechToText)

        /** Convoluted, but meant to give a human readable name to running processes.
         * I started VoskSTT as a bound service, so that it will not die as long as a service
         * (CoreService) is bound to it. This gives SAF the control over background processes to
         * help control battery life. Since CoreService is a foreground service it also prevents
         * me from spamming the user with notifications, or having the system kill the background
         * service
         */
        for (classNamePair: Pair<String, String> in startupApps) {
            connections.plus(
                Pair(
                    classNamePair.first,
                    startBoundService(
                        classNamePair.first,
                        classNamePair.second)
                )
            )
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
        var routes = loadRoutes()
        var routeRequest = ""

        //notifyHooks()
        var configJSON = loadConfig(CONFIG)
        //checkConditionals()

        // Postage is the minimum thing needed to send a message and/or runtime config. Here, it is the route name
        if(intent.hasExtra(POSTAGE)){
            routeRequest = intent.getStringExtra(POSTAGE)!!
            Log.i("PostOffice","pipelineRequest: ${routeRequest}")
        }else{
            Log.i("PostOffice","Nothing was found, sending it the default way")
            // currently, the default is to return
            return
        }

        var routeData = routes.get(routeRequest)!!
        var route = parseRoute(routeData)
        // It's going to be the first in the pipeline, right?
        intent.setClassName(this,route.first())
        intent.putExtra(MESSAGE,intent.getStringExtra(MESSAGE))
        intent.putExtra(ROUTE,routeData)

        startService(intent)
    }

    // These are services that CoreService is creating, and binding to. They're other modules
    fun startBoundService(pkg: String, classname: String): Connection {
        Log.i("CoreService", "binding ${pkg}, ${classname}")
        // This needs to be changed to not pseudocode
        var connection = Connection()
        // This will likely need to change over time
        // Package name is right. Is the class name?
        var coreService: Intent = Intent().setAction("mycroft.BIND")
        // There is an issue with the module name and class name. I will need to fix this
        coreService.setClassName(pkg, classname)
        // Preeeety sure this is archaic
        coreService.putExtra("CORE_PACKAGE", "www.mabase.tech.mycroft")
        coreService.putExtra("CORE_CLASSNAME", "www.mabase.tech.mycroft.CoreDaemon")
        if (coreService.resolveActivity(packageManager) != null) {
            bindService(coreService, connection, Context.BIND_AUTO_CREATE)
        } else {
            Log.e("CoreDaemon", "PackageManager says the service doesn't exist")
        }

        return connection
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

    fun checkHookLog(intent: Intent): Boolean {
        var databaseFile = File(filesDir, DATABASE)
        var databaseString = databaseFile.readText()
        var database = JSONObject(databaseString)
        var hooklog = database.getJSONObject(HOOK_TABLE)
        // What is the point of this?
        if (intent.getStringExtra(HOOK_TABLE) != null) {
            print("YAAAAAY")
            return true
        }
        return false
    }

    /**
     * What I want to happen here, is basically update a TextView in the CoreActivity.
     * Do I need to make it *not* hardcoded?
     *
     * It is looking a lot like an outgoing Multiprocess
     */
    fun notifyHooks(intent: Intent) {
        // I am thinking that checkHookLog is basically a shadowRoute
        if (checkHookLog(intent)) {
            var hookIntent = Intent().setClassName(this, "hook.intent.service")
            hookIntent.setAction("SECOND ROUTE")
        }
    }

    fun loadRoutesFile(routesFilename: String): JSONObject{
        var routesFile = File(filesDir,routesFilename)
        var routesJSON = JSONObject(routesFile.readText())
        return routesJSON
    }

    fun loadRoutes(): Map<String,String>{
        var routes = mutableMapOf<String,String>()
        // kaldiservice, in this example, is FROM not STDIN
        routes.put("com.example.vosksttmodule.KaldiService",
            "com.example.processormodule.ProcessorCentralService")
        //calendar, in this example, is STDIN, not FROM
        //routes.put("calendar","com.example.calendarskill.Calendar")
        // I just want everything to default to here for now
        routes.put("calendar","com.example.termuxmodule.TermuxService")

        return routes
    }

    // I think this is supposed to be a loadRoutes
    fun routeTest(){
        var database = JSONObject()

        var route = database.getJSONArray("modulename.filename")
        for(index in 0 until route.length()){
            var moduleData = route.getJSONObject(index)
            parseModuleData(moduleData)
        }
    }

    // What was I making this for? Oh! It's for replacing parseRoute so I can include flags
    fun parseModuleData(json: JSONObject){
        // I don't see any reason why it shouldn't just be these, predefined
        json.get("PACKAGE")
        json.get("CLASSNAME")
        // Should this be sub-parsed?
        var flags = json.getJSONObject("FLAGS")
        for(index in 0 until flags.length()){
            // add flags, as extras?
        }
    }


    override fun onDestroy() {
        stopBackgroundServices(sapphire_apps, connections)
        notificationManager.cancel(1337)
        super.onDestroy()
    }
}