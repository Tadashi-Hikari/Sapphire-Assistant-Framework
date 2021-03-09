package com.example.componentframework

import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.util.Log
import org.json.JSONArray
import java.io.File

abstract class SAFInstallService: SAFService(){

	// I'm either sending JSON or DSV
	fun registerBackgroundServices(intent: Intent, backgroundData: String): Intent{
		intent.putExtra("BACKGROUND",backgroundData)
		return intent
	}

	// I am thinknig this should actually be a JSONObject
	fun registerRouteInformation(intent: Intent, route: String): Intent{
		intent.putExtra(ROUTE,route)
		return intent
	}

	fun registerVersion(intent: Intent, version: String): Intent{
		intent.putExtra(MODULE_VERSION,version)
		return intent
	}

	fun registerModuleType(intent: Intent, component: String): Intent{
		intent.putExtra(MODULE_TYPE, component)
		return intent
	}

	fun version(intent: Intent, version: String): Intent{
		intent.putExtra(MODULE_VERSION,version)
		return intent
	}

	// This is called for retrieveData as well
	// This one WILL save time
	fun registerData(intent: Intent, filenames: List<String>): Intent{
		return intent
	}

	open fun registerModule(intent: Intent){
		// This needs to not be hardcoded. I can get the info from POSTAGE
		intent.setClassName(this,"com.example.sapphireassistantframework.CoreService")
		intent.setAction(ACTION_SAPPHIRE_MODULE_REGISTER)
		Log.d(this.javaClass.name, "Intent: ${intent.extras.toString()}")
		startService(intent)
	}

	// This is where MultiprocessService is calling to. I should add the data keys
	fun retrieveData(fileNames: List<String>): Map<String,ArrayList<String>> {
		// This is the data to go to the Processor. I think it's poorly named
		var processorData = mutableMapOf<String,ArrayList<String>>()
		//var something = convertFilesToSomething()

		for(fileName in fileNames){
			Log.i("SkillInstallService(Calendar)","Loading file: ${fileName}")
			// Processor cycles are cheap. Worry about optimization later
			var file = convertStreamToFile(fileName)

			var lines = ArrayList<String>()
			for(line in file.readLines()){
				// I account for the line termination in ProcessorTrainingService. I shouldn't cause, who knows what data will be sent
				lines.add("${line}")
			}
			Log.v("SkillInstallService(Calendar)","Added lines: ${lines}")
			processorData.put(fileName,lines)
		}
		return processorData
	}

	// This is for getting asset filenames
	// It is throwing a major error
	fun loadAssetNames(): List<File>{
		// This gives a list of the root directory in assets
		var assetsList = assets.list("")
		return assetsList as List<File>
	}

	override fun onBind(intent: Intent?): IBinder? {
		TODO("Not yet implemented")
	}
}