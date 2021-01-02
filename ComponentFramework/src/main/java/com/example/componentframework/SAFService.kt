package com.example.componentframework

import android.app.Service
import android.content.Intent

abstract class SAFService: Service(){
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


    // This is for having a SAF compontent pass along the pipeline w/o a callback to core
    fun parsePipeline(string: String): List<String>{
        var pipeline = emptyList<String>()
        pipeline = string.split(",")
        return pipeline
    }

    // This doesn't actually impact the list, which I would have to return... Should it take intent?
    fun getNextInPipeline(pipeline: List<String>): String{
        return pipeline[0]
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        return super.onStartCommand(intent, flags, startId)
    }
}