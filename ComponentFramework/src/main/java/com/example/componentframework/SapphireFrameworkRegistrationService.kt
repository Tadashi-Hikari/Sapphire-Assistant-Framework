package com.example.componentframework

import android.app.PendingIntent
import android.content.Intent
import android.os.IBinder
import java.io.File
import java.io.FileOutputStream
import java.io.InputStreamReader

abstract class SapphireFrameworkRegistrationService: SapphireFrameworkService(){

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

	fun registerData(intent: Intent, filenames: ArrayList<String>): Intent{
		intent.putStringArrayListExtra(DATA_KEYS,filenames)
		return intent
	}

	fun returnToCore(intent: Intent){
		intent.setClassName(this,"com.example.sapphireassistantframework.CoreService")
		intent.removeExtra(FROM)
		startService(intent)
	}

	open fun registerModule(intent: Intent){
		// This needs to not be hardcoded. I can get the info from POSTAGE
		intent.setClassName("com.example.sapphireassistantframework","com.example.sapphireassistantframework.CoreService")
		intent.removeExtra(FROM)
		intent.fillIn(landingFunction(),0)
		intent.setAction(ACTION_SAPPHIRE_MODULE_REGISTER)
		startService(intent)
	}

	open fun passthrough(intent: Intent, className: String){
		// This allows certain information to be passed through
		var passthroughIntent = Intent(intent)
		passthroughIntent.setClassName(PACKAGE_NAME,"${PACKAGE_NAME}.${className}")
		startService(passthroughIntent)
	}

	// This is a temporary name/function. It is for converting to PendingIntent model
	fun landingFunction(): Intent{
		Log.d(CLASS_NAME,"Testing intent received")
		var outgoingIntent = Intent().setClassName("com.example.sapphireassistantframework","com.example.sapphireassistantframework.CoreService")
		var localIntent = Intent().setClassName(PACKAGE_NAME,CLASS_NAME)
		var pendingIntent = PendingIntent.getService(this,1,localIntent, 0)

		outgoingIntent.putExtra("PENDING",pendingIntent)
		Log.d(CLASS_NAME, "Sending PendingIntent")
		return outgoingIntent
	}

	// This is where MultiprocessService is calling to. I should add the data keys
	fun retrieveData(fileNames: List<String>): Map<String,ArrayList<String>> {
		// This is the data to go to the Processor. I think it's poorly named
		var processorData = mutableMapOf<String,ArrayList<String>>()
		//var something = convertFilesToSomething()

		for(fileName in fileNames){
			Log.i(CLASS_NAME,"Loading file: ${fileName}")
			// Processor cycles are cheap. Worry about optimization later
			var file = convertAssetToFile(fileName)

			var lines = ArrayList<String>()
			for(line in file.readLines()){
				// I account for the line termination in ProcessorTrainingService. I shouldn't cause, who knows what data will be sent
				lines.add("${line}")
			}
			Log.v(CLASS_NAME,"Added lines: ${lines}")
			processorData.put(fileName,lines)
		}
		return processorData
	}


	/*
	fun createConfigFile(filename: String){
		try {
			convertAssetToFile(filename)
			Log.v(this.javaClass.name, "${filename} created")
		}catch (excption: Exception){
			Log.v(this.javaClass.name, "Error creating configFile")
		}
	}
	*/

	/*
	fun expandInternalData(directory: String){
		var filenames = loadAssetNames(directory)
		for(filename in filenames){
			Log.v(this.javaClass.name,"Converting asset ${filename} to file...")
			convertAssetToFile(filename)
		}
		Log.v(this.javaClass.name, "All assets expanded")
	}
	*/

	// This is for getting asset filenames
	fun loadAssetNames(directory: String): Array<String>{
		// This gives a list of the root directory in assets
		var assetArray = emptyArray<String>()
		try {
			assetArray = assets.list(directory)!!
			for(assetFilename in assetArray){
				Log.v(this.javaClass.name, assetFilename)
			}
		}catch (exception: Exception){
			Log.d(this.javaClass.name, "Error loading assets")
		}
		return assetArray
	}

	/*
	fun exportConfigFile(filename: String): String {
		val intent = Intent(Intent.ACTION_OPEN_DOCUMENT_TREE)
		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
		startActivity(intent)
		return "halt"
	}
	 */
}