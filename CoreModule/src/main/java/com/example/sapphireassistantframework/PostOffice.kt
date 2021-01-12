package com.example.sapphireassistantframework

import android.content.Intent
import android.content.pm.ResolveInfo
import android.os.IBinder
import android.util.Log
import com.example.componentframework.SAFService
import java.lang.Exception

class PostOffice: SAFService(){
    var DEFAULT = ""

    override fun onBind(intent: Intent?): IBinder? {
        TODO("Not yet implemented")
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        try {

            if(intent.action == ACTION_SAPPHIRE_CORE_REGISTER) {
                Log.i("PostOffice", "Registration action received")
                // This doesn't have to be processor.DATA exclusive
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
            }else if(intent.action == ACTION_SAPPHIRE_CORE_REQUEST_DATA){
                // I need to use the CoreService install process somehow, without duplicating code
                checkInstalledModules()
                for(module in uninstalledModules){
                    installModule()
                }
                checkForModulesWithData()

                var multiprocessIntent = Intent().setClassName(this,"com.example.multiprocessmodule.MultiprocessService")
                // var route = loadRoute("sapphire_processor_training_route")
                // This is an example of a multiprocess route
                multiprocessIntent.putExtra(ROUTE,"(package1;class1,package2;class2)")
                // -a means aggrigate. I'm using a unix like flag for an example
                multiprocessIntent.putExtra(POSTAGE,"-a")
                for(module: ResolveInfo in modulesWithData){
                    modules.put("PACKAGENAME",module.serviceInfo.packageName)
                    modules.put("CLASSNAME",module.serviceInfo.name)
                }
            }else {
                sortMail(intent)
            }
        }catch(exception: Exception){
            Log.e("PostOffice","Some intent error")
        }
        return super.onStartCommand(intent, flags, startId)
    }

    // This needs to be totally reworked
    /**
     * It's gonna work like this. Whatever is the LAST thing in the pipeline, core will read and upload pipeline data for.
     */
    fun sortMail(intent: Intent){
        var routes = loadRoutes()
        var routeRequest = ""

        //notifyHooks()
        loadConfig()
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
        outgoingIntent.setClassName(this,route.first())
        outgoingIntent.putExtra(MESSAGE,intent.getStringExtra(MESSAGE))
        outgoingIntent.putExtra(ROUTE,routeData)

        startService(outgoingIntent)
    }

    fun loadRoutes(): Map<String,String>{
        var routes = mutableMapOf<String,String>()
        // kaldiservice, in this example, is FROM not STDIN
        routes.put("com.example.vosksttmodule.KaldiService",
            "com.example.processormodule.ProcessorCentralService")
        //calendar, in this example, is STDIN, not FROM
        routes.put("calendar","com.example.calendarskill.Calendar")

        return routes
    }

    fun loadConfig(){
        // This will be added in later
    }
}