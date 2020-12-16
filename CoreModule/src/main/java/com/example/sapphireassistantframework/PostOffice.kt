package com.example.sapphireassistantframework

import android.content.Intent
import android.os.Bundle
import android.os.IBinder
import android.util.Log
import com.example.componentframework.SAFService
import java.lang.Exception

class PostOffice: SAFService(){
    override fun onBind(intent: Intent?): IBinder? {
        TODO("Not yet implemented")
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.i("PostOffice","Intent received")
        Log.i("PostOffice","${intent!!.component!!.className}")
        loadMailRoute(intent!!)

        return super.onStartCommand(intent, flags, startId)
    }

    fun loadMailRoute(intent: Intent){
        var pipelines = mutableMapOf<String,String>()
        // kaldiservice, in this example, is FROM not STDIN
        pipelines.put("com.example.vosksttmodule.kaldiservice",
            "com.example.parsermodule.UtteranceProcessing," +
                    "com.example.sapphireassistantframework.CoreService")
        //calendar, in this example, is STDIN, not FROM
        pipelines.put("calendar","com.example.calendarskill.calendar")

        var from = intent.getStringExtra(FROM)
        try{
            var pipeline = pipelines.get(from)!!.split("=")
            // Put the pipeline string in
            Log.i("PostOffice","Pipeline found: ${pipeline.get(0)}")
            intent.putExtra(PIPELINE,pipeline.get(0))
            Log.i("PostOffice","Next module in pipeline: ${parsePipeline(intent)}")
            //For now, I'm using the same package. I will need to account for this....
            intent.setClassName(this,parsePipeline(intent))
            // Mighty bold of me to assume that it is going to be a service...
            startService(intent)
        }catch(exception: Exception){
            Log.e("PostOffice","No pipeline info exists")
        }
    }
}