package com.example.sapphireassistantframework

/**
 * This module exists to handle start up and shut down tasks for the assistant framework, as well
 * as acts as a bind-able anchor for any other services. Does moving NotificationService to its own
 * file cause an issue with this service as long running? Does NotificationService need to bind this,
 * so that this doesn't shut down?
 */

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
        scanInstalledModules()
        mockStartBackgroundServices()
        Log.i("CoreService", "Everything should be starting up now")
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        // Should this sorting be done in PostOffice?
        try {
            if (intent.action == ACTION_SAPPHIRE_CORE_BIND) {
                // Do the binding
            }else{
                intent.setClassName(
                    "com.example.sapphireassistantframework",
                    "com.example.sapphireassistantframework.PostOffice"
                )
                startService(intent)
            }
        }catch(exception: Exception){
            Log.e("CoreService","Some intent error")
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

    // There could be an issue here, since it isn't waiting for a callback. I may need to run this through the multiprocess module
    fun scanInstalledModules(){
        var intent = Intent().setAction(ACTION_SAPPHIRE_MODULE_REGISTER)
        var installedSapphireModules = packageManager.queryIntentServices(intent, 0)

        for(module in installedSapphireModules) {
            try{
                var packageName = module.resolvePackageName
                var className = module.serviceInfo.name
                // Check the internal log to see if it's installed or updated
                isItRegistered(packageName)
                intent.setClassName(packageName,className)
                startService(intent)
            }catch(exception: Exception){
                continue
            }
        }
    }

    // This isn't really used yet, but it's just meant for internal tracking of apps & updates
    fun isItRegistered(packageName: String): Boolean {
        var installedData = this.getSharedPreferences(
            // This is a terrible name
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
        stopBackgroundServices(sapphire_apps, connections)
        notificationManager.cancel(1337)
        super.onDestroy()
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

    // These are services that CoreService is creating, and binding to. They're other modules
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
}