package com.example.multiprocessmodule

import android.content.Intent
import android.os.IBinder
import android.util.Log
import com.example.componentframework.SAFService
import org.json.JSONObject
import java.io.File
import java.util.*
import kotlin.math.absoluteValue
import kotlin.random.Random

/**
 * I may need to queue this module, to prevent an intent from coming in the same time its file is
 * being read.
 *
 * I really need to redesign this module. The text is sent back under data_keys, but MESSAGE is not
 * used. I feel that this is unexpected compared to all other modules, and doesn't follow a textual
 * command line example
 */

class MultiprocessService: SAFService(){

    val MULTIPROCESS_ID = "assistant.framework.multiprocess.protocol.ID"
    val SEQUENCE_NUMBER = "assistant.framework.multiprocess.protocol.SEQUENCE_NUMBER"
    val SEQUENCE_TOTAL = "assistant.framework.multiprocess.column.SEQUENCE_TOTAL"
    var PRIMARY_KEY = "assistant.framewoke.multiprocess.column.PRIMARY_KEY"

    lateinit var databaseFile: File
    lateinit var JSONDatabase: JSONObject

    override fun onCreate() {
        super.onCreate()
        JSONDatabase = loadDatabase()
    }

    override fun onStartCommand(startIntent: Intent?, flags: Int, startId: Int): Int {
        try{
            var intent = startIntent!!
            Log.i("MultiprocessService","MultiprocessService intent received")
            if(intent.hasExtra(MULTIPROCESS_ID)){
                if(JSONDatabase.isNull(intent.getStringExtra(MULTIPROCESS_ID))){
                    Log.e("MultiprocessService","This intent has an ID that isn't in our database. discarding")
                }else{
                    Log.i("MultiprocessService","This is a response to an existing multiprocess. Handling... ")
                    evaluateMultiprocessIntent(intent)
                }
            }else{
                Log.i("MultiprocessService","This is a new multiprocess intent. Handling... ")
                startMultiprocessIntent(intent)
            }

        }catch(exception: Exception){
            //Error
            Log.e("MultiprocessService","There was an error with the incoming intent")
            Log.e("MultiprocessService",exception.toString())
        }
        return super.onStartCommand(startIntent, flags, startId)
    }

    fun sendFinalData(intent: Intent){
        var id = intent.getStringExtra(MULTIPROCESS_ID)
        // The multiprocess record name is the multiprocess ID
        // may need to read the file instead
        var JSONMultiprocessRecord = JSONDatabase.getJSONObject(id)
        var dataKeys = arrayListOf<String>()
        var outgoingIntent = Intent()
        // This is taking it from the last intent. This WILL cause an error where only one intents keys are gotten
        outgoingIntent.putStringArrayListExtra(DATA_KEYS, intent.getStringArrayListExtra(DATA_KEYS)!!)

        // This is changing. each JSONRecord will have a PRIMARY_KEY category, which I'll just check for
        for(key in JSONMultiprocessRecord.keys()){
            // Load the record for the key
            if(key.contains(id.toString())){
                var JSONIntentRecord = JSONMultiprocessRecord.getJSONObject(key)
                var dataKeyString = JSONIntentRecord.getString(DATA_KEYS)
                dataKeyString = dataKeyString.substring(1,dataKeyString.length-1)
                dataKeys.addAll(dataKeyString.split(","))
                for(dataKey in dataKeys){
                    outgoingIntent.putExtra(dataKey.trim(),JSONIntentRecord.getString(dataKey.trim()))
                }
                // Do I need to copy any data other than the DATA_KEYs? I may need to
            }else{
                // Copy whatever original data was associated w/ the intent
                outgoingIntent.putExtra(key,JSONMultiprocessRecord.getString(key))
            }
        }
        //outgoingIntent.putExtra(ROUTE,JSONMultiprocessRecord.getString(ROUTE))
        //outgoingIntent.putExtra(MESSAGE,message)
        // This needs to be accounted for, not hardcoded. It would just be the next in the pipeline
        // proper
        outgoingIntent.setClassName(this,"com.example.processormodule.ProcessorTrainingService")
        startService(outgoingIntent)
    }

