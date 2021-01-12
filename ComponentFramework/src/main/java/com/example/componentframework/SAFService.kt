package com.example.componentframework

import android.app.Service
import android.content.Intent

abstract class SAFService: Service(){
    // Standard extras
    val MESSAGE="assistant.framework.protocol.MESSAGE"
    val STDERR="assistant.framework.protocol.STDERR"
    val POSTAGE="assistant.framework.protocol.POSTAGE"
    val ROUTE="assistant.framework.protocol.ROUTE"
    val CORE="assistant.framework.protocol.CORE"

    // Module specific extras
    val PROCESSOR_ENGINE="assistant.framework.processor.protocol.ENGINE"
    val PROCESSOR_VERSION="assistant.framework.processor.protocol.VERSION"
    val DATA_KEYS="assistant.framework.module.protocol.DATA_KEYS"

    // Actions
    val ACTION_SAPPHIRE_CORE_BIND="assistant.framework.core.action.BIND"
    // This is sent to the CORE from the module, so the core can handle the registration process
    val ACTION_SAPPHIRE_CORE_REGISTER = "assistant.framework.core.action.REGISTER"
    // This is for a module to request *all* data from the core (implicit intent style)
    val ACTION_SAPPHIRE_CORE_REQUEST_DATA="assistant.framework.core.action.REQUEST_DATA"

    val ACTION_SAPPHIRE_MODULE_REGISTER = "assistant.framework.module.action.REGISTER"
    // This is for core to request data from a specific module
    val ACTION_SAPPHIRE_MODULE_REQUEST_DATA="assistant.framework.module.action.REQUEST_DATA"
    val ACTION_SAPPHIRE_TRAIN="assistant.framework.processor.action.TRAIN"


    var outgoingIntent = Intent()

    // This is for having a SAF compontent pass along the route w/o a callback to core
    fun parseRoute(string: String): List<String>{
        var route = emptyList<String>()
        route = string.split(",")
        return route
    }

    // This doesn't actually impact the list, which I would have to return... Should it take intent?
    fun getNextAlongRoute(route: List<String>): String{
        return route[0]
    }

    fun addToRoute(newRoute: String){
        // This takes the existing route and adds some modules in to it
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        outgoingIntent = intent
        // Clear out the ClassName. Everything else is the same to pass along
        outgoingIntent.setClassName("","")
        return super.onStartCommand(intent, flags, startId)
    }
}