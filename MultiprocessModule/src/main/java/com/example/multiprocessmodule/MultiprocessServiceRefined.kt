package com.example.multiprocessmodule

import android.content.Intent
import android.os.IBinder
import com.example.componentframework.SapphireFrameworkService
import org.json.JSONObject
import java.io.File

class MultiprocessServiceRefined: SapphireFrameworkService(){
	val MULTIPROCESS_ID = "assistant.framework.multiprocess.protocol.ID"
	val SEQUENCE_NUMBER = "assistant.framework.multiprocess.protocol.SEQUENCE_NUMBER"
	val SEQUENCE_TOTAL = "assistant.framework.multiprocess.column.SEQUENCE_TOTAL"

	val MULTIPROCESS_TABLE = "multiprocess.tbl"

	lateinit var databaseFile: File
	lateinit var JSONDatabase: JSONObject

	override fun onCreate(){
		super.onCreate()
	}

	override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
		when{
			intent == null -> Log.d(this.javaClass.name, "There was an intent error. Stopping Multiprocess Module...")
			intent.hasExtra(MULTIPROCESS_ID) -> evaluateMultiprocessIntent(intent)
			else -> intializeMultiprocessIntent(intent)
		}
		return super.onStartCommand(intent, flags, startId)
	}

	fun evaluateMultiprocessIntent(intent: Intent){
	}

	fun intializeMultiprocessIntent(intent: Intent?){

	}

	override fun onBind(intent: Intent?): IBinder? {
		TODO("Not yet implemented")
	}
}