    // Shiiiiit. This is about to get a lot more complex
    fun evaluateMultiprocessIntent(intent: Intent){
        var JSONMultiprocessRecord = JSONObject()
        try {
            // Get the multiprocess record for the multiprocess id
            JSONMultiprocessRecord = JSONDatabase.getJSONObject(intent.getStringExtra(MULTIPROCESS_ID))
        }catch(exception: Exception){
            Log.e("MultiprocessService","There was an error loading the JSONRecord")
        }
        // get its sequence id
        var sequenceNumber = intent.getIntExtra(SEQUENCE_NUMBER, -1).toString()
        // get the total number of sequences
        var sequeceTotal = JSONMultiprocessRecord.getInt(SEQUENCE_TOTAL)

        // if there is no record for the sequence number
        if(JSONMultiprocessRecord.isNull(sequenceNumber)){
            // Then it is unique. Save the data
            var JSONIntentRecord = JSONObject()
            // make the intent record
            JSONIntentRecord.put(DATA_KEYS,intent.getStringArrayListExtra(DATA_KEYS))
            Log.i("MultiprocessService","Getting DATA_KEYS from intent. Keys are ${intent.getStringArrayListExtra(DATA_KEYS)}")
            for(dataKey in intent.getStringArrayListExtra(DATA_KEYS)!!){
                Log.i("MultiprocessService","Logging key ${dataKey}, value ${intent.getStringArrayListExtra(dataKey)}")
                //Bold to assume that it will only ever be data keys
                // I need to do something to account for duplicate names
                JSONIntentRecord.put(dataKey,intent.getStringArrayListExtra(dataKey))
            }
            // save the intent record with its sequence number as its unique ID
            var id = intent.getStringExtra(MULTIPROCESS_ID)
            // Should I change anything about how this is saved?
            JSONMultiprocessRecord.put(id+sequenceNumber,JSONIntentRecord)
            JSONDatabase.put(id,JSONMultiprocessRecord)
            // if it is the last in the sequence, process all the data out
            if(sequeceTotal-- <= 1){
                Log.i("MultiprocessService","This is the last intent for multiprocess ${intent.getStringExtra(MULTIPROCESS_ID)}. Sending all the data down the line")
                databaseFile.writeText(JSONDatabase.toString())
                sendFinalData(intent)
            // else just wait around for the rest
            }else{
                Log.i("MultiprocessService","Logged the multiprocess intent data. Now waiting for the rest")
                // update the counter
                JSONMultiprocessRecord.put(SEQUENCE_TOTAL,sequeceTotal)
                JSONDatabase.put(id,JSONMultiprocessRecord)
                databaseFile.writeText(JSONDatabase.toString())
            }
        }else{
            // Its a duplicate. Ignore it
            return
        }
    }

    // Basically an orderedBroadcast for startService. No return expected
    fun dispatchOrdered(intentStack: LinkedList<Intent>){
        for(intent in intentStack){
            startService(intent)
        }
    }

    // This creates and starts the multiprocess intent
    fun startMultiprocessIntent(initialIntent: Intent){
        // Get the ID for this multiprocess
        var id = getNewID()
        // Create an empty record for this multiprocess
        var JSONMultiprocessRecord = JSONObject()
        // Put the ID in the outgoing intent
        var outgoingIntent = Intent(initialIntent).putExtra(MULTIPROCESS_ID,id)
        outgoingIntent.setAction(ACTION_SAPPHIRE_MODULE_REQUEST_DATA)

        var routes = routeParser(initialIntent)
        var multiprocessRoute = routes.first.split(",")
        // This is how many individual Intents will be coming back for this multiprocess
        JSONMultiprocessRecord.put(SEQUENCE_TOTAL,multiprocessRoute.size)
        // This is the route for it to traverse once all the data comes back
        JSONMultiprocessRecord.put(ROUTE,routes.second)
        for(route in multiprocessRoute){
            // I am using ; as the delimeter to separte packageName;className
            var packageClass = route.split(";")
            // Mark which intent in the sequence this is
            outgoingIntent.setClassName(packageClass[0],packageClass[1])
            Log.i("MultiprocessService","This intent is going to ${packageClass[0]};${packageClass[1]}")
            outgoingIntent.putExtra(SEQUENCE_NUMBER,multiprocessRoute.indexOf(route))
            // So the retrieveal skill knows to return here
            outgoingIntent.putExtra(ROUTE,"${packageName};com.example.multiprocessmodule.MultiprocessService")
            Log.i("MultiprocessService","Starting outgoing intent for MULTIPROCESS_ID ${id}")
            startService(outgoingIntent)
        }
        // The ID can be used to look up the record, so that its key
        JSONDatabase.put(id,JSONMultiprocessRecord)
        databaseFile.writeText(JSONDatabase.toString())
    }


    fun routeParser(intent: Intent): Pair<String,String>{
        var route = intent.getStringExtra(ROUTE)!!
        var start = route.indexOf("(")+1
        var end = route.indexOf(")",start)
        var multiprocessRoute = route.substring(start,end)
        var remainingRoute = route.substring(end+1,)
        return Pair(multiprocessRoute,remainingRoute)
    }

    fun getNewID(): String{
        var id = -1
        while((JSONDatabase.has(id.toString())) == true or (id == -1)){
            id = Random.nextInt().absoluteValue
        }
        return id.toString()
    }

    fun loadDatabase(): JSONObject{
        databaseFile = File(filesDir, "MultiprocessDatabase.txt")
        if(databaseFile.exists()){
            Log.i("MultiprocessService","Database loaded")
            return JSONObject(databaseFile.readText())
        }
        Log.i("MultiprocessService","Database created")
        return JSONObject()
    }

    override fun onBind(intent: Intent?): IBinder? {
        TODO("Not yet implemented")
    }
}