package com.example.multiprocessmodule

import android.content.Intent
import android.os.IBinder
import com.example.componentframework.SapphireFrameworkService
import org.json.JSONObject
import java.io.File
import kotlin.math.absoluteValue
import kotlin.random.Random

/*
This also can be used to wait for a response for a single intent. Perhaps it should have a different name
 */

class MultiprocessServiceRefined: SapphireFrameworkService(){
	val MULTIPROCESS_ID = "assistant.framework.multiprocess.protocol.ID"
	val SEQUENCE_NUMBER = "assistant.framework.multiprocess.protocol.SEQUENCE_NUMBER"
	val SEQUENCE_TOTAL = "assistant.framework.multiprocess.column.SEQUENCE_TOTAL"

	val MULTIPROCESS_TABLE = "multiprocess.tbl"

	lateinit var databaseFile: File
	lateinit var JSONDatabase: JSONObject

	override fun onCreate(){
		// This should bind while waiting for a response?
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
		var multiprocessRoute = expandMultiprocessRoute(intent)

		intent.putExtra(MULTIPROCESS_ID, id)
		// This is just for easy recording. The Sequence number gets overwritten by each local intent
		intent.putExtra(SEQUENCE_NUMBER,multiprocessRoute.size)

		for(route in multiprocessRoute.withIndex()){
			dispatchMultiIntent(route,intent)
		}
	}

	fun recordProcessInfo(intent: Intent){

	}

	fun dispatchMultiIntent(route: IndexedValue<String>,intent: Intent){
		// I'd like to move this up a level, if I can
		recordProcessInfo(intent)
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
	fun expandMultiprocessRoute(intent: Intent): List<String>{

	}

	fun sendFinalData(intent: Intent){

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