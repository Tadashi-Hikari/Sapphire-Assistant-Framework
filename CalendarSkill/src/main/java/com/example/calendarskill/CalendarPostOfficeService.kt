package com.example.calendarskill

import android.app.PendingIntent
import android.content.Intent
import android.net.ParseException
import android.net.Uri
import android.os.IBinder
import android.widget.Toast
import com.example.componentframework.SapphireFrameworkRegistrationService
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream

class CalendarPostOfficeService: SapphireFrameworkRegistrationService(){
    val VERSION = "0.0.1"
    val CONFIG = "calendar.conf"
    val fileList = arrayListOf<String>("get.intent","set.intent")

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.i("Calendar intent received")
        // This is, already kind of a post office. I just need to move it from CalendarModuleInstallService to CalendarPostOffice
        when(intent?.action){
            ACTION_SAPPHIRE_MODULE_REGISTER -> registerModule(intent)
            ACTION_REQUEST_FILE_DATA -> sendFileNames(intent)
            ACTION_MANIPULATE_FILE_DATA -> coreTransferFile(intent)
        }
        return super.onStartCommand(intent, flags, startId)
    }

    // This sends a list of filenames that this module wishes to put in the core FileServer
    fun sendFileNames(intent: Intent){
        var filenameIntent = Intent(intent)
        // I believe this returns the proper from
        filenameIntent.putExtra(FROM,"${this.packageName};${this.javaClass.canonicalName}")
        Log.v(filenameIntent.getStringExtra(FROM)!!)
        filenameIntent.putExtra(DATA_KEYS,fileList)
        returnToCore(intent)
    }

    // This actually transfers the files
    fun coreTransferFile(intent: Intent){
        try{
            when(intent.hasExtra(DATA_KEYS)){
                true -> offloadFiles(intent)
                false -> Log.d("There was some kind of DATA_KEY error")
            }
        }catch(exception: Exception){
            Log.d("Exception. There was some kind of DATA_KEY error")
            exception.printStackTrace()
        }
    }

    // This seems like unneeded modularity. Also, it's messy
    fun offloadFiles(intent: Intent){
        Log.d("Requesting a file offload")
        Log.v("DATA_KEY for calendar is ${intent.getStringArrayListExtra(DATA_KEYS)}")
        var count = intent.getStringArrayListExtra(DATA_KEYS)!!.size

        if(intent.data != null) {
            Log.v("Writing data: ${intent.data}")
            writeToCore(intent.data!!)
        }

        if(intent.clipData != null) {
            Log.d("ClipData = ${intent.clipData}")
            var clipData = intent.clipData!!
            for (clipIndex in 0..clipData.itemCount-1) {
                Log.v("Writing clip: ${clipData.getItemAt(clipIndex)}")
                // This is how it has to be done w/ clipData it seems
                writeToCore(clipData.getItemAt(clipIndex).uri)
            }
        }

        intent.action = "FILE_TRANSFER_FINISHED"
        // This should not be hardcoded
        intent.setClassName("com.example.sapphireassistantframework","com.example.sapphireassistantframework.CoreService")
        Log.i("File transfer finished")
        // Does this need to bounce back to core, or multiprocess?
        startService(intent)
    }

    fun writeToCore(uri: Uri){
        try {
            Log.i(uri.toString()!!)
            //var testFile = uri.toFile()
            var somethingFD = contentResolver.openFileDescriptor(uri,"wa")!!
            var fd = somethingFD.fileDescriptor
            var outputStream = FileOutputStream(fd)
            outputStream.write("This text you are reading was appended to the file".toByteArray())
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

    // I think I can touch this up a lot
    override fun registerModule(intent: Intent){
        Log.i("Registering calendar skill")
        var returnIntent = Intent(intent)
        returnIntent = registerVersion(returnIntent, VERSION)
        // This is just the filenames the core keeps as a stub until requested for the first time
        registerData(returnIntent, fileList)
        super.registerModule(returnIntent)
    }

    // use Core to bridge content to a module
    fun p2pFile(): Uri?{
        var uri = null
        return uri
    }

    fun contentProvider(){
        // Implemented by developer
    }
}