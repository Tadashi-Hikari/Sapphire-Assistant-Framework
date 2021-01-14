package com.example.multiprocessmodule

/**
 * This is the multiprocess main service. Right now I am writing it to count the max number of
 * modules, but I suppose I can change it to also put modules in the right order
 *
 * Maybe I should have it use a database or flat file, rather than sharedPreferences
 *
 * Is there a way I can have it wait for something w/ an externally allocated ID? this would
 * allow core to send all of its data to the multiprocessor, and it will wait until it's all recieved
 * Maybe I should make a separate aggriagtor for that.
 */

import android.content.Intent
import android.os.IBinder
import android.util.Log
import com.example.componentframework.*
import org.json.JSONObject
import java.io.File
import kotlin.random.Random

class MultiprocessService: SAFService() {
    //Module specific extras. Should this be moved to SAFService?
    val MULTIPROCESS_ID = "assistant.framework.multiprocess.protocol.ID"
    val SEQUENCE_NUMBER = "assistant.framework.multiprocess.protocol.SEQUENCE_NUMBER"

    override fun onBind(intent: Intent?): IBinder? {
        TODO("Not yet implemented")
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        Log.i("MultiprocessService","Data keys ${intent.getStringArrayListExtra(DATA_KEYS)}")
        var file = File(filesDir,"MulitprocessDatabase.txt")
        var databaseJSON = JSONObject()
        if(file.exists()) {
            databaseJSON = JSONObject(file.readText())
        }

        Log.i("MultiprocessService","Multiprocess intent received")
        // If it is something new, give it an ID and send it out
        if(intent.hasExtra(MULTIPROCESS_ID)){
            if(updateIDCount(intent)){
                handleAndSend(intent)
            }
        }else{
            var id = Random.nextInt()
            intent.putExtra(MULTIPROCESS_ID,id)
            var recordJSON = JSONObject()
            recordJSON.put(MULTIPROCESS_ID,id)
            recordJSON.put("COUNT",0)
            databaseJSON.put(id.toString(),recordJSON.toString())
            file.writeText(databaseJSON.toString())

            broadcastMultiService(intent)
        }
        return super.onStartCommand(intent, flags, startId)
    }

    // This is meant to send them all along
    fun broadcastMultiService(intent: Intent){
        var route = intent.getStringExtra(ROUTE)!!
        // "(" indicates the start of a multiprocess, ")" indicates a join
        //var intentList = route!!.removeSurrounding("(",")")
        var intentListPartial = route.split("(")
        var multiprocessList = mutableListOf<String>()
        for(intentString in intentListPartial){
            if(intentString.contains(")")){
                var temp = intentString.split(")")
                multiprocessList.add(temp[0])
            }else{
                continue
            }
        }
        // I am assuming that there is a () in this intent. I need to fix otherwise it will crash
        Log.i("MultiprocessService","The modules to be multiprocessed are as follows: ${multiprocessList}")
        var intents = multiprocessList[0].split(",")
        var sequenceNumber = 0
        for(intentString in intents){
            sequenceNumber++
            // I am using ; as the delimeter to separte packageName;className
            var packageClass = intentString.split(";")
            // Mark which intent in the sequence this is
            intent.putExtra(SEQUENCE_NUMBER,sequenceNumber)
            // This should send a duplicate intent to each intent in the route
            intent.setClassName(packageClass[0],packageClass[1])
            intent.setAction(ACTION_SAPPHIRE_MODULE_REQUEST_DATA)
            Log.i("MultiprocessService","Dispatching intent to ${packageClass[0]};${packageClass[1]}")
            Log.i("MultiprocessService","Requesting data keys ${intent.getStringArrayListExtra(DATA_KEYS)}" )
            startService(intent)
        }
        // I don't like how ugly and not-understandable this is
        var newRoute = "com.example.sapphireassistantframework;com.example.processormodule.ProcessorCentralService"
        var file = File(filesDir,"MultiprocessDatabase.txt")
        Log.i("MultiprocessService","New route is as follows: ${newRoute}")
        var databaseJSON = JSONObject()
        var recordJSON = JSONObject()
        recordJSON.put(ROUTE,newRoute)
        databaseJSON.put(intent.getIntExtra(MULTIPROCESS_ID,0).toString(),recordJSON)
    }

    // I need a way to store & aggregate all info. I think a record Jar (or JSON) will work just fine
    // I don't like that updateID count itself is has handleAndSend
    fun updateIDCount(intent: Intent): Boolean{
        var id = intent.getStringExtra(MULTIPROCESS_ID)
        var sequenceNumber = intent.getStringExtra(SEQUENCE_NUMBER)
        var file = File(filesDir,"MulitprocessDatabase.txt")
        var databaseJSON = JSONObject(file.readText())
        var recordJSON = databaseJSON.getJSONObject(id)

        // if sequenceNumber == unique,
        var count = recordJSON.getInt("COUNT")
        if(count-- == 0){
            return true
        }else {
            recordJSON.put("COUNT",count)
            }
        databaseJSON.put(id,recordJSON.toString())
        file.writeText(databaseJSON.toString())
        return false
    }

    // It shouldn't matter which intent was received. They should all have the same ID
    fun handleAndSend(intent: Intent){
        // How should I send the information? Aggregated? Marked up? I'm inclined to send it record jar or JSON style
        // I can have it retrieve a host and port, then it can also make socket connections
        intent.putExtra(MESSAGE, getFinalMessage(intent.getStringExtra(MULTIPROCESS_ID)!!))
        var routeData = intent.getStringExtra(ROUTE)!!
        var route = parseRoute(routeData)
        intent.setClassName(this,getNextAlongRoute(route))
        startService(intent)
    }

    fun getFinalMessage(id: String): String{
        return ""
    }
}