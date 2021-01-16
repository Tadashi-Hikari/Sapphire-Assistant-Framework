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
            if(intent.action == ACTION_SAPPHIRE_MODULE_REGISTER){
                Log.i("PostOffice", "Module registration action received")
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

    fun isItRegistered(packageName: String){

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
        intent.setClassName(this,route.first())
        intent.putExtra(MESSAGE,intent.getStringExtra(MESSAGE))
        intent.putExtra(ROUTE,routeData)

        startService(intent)
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