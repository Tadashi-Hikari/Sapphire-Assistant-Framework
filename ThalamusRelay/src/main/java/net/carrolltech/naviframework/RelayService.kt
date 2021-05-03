package net.carrolltech.naviframework

import android.content.Intent
import net.carrolltech.nervoussystemlibrary.StatefulService
import org.json.JSONObject

class RelayService: StatefulService(){

    var routeTable = mutableMapOf<String,String>()
    var currentState = emptyMap<String,String>()

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when(intent?.action){
            ACTION_SAPPHIRE_INITIALIZE -> doInit()
            // This could be used by a co-core
            ACTION_SAPPHIRE_STATE_CREATE -> null
            ACTION_SAPPHIRE_STATE_UPDATE -> updateState()
            ACTION_SAPPHIRE_STATE_READ -> readState()
            ACTION_SAPPHIRE_STATE_DESTROY -> onDestroy()
            ACTION_SAPPHIRE_SHUTDOWN -> shutdown()
            else -> relay()
        }
        return super.onStartCommand(intent, flags, startId)
    }

    // Is there a reason I don't have these as actions?
    fun relay(){
        // Actions are either: Block (ID), Start (Route), Stop all
        when(checkStateConditions()){
            "BLOCK" -> block()
            "START" -> start()
            // This stops ALL current actions in the system
            "STOP" -> stopAll()
            null -> send()
        }
    }

    fun stopAll(){

    }

    fun start(){

    }

    fun block(){

    }

    fun send(){

    }

    fun checkStateConditions(): String?{
        var conditionTables = emptyMap<String,JSONObject>()

        for(table in conditionTables.values){
            for(condition in table.keys()) {
                // This is basically context, but for the app
                for (state in currentState.keys) {
                    if (condition == state){
                        return table.getString(condition)
                    }
                }
            }
        }
        return null
    }

    fun setRouteTable(){
        //routeTable = loadedTable
    }
}