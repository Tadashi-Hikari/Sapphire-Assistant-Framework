package com.example.calendarskill

import android.content.ContentProvider
import android.content.Intent
import android.os.Binder
import android.os.IBinder
import android.provider.OpenableColumns
import android.renderscript.ScriptGroup
import androidx.core.content.FileProvider
import com.example.componentframework.SapphireFrameworkRegistrationService
import com.example.componentframework.SapphireFrameworkService
import java.io.File
import java.io.InputStream
import java.io.OutputStream

class CalendarModuleInstallServiceRefined: SapphireFrameworkRegistrationService(){
	val VERSION = "0.0.1"
	val CONFIG = "calendar.conf"
	val fileList = arrayListOf<String>("get.intent","set.intent")
	var REQUEST_FILE = "ACTION_SAPPHIRE_REQUEST_FILE"

	override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
		Log.i(this.javaClass.name,"Calendar intent received")
		when(intent?.action){
			ACTION_SAPPHIRE_MODULE_REGISTER -> registerModule(intent!!)
			ACTION_SAPPHIRE_CORE_REQUEST_DATA -> sendRequestedFiles(intent)
			REQUEST_FILE -> demoRequestFile(intent)
		}
		return super.onStartCommand(intent, flags, startId)
	}

	fun demoRequestFile(intent: Intent){
		try {
			var uri = intent.data!!
			Log.i(this.javaClass.name,uri.path!!)
			var testFile = File(uri.path)
			Log.i(this.javaClass.name, "This seems like a vaild way to get the file")
		}catch (exception: Exception){
			Log.d(this.javaClass.name, "Looks like you can't get a file this way")
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

		Log.d(this.javaClass.name,filepath)
		var file = File(filepath)
		Log.d(this.javaClass.name,"It looks like the file was made: ${file.name}")

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
		Log.v(this.javaClass.name,"It appears, you actually can start me!")
		var temp = Intent(intent)
		demoRequestFile(temp)
		return Binder()
	}
}