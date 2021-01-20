package com.example.multiprocessmodule

import android.content.Intent
import android.os.IBinder
import android.util.JsonReader
import android.util.Log
import com.example.componentframework.SAFService
import org.json.JSONObject
import java.io.File
import kotlin.math.absoluteValue
import kotlin.random.Random

/**
 * I may need to queue the intents coming in, so that I don't run in to database issues.
 */

class MultiprocessService: SAFService(){

    val MULTIPROCESS_ID = "assistant.framework.multiprocess.protocol.ID"
    val SEQUENCE_NUMBER = "assistant.framework.multiprocess.protocol.SEQUENCE_NUMBER"
    val SEQUENCE_TOTAL = "assistant.framework.multiprocess.column.SEQUENCE_TOTAL"
    lateinit var databaseFile: File
    lateinit var JSONDatabase: JSONObject

    override fun onCreate() {
        super.onCreate()
        JSONDatabase = loadDatabase()
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        try{
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
        return super.onStartCommand(intent, flags, startId)
    }

    fun sendFinalData(intent: Intent){
        var JSONRecord = JSONDatabase.getJSONObject(intent.getStringExtra(MULTIPROCESS_ID))
        var ignore = listOf(SEQUENCE_NUMBER,SEQUENCE_TOTAL,ROUTE)
        var message = ""
        var intent = Intent()

        // I don't like how this is laid out. It's kind of a bitch. maybe I should have an identifier
        // to make a regex easy. I'm hesitent to store another nested JSON
        for(key in JSONRecord.keys()){
            if(ignore.contains(key) == false){
                message += JSONRecord.getString(key)
            }
        }
        intent.putExtra(ROUTE,JSONRecord.getString(ROUTE))
        intent.putExtra(MESSAGE,message)
        // This needs to be accountet for, not hardcoded
        intent.setClassName(this,"com.example.processormodule.ProcessorTrainingService")
        startService(intent)
    }

    // Shiiiiit. This is about to get a lot more complex
    fun evaluateMultiprocessIntent(intent: Intent){
        var JSONRecord = JSONObject()
        try {
             JSONRecord = JSONDatabase.getJSONObject(intent.getStringExtra(MULTIPROCESS_ID))
        }catch(exception: Exception){
            Log.e("MultiprocessService","There was an error loading the JSONRecord")
        }
        var sequenceNumber = intent.getIntExtra(SEQUENCE_NUMBER, -1).toString()
        var sequeceTotal = JSONRecord.getInt(SEQUENCE_TOTAL)

        if(JSONRecord.isNull(sequenceNumber)){
            // This maps the unique repsonses of each intent
            JSONRecord.put(sequenceNumber,intent.getStringExtra(MESSAGE))
            if(sequeceTotal-- <= 1){
                Log.i("MultiprocessService","This is the last intent for this multiprocess. Sending all the data down the line")
                sendFinalData(intent)
            }else{
                Log.i("MultiprocessService","Logged the multiprocess intent data. Now waiting for the rest")
                JSONRecord.put(SEQUENCE_TOTAL,sequeceTotal)
            }
        }else{
            // Its a duplicate. Ignore it
            return
        }
    }

    // This creates and starts the multiprocess intent
    fun startMultiprocessIntent(initialIntent: Intent){
        // Get the ID for this multiprocess
        var id = getNewID()
        // Create an empty record for this multiprocess
        var JSONRecord = JSONObject()
        JSONRecord.put(MULTIPROCESS_ID,id)
        // Put the ID in the outgoing intent
        var outgoingIntent = Intent(initialIntent).putExtra(MULTIPROCESS_ID,id)
        outgoingIntent.setAction(ACTION_SAPPHIRE_MODULE_REQUEST_DATA)

        var routes = routeParser(initialIntent)
        var multiprocessRoute = routes.first.split(",")
        JSONRecord.put(SEQUENCE_TOTAL,multiprocessRoute.size)
        JSONRecord.put(ROUTE,routes.second)
        for(route in multiprocessRoute){
            // I am using ; as the delimeter to separte packageName;className
            var packageClass = route.split(";")
            // Mark which intent in the sequence this is
            outgoingIntent.setClassName(packageClass[0],packageClass[1])
            Log.i("MultiprocessService","This intent is going to ${packageClass[0]};${packageClass[1]}")
            outgoingIntent.putExtra(SEQUENCE_NUMBER,multiprocessRoute.indexOf(route))
            // So the retrieveal skill knows to return here
            outgoingIntent.putExtra(ROUTE,"${packageName};com.example.multiprocessmodule.MultiprocessService")
            Log.i("MultiprocessService","Starting outgoing intent")
            startService(outgoingIntent)
        }
        // The ID can be used to look up the record, so thats its primary key
        JSONDatabase.put(id,JSONRecord)
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