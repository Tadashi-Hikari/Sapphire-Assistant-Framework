package com.example.sapphireassistantframework

import android.content.Intent
import android.os.IBinder
import android.util.Log
import com.example.componentframework.SAFService

class PostOffice: SAFService(){
    var DEFAULT = ""

    override fun onBind(intent: Intent?): IBinder? {
        TODO("Not yet implemented")
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        Log.i("PostOffice","Intent received from ${intent.getStringExtra(FROM)!!}")
        loadMailRoute(intent)

        return super.onStartCommand(intent, flags, startId)
    }

    // This needs to be totally reworked
    /**
     * It's gonna work like this. Whatever is the LAST thing in the pipeline, core will read and upload pipeline data for.
     */
    fun loadMailRoute(intent: Intent){
        var pipelines = loadPipelines()
        var pipelineRequest = ""
        var outgoingIntent = Intent()

        //notifyHooks()
        loadConfig()
        //checkConditionals()

        // maybe change PIPELINE to PIPELINE_REQUEST
        if(intent.hasExtra(TO)){
            pipelineRequest = intent.getStringExtra(TO)!!
        }else if(intent.hasExtra(FROM)) {
            pipelineRequest = intent.getStringExtra(FROM)!!
            Log.i("PostOffice","pipelineRequest: ${pipelineRequest}")
        }else{
            // currently, the default is to return
            return
        }

        var pipelineData = pipelines.get(pipelineRequest)!!
        var pipeline = parsePipeline(pipelineData)
        // It's going to be the first in the pipeline, right?
        outgoingIntent.setClassName(this,pipeline.first())
        outgoingIntent.putExtra(PIPELINE,pipelineData)

        startService(outgoingIntent)
    }

    fun loadPipelines(): Map<String,String>{
        var pipelines = mutableMapOf<String,String>()
        // kaldiservice, in this example, is FROM not STDIN
        pipelines.put("com.example.vosksttmodule.KaldiService",
            "com.example.parsermodule.UtteranceProcessing")
        //calendar, in this example, is STDIN, not FROM
        pipelines.put("calendar","com.example.calendarskill.calendar")

        return pipelines
    }

    fun loadConfig(){
        // This will be added in later
    }
}