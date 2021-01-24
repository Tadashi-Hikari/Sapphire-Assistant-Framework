package com.example.multiprocessmodule

import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import android.util.Log
import com.example.componentframework.SAFService
import org.json.JSONObject

/**
*The data sent back from the InstallService is in DATA_KEYS, not message. How would this be
* sent along a command line? Maybe an Android intent per skilli intent? There's no real way
* to track that, unless I do an ordered Intent system. That said, an ordered intent system could
*
* The complexity here is coming from Android not wanting to share file information, which is a
* less significant issue on Linux. I am basically working around this by either sending the
* text outright, or creating a socket/pipe to send the information over. Therefore DATA_KEYS
* isn't really meant to replicate the CLI way of doing things here. It's one of the workarounds
* I need for Android
*/

class MultiprocessSimplified: SAFService(){
    var JSONDatabase = JSONObject()
    var MULTIPROCESS_ID = "something"
    var SEQUENCE_ID = "android.SEQUENCE_ID"

    override fun onBind(intent: Intent?): IBinder? {
        TODO("Not yet implemented")
    }

    /**
     * The purpose of evaluateMultiprocess() is to take an intent, and store its information until all intents have returned for a specific process
     * If it is the last intent in the sequence, then it calls sendFinalData()
     */
    fun evalutateMultiprocessIntent(intent: Intent){
        var JSONMultiprocessRecord = JSONObject()
        try {
            // Get the multiprocess record for the multiprocess id
            JSONMultiprocessRecord = JSONDatabase.getJSONObject(intent.getStringExtra(MULTIPROCESS_ID))
        }catch(exception: Exception){
            Log.e("MultiprocessService","There was an error loading the JSONRecord")
        }

        var JSONIntentRecord = JSONObject()
        JSONIntentRecord.put(DATA_KEYS,intent.getStringArrayListExtra(DATA_KEYS))
        for(dataKey in intent.getStringArrayListExtra(DATA_KEYS)!!){
            //Bold to assume that it will only ever be data keys
            // I need to do something to account for duplicate names
            JSONIntentRecord.put(dataKey,intent.getStringExtra(dataKey))
        }
        var SEQUENCE_NUMBER = intent.getStringExtra(SEQUENCE_ID)
        // Why am I meshing them here? This is a potential way to flatten the JSON down to two levels instead of three
        // Should this be JSONMultiprocessRecord, not JSONDatabase?
        // See sendFinalData for this in action
        JSONDatabase.put(MULTIPROCESS_ID+SEQUENCE_NUMBER, JSONIntentRecord)
        databaseFile.writeText(JSONDatabase.toString())
    }

    fun sendFinalData(intent: Intent){
        var JSONMultiprocessRecord = JSONObject()
        var dataKeys = arrayListOf<String>()
        var outgoingIntent = Intent()
        for(key in JSONMultiprocessRecord.keys()){
            // This is used to give the intents unique ids. That said, I could just do this in mulitprocess database directly
            if(key.contains(SEQUENCE_ID)){
                var JSONIntentRecord = JSONMultiprocessRecord.getJSONObject(key)
                // I need to add something to make sure dataKeys doesn't collide w/ keys of the same name
                dataKeys.add(JSONIntentRecord.getString(DATA_KEYS))
                for(dataKey in dataKeys){
                    // Bold to assume it is a string
                    outgoingIntent.putExtra(dataKey, JSONIntentRecord.getString(dataKey))
                }
                // Do I need to copy any data other than the DATA_KEYs? I may need to copy the message
            }else{
                // This copys all of the original intent data.
                outgoingIntent.putExtra(key,JSONMultiprocessRecord.getString(key))
            }
        }
        startService(outgoingIntent)
    }
}