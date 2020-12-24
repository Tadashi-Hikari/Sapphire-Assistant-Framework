package com.example.componentframework

import android.app.Service
import android.content.ComponentName
import android.content.Intent
import android.os.Bundle
import android.util.Log
import java.util.regex.Pattern
import kotlin.math.PI

abstract class SAFService: SAFComponent, Service(){
    val STDIO = "ASISTANT_FRAMEWORK_STDIO"
    // This *could* call chatbot....
    val STDERR = "ASSISTANT_FRAMEWORK_STDERR"
    // I don't need this. It's held in intent.component.getClassName
    val FROM = "ASSISTANT_FRAMEWORK_SENDING_MODULE"
    val TO = "ASSISANT_FRAMEWORK_RECEIVENG_MODULE"
    // I think this value is inherently returned in "onStartCommand"
    val PID = "ASSISTANT_FRAMEWORK_PROCESS_ID"
    // The chain the app is supposed to follow
    val PIPELINE = "ASSISTANT_FRAMEWORK_PIPELINE"
    // A reference, to always call back to core
    val CORE = "ASSISTANT_FRAMEWORK_CORE_MODULE"
    var initialIntent = Intent()
    var SAFIntent = Intent()


    // This is for having a SAF compontent pass along the pipeline w/o a callback to core
    fun parsePipeline(intent: Intent): List<String>{
        var pipeline = emptyList<String>()
        try{
            if(intent.hasExtra(PIPELINE)){
                pipeline = intent.getStringExtra(PIPELINE)!!.split(",")
                return pipeline
            }
        }catch (exception: Exception){
            Log.e("SAFService","There is no pipeline to parse!")
        }
        return emptyList()
    }

    fun getNextComponent(intent: Intent): String{
        var pipeline = parsePipeline(intent)

        // Return the next (or only) component in the pipeline. else, send it back to the core
        if (pipeline != emptyList<String>()) {
            if (intent.component != null) {
                Log.i("SAFService","Getting next component for ${intent.getStringExtra(FROM)}")
                var index = pipeline.indexOf(intent.getStringExtra(FROM))
                if(pipeline.size >= index){
                    Log.e("SAFService","There are no more components in the pipeline! Sent from ${intent.getStringExtra(FROM)!!}")
                    return intent.getStringExtra(FROM)!!
                }
            }else{
                return pipeline.first()
            }
        }
        // This could error out to a question. or it could add an action?
        return "com.example.sapphireassistantframework.CoreService"
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        initialIntent = intent
        // is this redundant? I'm assuming this makes a copy, rather than a reference
        SAFIntent = Intent(intent)

        try {
            if(initialIntent.component != null){
                var nextComponent = parsePipeline(initialIntent.component!!.className)
            }
        }catch (exception: Exception){
            Log.e("SAFService","There was an issue preparing SAFIntent")
        }
        return super.onStartCommand(intent, flags, startId)
    }
}