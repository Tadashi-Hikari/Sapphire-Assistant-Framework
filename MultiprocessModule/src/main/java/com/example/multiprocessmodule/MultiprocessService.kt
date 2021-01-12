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

import android.content.Context
import android.content.Intent
import android.os.IBinder
import android.util.Log
import androidx.core.content.edit
import com.example.componentframework.*
import java.io.File
import java.lang.Exception
import java.net.IDN
import kotlin.random.Random

class MultiprocessService: SAFService() {
    //Module specific extras. Should this be moved to SAFService?
    val MULTIPROCESS_ID = "assistant.framework.multiprocess.protocol.ID"
    val SEQUENCE_NUMBER = "assistant.framework.multiprocess.protocol.SEQUENCE_NUMBER"
    // I think I want to replace this with a simple record Jar or JSON file. Whichever is easier to parse
    var sharedPreferences = getSharedPreferences("com.example.multiprocessmodule", Context.MODE_PRIVATE)

    override fun onBind(intent: Intent?): IBinder? {
        TODO("Not yet implemented")
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        Log.i("MultiprocessService","Multiprocess intent received")
        // If it is something new, give it an ID and send it out
        if(intent.hasExtra(MULTIPROCESS_ID)){
            updateIDCount(intent)
        }else{
            var ID = Random.nextInt()
            intent.putExtra(MULTIPROCESS_ID,ID.toString())
            sharedPreferences.edit{
                putInt(ID.toString(),0)
                apply()
            }
            startMultiService(intent)
        }
        return super.onStartCommand(intent, flags, startId)
    }

    // This is meant to send them all along
    fun startMultiService(intent: Intent){
        var route = intent.getStringExtra(ROUTE)
        // "(" indicates the start of a multiprocess, ")" indicates a join
        var intentList = route!!.removeSurrounding("(",")")
        Log.i("MultiprocessService","The modules to be multiprocessed are as follows: ${intentList}")
        var intents = intentList.split(",")
        var sequenceNumber = 0
        for(intent in intents){
            sequenceNumber++
            // I am using ; as the delimeter to separte packageName;className
            var packageClass = intent.split(";")
            // Mark which intent in the sequence this is
            outgoingIntent.putExtra(SEQUENCE_NUMBER,sequenceNumber)
            // This should send a duplicate intent to each intent in the route
            outgoingIntent.setClassName(packageClass[0],packageClass[1])
            startService(outgoingIntent)
        }
    }

    // I need a way to store & aggregate all info. I think a record Jar (or JSON) will work just fine
    fun updateIDCount(intent: Intent){
        var id = intent.getStringExtra(MULTIPROCESS_ID)
        var sequenceNumber = intent.getStringExtra(SEQUENCE_NUMBER)
        // if sequenceNumber == unique,
        var count = sharedPreferences.getInt(intent.getStringExtra(MULTIPROCESS_ID),-1)
        if(count-- == 0){
            handleAndSend(intent)
        }else {
            sharedPreferences.edit {
                putInt(id.toString(), count)
                apply()
            }
        }
    }

    // It shouldn't matter which intent was received. They should all have the same ID
    fun handleAndSend(intent: Intent){
        // How should I send the information? Aggregated? Marked up? I'm inclined to send it record jar or JSON style
        // I can have it retrieve a host and port, then it can also make socket connections
        intent.putExtra(MESSAGE, getFinalMessage(intent.getStringExtra(MULTIPROCESS_ID)!!))
        var routeData = intent.getStringExtra(ROUTE)!!
        var route = parseRoute(routeData)
        outgoingIntent.setClassName(this,getNextAlongRoute(route))
        startService(outgoingIntent)
    }

    fun getFinalMessage(id: String): String{
        return ""
    }
}