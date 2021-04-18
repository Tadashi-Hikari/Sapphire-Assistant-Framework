package com.example.calendarskill

import android.content.Intent
import android.net.Uri
import android.os.IBinder
import com.example.componentframework.SapphireFrameworkRegistrationService
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream

class CalendarModuleInstallService: SapphireFrameworkRegistrationService(){
    val VERSION = "0.0.1"
    val CONFIG = "calendar.conf"
    val fileList = arrayListOf<String>("get.intent","set.intent")

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.i(this.javaClass.name,"Calendar intent received")
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
        Log.v(this.javaClass.name,filenameIntent.getStringExtra(FROM)!!)
        filenameIntent.putExtra(DATA_KEYS,fileList)
        returnToCore(intent)
    }

    // This actually transfers the files
    fun coreTransferFile(intent: Intent){
        try{
            when(intent.hasExtra(DATA_KEYS)){
                true -> offloadFiles(intent)
                false -> Log.d(this.javaClass.name, "There was some kind of DATA_KEY error")
            }
        }catch(exception: Exception){
            Log.d(this.javaClass.name, "Exception. There was some kind of DATA_KEY error")
            Log.d(this.javaClass.name, exception.toString())
        }
    }

    // This seems like unneeded modularity. Also, it's messy
    fun offloadFiles(intent: Intent){
        Log.d(CLASS_NAME,"Requesting a file offload")
        if(intent.data != null) {
            writeToCore(intent.data!!)
        }

        if(intent.clipData != null) {
            var clipData = intent.clipData!!
            for (clipIndex in 0..clipData.itemCount) {
                // This is how it has to be done w/ clipData it seems
                writeToCore(clipData.getItemAt(clipIndex).uri)
            }
        }

        var finishedIntent = Intent()
        finishedIntent.action = "FILE_TRANSFER_FINISHED"
        // This should not be hardcoded
        intent.setClassName("com.example.sapphireassistantframework","com.example.sapphireassistantframework.CoreService")
        startService(finishedIntent)
    }

    fun writeToCore(uri: Uri){
        try {
            Log.i(this.javaClass.name,uri.toString()!!)
            //var testFile = uri.toFile()
            var somethingFD = contentResolver.openFileDescriptor(uri,"wa")!!
            var fd = somethingFD.fileDescriptor
            var outputStream = FileOutputStream(fd)
            outputStream.write(". This is appended".toByteArray())
            Log.i(this.javaClass.name,"Did it write?")

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

            Log.i(this.javaClass.name, testFile.readText())

            Log.i(this.javaClass.name, "This seems like a valid way to edit the file")
        }catch (exception: Exception){
            Log.d(this.javaClass.name, "You cannot access the file this way")
            Log.i(this.javaClass.name, exception.toString())
        }
    }

    // I think I can touch this up a lot
    override fun registerModule(intent: Intent){
        Log.i(CLASS_NAME,"Registering calendar skill")
        var returnIntent = Intent(intent)
        returnIntent.putExtra(MODULE_PACKAGE,this.packageName)
        returnIntent.putExtra(MODULE_CLASS,"com.example.calendarskill.CalendarService")
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