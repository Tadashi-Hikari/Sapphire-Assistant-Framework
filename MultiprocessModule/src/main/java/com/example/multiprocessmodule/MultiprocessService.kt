package com.example.multiprocessmodule

/**
 * This is the multiprocess main service. Right now I am writing it to count the max number of
 * modules, but I suppose I can change it to also put modules in the right order
 */

import android.content.Intent
import android.os.IBinder
import android.util.Log
import com.example.componentframework.*
import java.io.File
import java.lang.Exception
import kotlin.math.PI

class MultiprocessService: SAFService() {
    // Timeout after 3 minutes
    val TIMEOUT = "180"
    val ID = "assistant.framework.protocol.ID"

    override fun onBind(intent: Intent?): IBinder? {
        TODO("Not yet implemented")
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        Log.i("MultiprocessService","MultiprocessService intent received")
        bindCoreService()
        if(verifyID(intent)){
            updateID(intent)
        }
        if(allSequencesReceived(intent)){
            handleAndSend(intent)
        }

        return super.onStartCommand(intent, flags, startId)
    }

    fun handleAndSend(intent: Intent){
        var outgoingIntent = Intent()
        var pipelineData = intent.getStringExtra(PIPELINE)!!
        var pipeline = parsePipeline(pipelineData)
        outgoingIntent.setClassName(this,getNextInPipeline(pipeline))

        if(needsAggrigation(id)){
            aggregate(id)
            // What data is sent over STDIO from this?
            outgoingIntent.putExtra(STDIO,"something")
            outgoingIntent.putStringArrayListExtra("assistant.framework.processor.DATA",datum)
        }else{
            // What data is sent over STDIO from this?
            outgoingIntent.putExtra(STDIO,"something")
            for(data in allData as Map<String,ArrayList<String>>){
                outgoingIntent.putStringArrayListExtra(data.key,data.value)
            }
        }
        startService(outgoingIntent)
    }

    fun countSequence(intent: Intent){
        try{

        }catch(exception: Exception){
            Log.e("MultiprocessService","There was an error with the sequencing of this intent")
        }
    }

    fun needsAggrigation(id: String): Boolean{
        return false
    }

    fun verifyID(intent: Intent): Boolean{
        try{
            var id = intent.getStringExtra(ID)!!
            // How does this return if it doesn't exist
            // Can I just use getPreferences (can it be called from a Service?)
            var sharedPreferences = this.getSharedPreferences(id, MODE_PRIVATE)
            if(sharedPreferences == null) {
                createIDTracker(id)
                // This doesn't work w/ the returned value
                updateID(intent)
                return false
            }
        }catch(exception: Exception){
            Log.e("MultiprocessService","There was an error with the ID of this intent")
        }

        return true
    }

    fun createIDTracker(id: String){
        var sharedPreferences = this.getSharedPreferences(id, MODE_PRIVATE)
    }

    fun allSequencesReceived(intent: Intent): Boolean{
        return true
    }

    fun updateID(intent: Intent): Boolean{
        var id = intent.getStringExtra(ID)!!
        var KEY = "assistant.framework.protocol.SEQUENCE_NUMBER"
        var total = totalNumberOfModules(id)
        var sharedPreferences = this.getSharedPreferences(id, MODE_PRIVATE)
        var count = sharedPreferences.getInt(KEY,-1)
        // I should probably do something to handle an error
        if(count != -1){
            count++
        }

        if(count == total){
            return true
        }

        return false
    }

    fun totalNumberOfModules(id: String): Int{
        var sharedPreferences = this.getSharedPreferences(id, MODE_PRIVATE)
        val total = "assistant.framework.protocol.SEQUENCE_TOTAL"
        // I need to do some kind of error checking
        return sharedPreferences.getInt(total, -1)
    }

    fun aggregate(filesToAggregate: List<File>): List<String>{
        // This takes files, and turns them into lines in a single file, so it's easy to pass along
        return emptyList()
    }

    // This needs to bind the core service until either TIMEOUT
    fun bindCoreService(){
        // Should I formally incorporate this?
        //intent.putExtra(timeout,TIMEOUT)
    }

    fun unbindCoreService(){
    }
}