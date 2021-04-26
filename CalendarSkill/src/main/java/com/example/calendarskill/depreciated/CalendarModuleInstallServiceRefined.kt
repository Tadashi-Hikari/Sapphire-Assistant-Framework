package com.example.calendarskill.depreciated

import android.content.Intent
import android.os.Binder
import android.os.IBinder
import com.example.componentframework.SapphireFrameworkRegistrationService
import java.io.*

class CalendarModuleInstallServiceRefined: SapphireFrameworkRegistrationService(){
	val VERSION = "0.0.1"
	val CONFIG = "calendar.conf"
	val fileList = arrayListOf<String>("get.intent","set.intent")
	var REQUEST_FILE = "ACTION_SAPPHIRE_REQUEST_FILE"

	override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
		Log.i("Calendar intent received")
		when(intent?.action){
			ACTION_SAPPHIRE_MODULE_REGISTER -> registerModule(intent!!)
			ACTION_SAPPHIRE_CORE_REQUEST_DATA -> sendRequestedFiles(intent)
			REQUEST_FILE -> demoRequestFile(intent)
		}
		return super.onStartCommand(intent, flags, startId)
	}

	fun demoRequestFile(intent: Intent){
		var uri = intent.data!!
		try {
			var uri = intent.data!!
			Log.i(uri.toString()!!)
			//var testFile = uri.toFile()
			var somethingFD = contentResolver.openFileDescriptor(uri,"wa")!!
			var fd = somethingFD.fileDescriptor
			var outputStream = FileOutputStream(fd)
			outputStream.write(". This is appended".toByteArray())
			Log.i("Did it write?")

			// This is the essential part, when it comes to editing a file
			somethingFD = contentResolver.openFileDescriptor(uri,"rw")!!
			fd = somethingFD.fileDescriptor
			var inputStream = FileInputStream(fd)

			var testFile = File(cacheDir,"temp")
			var fileWriter = testFile.outputStream()

			var data = inputStream!!.read()
			while(data != -1){
				fileWriter.write(data)
				data = inputStream.read()
			}
			fileWriter.close()

			Log.i(testFile.readText())

			Log.i("This seems like a valid way to edit the file")
		}catch (exception: Exception){
			Log.d("You cannot access the file this way")
			Log.i(exception.toString())
		}
	}

	// This is going to have to get moved to FrameworkRegistrationService
	fun sendFile(outputStream: OutputStream,filename: String){
		lateinit var localFileInputStream: InputStream
		try {
			localFileInputStream = File(filesDir, filename).inputStream()
		}catch(exception: Exception){
			localFileInputStream = assets.open(filename)
		}

		// Write the data to the FileProvider
		var fileData = localFileInputStream.read()
		while(fileData != -1){
			outputStream.write(fileData)
			fileData = localFileInputStream.read()
		}
	}

	fun sendRequestedFiles(intent: Intent){
		var returnIntent = Intent()

		var filenames = intent.getStringArrayExtra(DATA_KEYS)!!
		var contentUri = intent.data!!
		var filepath = contentUri.path!!

		Log.d(filepath)
		var file = File(filepath)
		Log.d("It looks like the file was made: ${file.name}")

		for(filename in filenames){
			sendFile(contentResolver.openOutputStream(contentUri)!!,filename)
		}
	}

	fun fileTransferFinished(intent: Intent){
		// This would actually need to return to Core, before multiprocess service
		intent.action = null
		dispatchSapphireServiceToCore(intent)
	}

	// I think I can touch this up a lot
	override fun registerModule(intent: Intent){
		var returnIntent = Intent(intent)
		returnIntent.putExtra(MODULE_PACKAGE,this.packageName)
		returnIntent.putExtra(MODULE_CLASS,"com.example.calendarskill.CalendarService")
		returnIntent = registerVersion(returnIntent, VERSION)
		// This is just the filenames the core keeps as a stub until requested for the first time
		registerData(returnIntent, fileList)

		super.registerModule(returnIntent)
	}



	override fun onBind(intent: Intent?): IBinder? {
		Log.v("onBind can start the service, but it can't access Uris. Accessing in onStartCommand()")
		return Binder()
	}
}