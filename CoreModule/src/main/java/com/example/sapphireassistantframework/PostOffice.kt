package com.example.sapphireassistantframework

import android.content.Intent
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
        Log.i("PostOffice","Intent received from ${intent.getStringExtra(FROM)!!}")
        loadMailRoute(intent!!)

        return super.onStartCommand(intent, flags, startId)
    }

    // This needs to be totally reworked
    /**
     * It's gonna work like this. Whatever is the LAST thing in the pipeline, core will read and upload pipeline data for.
     */
    fun loadMailRoute(intent: Intent){
        var pipelines = mutableMapOf<String,String>()
        // kaldiservice, in this example, is FROM not STDIN
        pipelines.put("com.example.vosksttmodule.KaldiService",
            "com.example.parsermodule.UtteranceProcessing," +
                    "com.example.sapphireassistantframework.CoreService")
        //calendar, in this example, is STDIN, not FROM
        pipelines.put("calendar","com.example.calendarskill.calendar")

        //notifyHooks()
        loadConfig()
        //checkConditionals()

        var newPipeline = ""
        if(intent.hasExtra(PIPELINE)){
            newPipeline = pipelines.get(intent.getStringExtra(PIPELINE))!!
        }else if(intent.hasExtra(TO)){
            newPipeline = pipelines.get(intent.getStringExtra(TO))!!
            // This could get caught up on internal messaging. It's a catch-all though
        }else if(intent.hasExtra(FROM)){
            newPipeline = pipelines.get(intent.getStringExtra(FROM))!!
        }else{
            newPipeline = DEFAULT
        }

        // Check to make sure.....
        if(newPipeline == "Not Found"){
            Log.i("PostOffice","Nothing was found for that pipeline, using default")
            newPipeline = DEFAULT
        }
        startService(intent)
    }

    fun loadConfig(){

    }
}