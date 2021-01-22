package com.example.multiprocessmodule

import android.content.Intent
import android.util.Log
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

class MultiprocessSimplified {
    var JSONDatabase = JSONObject()
    var MULTIPROCESS_ID = "something"
    var DATA_KEYS = "something"
    var SEQUENCE_ID = "android.SEQUENCE_ID"

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
            JSONIntentRecord.put(dataKey,intent.getStringExtra(dataKey))
        }
        var SEQUENCE_NUMBER = intent.getStringExtra(SEQUENCE_ID)
        JSONDatabase.put(SEQUENCE_ID+SEQUENCE_NUMBER)
    }

    fun sendFinalData(intent: Intent){
        var JSONMultiprocessRecord = JSONObject()
        var dataKeys = arrayListOf<String>()
        var outgoingIntent = Intent()
        for(key in JSONMultiprocessRecord.keys()){
            if(key.contains(SEQUENCE_ID)){
                var JSONIntentRecord = JSONMultiprocessRecord.getJSONObject(key)
                // I need to add something to make sure dataKeys doesn't collide w/ keys of the same name
                dataKeys.add(JSONIntentRecord.getString(DATA_KEYS))
            }
        }
    }
}