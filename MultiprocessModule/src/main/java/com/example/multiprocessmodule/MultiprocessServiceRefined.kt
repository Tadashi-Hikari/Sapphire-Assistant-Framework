package com.example.multiprocessmodule

import android.content.ComponentName
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import android.os.SystemClock
import com.example.componentframework.SapphireFrameworkService
import org.json.JSONObject
import java.io.File
import kotlin.math.absoluteValue
import kotlin.random.Random

/*
This also can be used to wait for a response for a single intent. Perhaps it should have a different name

I believe this may *have* to bind CoreService, now that Uris are the only way to pass around information
I can't really record it to a JSON file the same way.
 */

class MultiprocessServiceRefined: SapphireFrameworkService(){
	// This ID is used to identify when a multiprocess intent is returning
	val MULTIPROCESS_ID = "assistant.framework.multiprocess.protocol.ID"
	// This is the count of multiprocess intents that still need to return for a specific ID
	val SEQUENCE_NUMBER = "assistant.framework.multiprocess.protocol.SEQUENCE_NUMBER"
	// This is the total amount of multiprocess intents under an ID
	val SEQUENCE_TOTAL = "assistant.framework.multiprocess.column.SEQUENCE_TOTAL"

	val MULTIPROCESS_TABLE = "multiprocess.tbl"

	lateinit var databaseFile: File
	lateinit var JSONDatabase: JSONObject
	var connection = Connection()
	// This will be the temp JSON replacement
	var intentStorage = mutableListOf<Intent>()

	// This ties the Multiprocess to Core much more than I wanted. Perhaps I should specify 'levels' of modules
	inner class Connection() : ServiceConnection {
		override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
			Log.i(this.javaClass.name, "Service connected")
		}

