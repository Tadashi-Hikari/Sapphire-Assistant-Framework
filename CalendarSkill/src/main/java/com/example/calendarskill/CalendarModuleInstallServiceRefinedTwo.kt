package com.example.calendarskill

import android.content.Intent
import android.net.Uri
import com.example.componentframework.SapphireFrameworkRegistrationService
import java.io.FileOutputStream
import java.io.OutputStream
import java.net.URI

class CalendarModuleInstallServiceRefinedTwo: SapphireFrameworkRegistrationService(){
    val VERSION = "0.0.1"
    val CONFIG = "calendar.conf"
    val fileList = arrayListOf<String>("get.intent","set.intent")
    var REQUEST_FILE = "ACTION_SAPPHIRE_REQUEST_FILE"

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.i(this.javaClass.name,"Calendar intent received")
        when(intent?.action){
            ACTION_SAPPHIRE_MODULE_REGISTER -> registerModule(intent!!)
            //ACTION_SAPPHIRE_CORE_REQUEST_DATA -> sendRequestedFiles(intent)
            REQUEST_FILE -> demoRequestFile(intent)
        }
        return super.onStartCommand(intent, flags, startId)
    }

    fun demoRequestFile(intent: Intent?){
        // Bounce: ACTION_P2P_URI
        // Transfer to Core: ACTION_CORE_TRANSFER
        // File/Content Provider: ACTION_CUSTOM

    }

    // I can implement this easy
    fun coreTransferFile(){
        var fileStream: FileOutputStream? = null
        fileStream?.write(0)
    }

    // Same. Should they be moved to the back?
    fun p2pFile(): Uri?{
        var uri = null
        return uri
    }

    fun contentProvider(){
        // Implemented by developer
    }

    inner class SapphireFile(){

    }
}