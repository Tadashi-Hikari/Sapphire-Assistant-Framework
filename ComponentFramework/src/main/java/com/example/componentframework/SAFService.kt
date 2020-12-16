package com.example.componentframework

import android.app.Service
import android.content.ComponentName
import android.content.Intent
import android.util.Log
import java.util.regex.Pattern

abstract class SAFService: SAFComponent, Service(){
    val STDIO = "ASISTANT_FRAMEWORK_STDIO"
    // This *could* call chatbot
    val STDERR = "ASSISTANT_FRAMEWORK_STDERR"
    // I don't need this. It's held in intent.component.getClassName
    val FROM = "ASSISTANT_FRAMEWORK_SENDING_MODULE"
    // I think this value is inherently returned in "onStartCommand"
    val PID = "ASSISTANT_FRAMEWORK_PROCESS_ID"
    val PIPELINE = "ASSISTANT_FRAMEWORK_PIPELINE"

    fun parsePipeline(intent: Intent): String{
        var pipeline = intent.getStringExtra(PIPELINE)!!
        var order =  pipeline.split(",")
        var component = emptyList<String>()
        try {
            var index = order.indexOf(intent.component!!.className)
            var nextModule = order.get(index+1)
            component = nextModule.split("=")
        }catch(exception: Exception){
            Log.e("SAFService","Classname ${intent.component!!.className} not found in the pipeline. Maybe call the chatbot?")
        }
        return component[0]
    }
}