		override fun onServiceDisconnected(name: ComponentName?) {
			Log.i(this.javaClass.name, "Service disconnected")
		}
	}

	override fun onCreate(){
		var coreIntent = Intent().setClassName(this.packageName,"com.example.sapphireassistantframework.CoreService")
		bindService(coreIntent,connection,0)
		JSONDatabase = loadDatabase()
		super.onCreate()
	}

	override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
		when(intent?.hasExtra(MULTIPROCESS_ID)){
			true -> evaluateMultiprocessIntent(intent)
			false -> intializeMultiprocessIntent(intent)
			else -> Log.d(this.javaClass.name, "There was an intent error. Stopping Multiprocess Module...")
		}
		return super.onStartCommand(intent, flags, startId)
	}

	fun evaluateMultiprocessIntent(intent: Intent){
		when{
			//value != 1 -> updateAndWait(intent)
			//value == 1 -> sendFinalData(intent)
		}
	}

	fun intializeMultiprocessIntent(intent: Intent){
		var id = getNewID()
		var routePair = expandMultiprocessRoute(intent)

		intent.putExtra(MULTIPROCESS_ID, id)
		// This is just for easy recording. The Sequence number gets overwritten by each local intent
		intent.putExtra(SEQUENCE_NUMBER,routePair.first.size)

		for(route in routePair.first.withIndex()){
			dispatchMultiIntent(route,intent)
		}
	}

	// I think this is recording the information before its first dispatch. Need to double check
	fun makeIntentRecords(intent: Intent) {
		var intentId = intent.getIntExtra(SEQUENCE_NUMBER, -1).toString()
		var activeMultiprocessesTable = loadTable(MULTIPROCESS_TABLE)

		// If the ID doesn't exist, then record it in the table. Why did I record check this?
		if(activeMultiprocessesTable.isNull(intentId)){

		}

		intentStorage.add(intent)
		var storageId = intentStorage.size

		var record = JSONDatabase.getJSONArray(intent.getStringExtra(MULTIPROCESS_ID))
		record.put(storageId)
		// I don't think I need to write anything, since this will be bound
		JSONDatabase.put(MULTIPROCESS_ID,record)
	}

	// What is the point of this?
	fun checkDuplicates(intent: Intent) {
		var sequenceNumber = intent.getIntExtra(SEQUENCE_NUMBER, -1).toString()
		var JSONMultiprocessRecord = loadTable(MULTIPROCESS_TABLE)
		if (JSONMultiprocessRecord.isNull(sequenceNumber)) {
			// Then it is unique. Save the data
			var JSONIntentRecord = JSONObject()
			// make the intent record
			JSONIntentRecord.put(DATA_KEYS, intent.getStringArrayListExtra(DATA_KEYS))
			Log.i(
					"MultiprocessService",
					"Getting DATA_KEYS from intent. Keys are ${intent.getStringArrayListExtra(DATA_KEYS)}"
			)
			for (dataKey in intent.getStringArrayListExtra(DATA_KEYS)!!) {
				Log.i(
						"MultiprocessService",
						"Logging key ${dataKey}, value ${intent.getStringArrayListExtra(dataKey)}"
				)
				//Bold to assume that it will only ever be data keys
				// I need to do something to account for duplicate names
				JSONIntentRecord.put(dataKey, intent.getStringArrayListExtra(dataKey))
			}
			// save the intent record with its sequence number as its unique ID
			var id = intent.getStringExtra(MULTIPROCESS_ID)
			// Should I change anything about how this is saved?
			JSONMultiprocessRecord.put(id + sequenceNumber, JSONIntentRecord)
			JSONDatabase.put(id, JSONMultiprocessRecord)
		}
	}

	// This dispatches ALL intents related to the multiprocess
	fun dispatchMultiIntent(route: IndexedValue<String>,intent: Intent){
		// I'd like to move this up a level, if I can
		makeIntentRecords(intent)
		var module = route.value.split(";")

		var outgoingIntent = Intent()
		// Dispatch it to one of the multiple locations listed
		outgoingIntent.setClassName(module.component1(),module.component2())
		outgoingIntent.putExtra(SEQUENCE_NUMBER, route.index)
		// Core should redirect it here. Am I overwriting? or appending
		outgoingIntent.putExtra(ROUTE,"${this.packageName};${this.javaClass.name}")
		startService(outgoingIntent)
	}

	// This is a specialized parser for MultiProcess Intent
	fun expandMultiprocessRoute(intent: Intent): Pair<List<String>,String>{
		var route = intent.getStringExtra(ROUTE)!!
		Log.v(this.javaClass.name,"routeParser received this for its route: ${route}")
		var start = route.indexOf("(")+1
		var end = route.indexOf(")",start)
		var multiprocessRouteString = route.substring(start,end)
		var multiprocessRoute = multiprocessRouteString.split(',')
		var remainingRoute = route.substring(end+1)
		// I don't think I want to leave this returning a pair
		return Pair(multiprocessRoute,remainingRoute)
	}

	fun sendFinalData(intent: Intent){
		var id = intent.getStringExtra(MULTIPROCESS_ID)
		Log.v(this.javaClass.name,"All data for MULTIPROCESS_ID ${id} received")
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
		// I don't know if this will work
		if(JSONDatabase.keys() == null) {
			// If there are no more multiprocess intents, shut down
			unbindService(connection)
			// could I clean this up a bit?
			SystemClock.sleep(500)
			stopSelf()
		}
	}

	// This could probably be simplified?
	fun getNewID(): String{
		var id = -1
		do{
			id = Random.nextInt().absoluteValue
		}while(id == -1)
		return id.toString()
	}

	fun loadDatabase(): JSONObject{
		// This seems... like it may not need to be formatted this way
		databaseFile = File(filesDir, MULTIPROCESS_TABLE)
		when(databaseFile.exists()) {
			true -> return JSONObject(databaseFile.readText())
			false -> return JSONObject()
		}
	}

	override fun onBind(intent: Intent?): IBinder? {
		TODO("Not yet implemented")
	}
}