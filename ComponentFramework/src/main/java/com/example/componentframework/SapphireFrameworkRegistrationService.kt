package com.example.componentframework

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

	// This is called for retrieveData as well
	// This one WILL save time
	fun registerData(intent: Intent, filenames: List<String>): Intent{
		return intent
	}

	open fun registerModule(intent: Intent){
		// This needs to not be hardcoded. I can get the info from POSTAGE
		intent.setClassName(this,"com.example.sapphireassistantframework.CoreService")
		intent.removeExtra(FROM)
		intent.setAction(ACTION_SAPPHIRE_MODULE_REGISTER)
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


	fun createConfigFile(filename: String){
		try {
			convertAssetToFile(filename)
			Log.v(this.javaClass.name, "${filename} created")
		}catch (excption: Exception){
			Log.v(this.javaClass.name, "Error creating configFile")
		}
	}

	fun expandInternalData(directory: String){
		var filenames = loadAssetNames(directory)
		for(filename in filenames){
			Log.v(this.javaClass.name,"Converting asset ${filename} to file...")
			convertAssetToFile(filename)
		}
		Log.v(this.javaClass.name, "All assets expanded")
	}

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

	fun convertAssetToFile(filename: String){
		// This file needs to be tab separated columns
		var asset = assets.open(filename)
		var fileReader = InputStreamReader(asset)

		var file = File(filesDir,filename)
		var fileWriter = FileOutputStream(file)
		// This is ugly AF
		var data = fileReader.read()
		while (data != -1) {
			fileWriter.write(data)
			data = fileReader.read()
		}
		// Do a little clean up
		asset.close()
		fileWriter.close()
	}

	fun exportConfigFile(filename: String): String {
		val intent = Intent(Intent.ACTION_OPEN_DOCUMENT_TREE)
		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
		startActivity(intent)
		return "halt"
	}

	override fun onBind(intent: Intent?): IBinder? {
		TODO("Not yet implemented")
	}
}