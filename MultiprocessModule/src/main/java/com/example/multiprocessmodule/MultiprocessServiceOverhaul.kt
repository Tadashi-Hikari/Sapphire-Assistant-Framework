package com.example.multiprocessmodule

import android.content.Intent
import android.os.IBinder
import android.util.Log
import com.example.componentframework.SAFService
import org.json.JSONObject
import java.io.File
import kotlin.random.Random

/**
 * I may need to queue the intents coming in, so that I don't run in to database issues.
 */

class MultiprocessServiceOverhaul: SAFService(){

    val MULTIPROCESS_ID = "assistant.framework.multiprocess.protocol.ID"
    val SEQUENCE_NUMBER = "assistant.framework.multiprocess.protocol.SEQUENCE_NUMBER"
    val SEQUENCE_TOTAL = "assistant.framework.multiprocess.column.SEQUENCE_TOTAL"
    lateinit var databaseFile: File
    lateinit var JSONDatabase: JSONObject

    override fun onCreate() {
        super.onCreate()
        loadDatabase()
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        try{
            if(intent.hasExtra(MULTIPROCESS_ID)){
                if(JSONDatabase.isNull(intent.getStringExtra(MULTIPROCESS_ID))){
                    Log.e("MultiprocessService","This intent has an ID that isn't in our database. discarding")
                }else{
                    evaluateMultiprocessIntent(intent)
                }
            }else{
                startMultiprocessIntent(intent)
            }

        }catch(exception: Exception){
            //Error
        }
        return super.onStartCommand(intent, flags, startId)
    }

    fun evaluateMultiprocessIntent(intent: Intent){
        var JSONRecord = JSONDatabase.getJSONObject(intent.getStringExtra(MULTIPROCESS_ID))
        var sequenceNumber = intent.getStringExtra(SEQUENCE_NUMBER)
        var sequeceTotal = JSONRecord.getInt(SEQUENCE_TOTAL)

        if(JSONRecord.isNull(sequenceNumber)){
            // This maps the unique repsonses of each intent
            JSONRecord.put(sequenceNumber,intent.getStringExtra(MESSAGE))
            if(sequeceTotal-- <= 1){
                sendFinalData()
            }else{
                JSONRecord.put(SEQUENCE_TOTAL,sequeceTotal)
            }
        }else{
            // Its a duplicate. Ignore it
            return
        }
    }

    fun startMultiprocessIntent(initialIntent: Intent){
        var id = getNewID()
        var JSONRecord = JSONObject()
        JSONRecord.put(MULTIPROCESS_ID,id)
        var outgoingIntent = Intent(initialIntent).putExtra(MULTIPROCESS_ID,id)

        var routes = routeParser(initialIntent)
        var multiprocessRoute = routes.first.split(",")
        JSONRecord.put(SEQUENCE_TOTAL,multiprocessRoute.size)
        JSONRecord.put(ROUTE,routes.second)
        for(route in multiprocessRoute){
            // I am using ; as the delimeter to separte packageName;className
            var packageClass = route.split(";")
            // Mark which intent in the sequence this is
            outgoingIntent.setClassName(packageClass[0],packageClass[1])
            outgoingIntent.putExtra(SEQUENCE_NUMBER,multiprocessRoute.indexOf(route))
            // So the retrieveal skill knows to return here
            outgoingIntent.putExtra(ROUTE,"${packageName};com.example.multiprocessmodule.MultiprocessService")
            startService(outgoingIntent)
        }
        JSONDatabase.put(MULTIPROCESS_ID,JSONRecord)
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
        while((JSONDatabase.has(id.toString())) or (id == -1)){
            id = Random.nextInt()
        }
        return id.toString()
    }

    fun loadDatabase(): JSONObject{
        databaseFile = File(filesDir, "MultiprocessDatabase.txt")
        if(databaseFile.exists()){
            return JSONObject(databaseFile.readText())
        }
        return JSONObject()
    }

    override fun onBind(intent: Intent?): IBinder? {
        TODO("Not yet implemented")
    }
}