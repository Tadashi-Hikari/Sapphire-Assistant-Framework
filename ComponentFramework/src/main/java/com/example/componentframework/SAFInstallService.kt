package com.example.componentframework

import android.content.Intent
import android.os.IBinder
import android.util.Log
import org.json.JSONArray
import java.io.File

abstract class SAFInstallService: SAFService(){

	/**
	 * Do these need to be their own modules? I don't think it's saving any time,
	 * that said, it could be saving complexity since the dev doesn't need to know
	 * extra strings to populate it
	 */

	fun registerRouteInformation(intent: Intent, route: String): Intent{
		return intent
	}

	// I'm either sending JSON or DSV
	fun registerBackgroundServices(intent: Intent, route: String): Intent{
		return intent
	}

	fun registerVersion(intent: Intent, version: String): Intent{
		intent.putExtra(MODULE_VERSION,version)
		return intent
	}

	fun registerPackageName(intent: Intent, packageName: String): Intent{
		intent.putExtra(MODULE_PACKAGE,packageName)
		return intent
	}

	fun registerSettings(intent: Intent, component: String): Intent{
		intent.putExtra(MODULE_TYPE, component)
		return intent
	}

	// This is called for retrieveData as well
	// This one WILL save time
	fun registerData(intent: Intent, filenames: List<String>): Intent {
		return intent
	}

	fun registerModule(intent: Intent){
		// This needs to not be hardcoded. I can get the info from POSTAGE
		intent.setClassName(this,"com.example.sapphireassistantframework.CoreService")
		intent.setAction(ACTION_SAPPHIRE_MODULE_REGISTER)
		startService(intent)
	}

	fun version(){
		var intent = Intent().setClassName(this,"com.example.sapphireassistantframework.CoreService")
		intent.setAction(ACTION_SAPPHIRE_MODULE_VERSION)
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
	fun loadAssetNames(): List<File>{
		// This gives a list of the root directory in assets
		var assetsList = assets.list("")
		return assetsList as List<File>
	}

	override fun onBind(intent: Intent?): IBinder? {
		TODO("Not yet implemented")
	}
}