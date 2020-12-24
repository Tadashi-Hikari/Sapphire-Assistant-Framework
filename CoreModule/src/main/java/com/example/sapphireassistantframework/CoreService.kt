package com.example.sapphireassistantframework

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.content.pm.PackageManager
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.componentframework.SAFService
import java.lang.Exception
import java.util.*
import org.json.JSONObject

class CoreService: SAFService(){
    private var connections: LinkedList<Pair<String, Connection>> = LinkedList()
    private lateinit var notificationManager: NotificationManager
    private val CHANNEL_ID = "SAF"
    private val NAME = "Sapphire Assistant Framework"
    private val SERVICE_TEXT = "Sapphire Assistant Framework"
    private lateinit var sapphire_apps: LinkedList<Pair<String, String>>
    private var pipeline: LinkedList<String> = LinkedList<String>()

    override fun onBind(intent: Intent?): IBinder? {
        TODO("Not yet implemented")
    }

    override fun onCreate() {
        super.onCreate()

        /** I want all of these things to run when the service is created, not every time it
         * it receives a message. Though, perhaps It should run when the foreground service
         * is launched.
         */
        buildForegroundNotification()
        scanInstalledApps()
        mockStartBackgroundServices()
        Log.i("CoreService", "Everything should be starting up now")
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        if (intent.action == "sapphire_assistant_framework.BIND") {
            //This should be an onFirstRun
        } else {
            //sortPost(intent)
            var sendingModule = intent.getStringExtra(FROM)
            // I needed a dynamic way to handle data, based on the apps data needs and purpose.
            if(sendingModule == null) {
                Log.e(
                    "CoreServicePostOffice",
                    "Some kind of generic data received from somewhere unknown. Ignoring"
                )
            }else if(sendingModule == "KaldiService") {
                Log.i("CoreService", "KaldiService received")
                var newIntent = Intent()
                newIntent.setClassName(
                    "com.example.sapphireassistantframework",
                    "com.example.sapphireassistantframework.PostOffice"
                )
                // Core is just redirecting. Should I include it?
                newIntent.putExtra(FROM,"com.example.vosksttmodule.KaldiService")
                newIntent.putExtra(STDIO,intent.getStringExtra(STDIO))
                startService(newIntent)
            }else{
                Log.i("CoreService","CoreService just received a command from ${intent.getStringExtra(FROM)}," +
                        " containing data: ${intent.getStringExtra(STDIO)}")
                var newIntent = Intent()
                newIntent.setClassName(
                    "com.example.sapphireassistantframework",
                    "com.example.sapphireassistantframework.PostOffice"
                )
                // Core is just redirecting. Should I include it?
                newIntent.putExtra(FROM,intent.getStringExtra(FROM))
                newIntent.putExtra(STDIO,intent.getStringExtra(STDIO))
                startService(newIntent)
            }
        }

        return super.onStartCommand(intent, flags, startId)
    }

    fun buildForegroundNotification(){
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(CHANNEL_ID, NAME, importance).apply {
                description = SERVICE_TEXT
            }

            notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }

