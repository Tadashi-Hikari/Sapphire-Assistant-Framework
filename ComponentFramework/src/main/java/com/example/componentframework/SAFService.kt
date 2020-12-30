package com.example.componentframework

import android.app.Service
import android.content.ComponentName
import android.content.Intent
import android.os.Bundle
import android.util.Log
import java.util.regex.Pattern
import kotlin.math.PI

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
    var initialIntent = Intent()
    var SAFIntent = Intent()


    // This is for having a SAF compontent pass along the pipeline w/o a callback to core
    fun parsePipeline(string: String): List<String>{
        var pipeline = emptyList<String>()
        pipeline = string.split(",")
        return pipeline
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        return super.onStartCommand(intent, flags, startId)
    }
}