        // This is the notification for the foreground service. Maybe have it lead into other bound services
        var builder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.assistant)
            .setContentTitle("Sapphire Assistant Framework")
            .setContentText("SAF is running")
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)

        with(NotificationManagerCompat.from(this)) {
            // I defined notification id as 1, may need to change this later
            notify(1337, builder.build())
        }
    }

    // This is intended to replace scanApplicationManifest
    fun scanInstalledApps(){
        var intent = Intent().setAction("assistant.framework.module.INSTALL")
        intent.addCategory("assistant.framework.module.CATEGORY_DEFAULT")
        var installedApplications = packageManager.getInstalledApplications(PackageManager.GET_META_DATA)

        for(installedApplication in installedApplications) {
            try{
                var metadataBundle = installedApplication.metaData
                if(installedApplication.packageName == "com.example.sapphireassistantframework"){
                    for(key in metadataBundle.keySet()){
                        Log.i("CoreService","Package name: ${installedApplication.packageName}")
                        Log.i("CoreService","Install Service: ${metadataBundle.getString(key)}")
                        // just a placeholder method for now
                        isItRegistered(installedApplication.packageName)
                        var installIntent = Intent().setClassName(installedApplication.packageName,metadataBundle.getString(key)!!)
                        installIntent.action = "assistant.framework.module.INSTALL"
                        startService(installIntent)
                    }
                }
            }catch(exception: Exception){
                continue
            }
        }
    }

    // This isn't really used yet, but it's just meant for internal tracking of apps & updates
    fun isItRegistered(packageName: String): Boolean {
        var installedData = this.getSharedPreferences(
            "com.example.sapphireassistantframework.CORE_PREFERENCES",
            Context.MODE_PRIVATE
        )
        if(installedData.contains(packageName)){
            Log.i("CoreService","${packageName} is already registered")
            return true
        }else{
            var editor = installedData.edit()
            editor.putString(packageName,"registered")
            editor.apply()
            Log.i("CoreService","${packageName} was not registered. It is now")
            return false
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        stopBackgroundServices(sapphire_apps, connections)
        notificationManager.cancel(1337)
    }

    // This is really the 'special feature' handler
    fun intentHandler(intent: Intent) {
        Log.i("CoreService", "IntentHandler received an intent")
        var options: LinkedList<String> = LinkedList<String>()
        options.add("HYPOTHESIS")
        for (extra: String in options.iterator()) {
            Log.i("CoreService", "Testing for extra ${extra}")
            if (intent.hasExtra(extra) == true) {
                var value = intent.getStringExtra(extra)
                Log.i("CoreService", "${value}")
                // Load task from some database
                // Execute task (intent?)
                if (extra.equals("HYPOTHESIS")){
                    if (value != null && value != "") updateUtterance(value)
                }
            }
        }
    }

    // I want to change this to the flat file database
    fun mockStartBackgroundServices(){
        var speechToText = Pair(
            "com.example.sapphireassistantframework",
            "com.example.vosksttmodule.KaldiService")
        var startupApps = listOf(speechToText)

        /** Convoluted, but meant to give a human readable name to running processes.
         * I started VoskSTT as a bound service, so that it will not die as long as a service
         * (CoreService) is bound to it. This gives SAF the control over background processes to
         * help control battery life. Since CoreService is a foreground service it also prevents
         * me from spamming the user with notifications, or having the system kill the background
         * service
         */
        for(classNamePair: Pair<String,String> in startupApps){
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

    fun stopBackgroundServices(sapphire_apps: List<Pair<String,String>>, connections: LinkedList<Pair<String,Connection>>){
        for(connection in connections){
            var service = connection.second
            unbindService(service)
        }
        for(info_pair: Pair<String,String> in sapphire_apps){
            // Do I need to return a pair? Seems annoying.
            Log.i("CoreService","launching ${info_pair.first}, ${info_pair.second}")
            var intent = Intent().setClassName(info_pair.first, info_pair.second)
            stopService(intent)
        }
    }

    fun startBoundService(pkg: String, classname: String): Connection{
        Log.i("CoreService", "binding ${pkg}, ${classname}")
        // This needs to be changed to not pseudocode
        var connection = Connection()
        // This will likely need to change over time
        // Package name is right. Is the class name?
        var coreService: Intent = Intent().setAction("mycroft.BIND")
        // There is an issue with the module name and class name. I will need to fix this
        coreService.setClassName(pkg, classname)
        coreService.putExtra("CORE_PACKAGE", "www.mabase.tech.mycroft")
        coreService.putExtra("CORE_CLASSNAME", "www.mabase.tech.mycroft.CoreDaemon")
        if(coreService.resolveActivity(packageManager) != null) {
            bindService(coreService, connection, Context.BIND_AUTO_CREATE)
        }else{
            Log.e("CoreDaemon", "PackageManager says the service doesn't exist")
        }

        return connection
    }


    // The bound connection. The core attaches to each service as a client, tying them to cores lifecycle
    inner class Connection(): ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            Log.i("CoreService", "Service connected")
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            Log.i("CoreService", "Service disconnected")
        }
    }

    fun checkSpecialFeatureFor(sendingModule:String): Boolean{
        var configs = emptyArray<Objects>()

        for(module in configs){
            if(module as String == sendingModule){
                return true
            }
        }
        return false
    }

    fun updateBackgroundMonitorUI(){
        var coreCentralActivityIntent = Intent()
        var stringBundle = emptyArray<String>()
        var monitorText = ""

        for(string in stringBundle){
            //Enter everything on its own line
            monitorText = "${string}\n"
        }

        coreCentralActivityIntent.setAction("UPDATE")
        coreCentralActivityIntent.putExtra("MONITOR_TEXT",monitorText)
        sendBroadcast(coreCentralActivityIntent)
    }

    // This is just updating the UI. I need to make this more dynamic I think
    fun updateUtterance(utterance: String){
        if (utterance != null && utterance != "") {
            var coreCentralActivityIntent = Intent()
            //coreCentralActivityIntent.setClassName(this, "${packageName}.CoreCentralActivity")
            coreCentralActivityIntent.setAction("UPDATE")

            var json = JSONObject(utterance)
            var text: String = json.getString("text")
            coreCentralActivityIntent.putExtra("HYPOTHESIS", text)
            sendBroadcast(coreCentralActivityIntent)
            Log.i("CoreService", "Text is ${text}")
        }
    }
}