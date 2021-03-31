package com.example.processormodule

import android.content.Intent
import android.os.IBinder
import com.example.componentframework.SapphireFrameworkService

class ProcessorCentralServiceUpdated: SapphireFrameworkService(){

    override fun onBind(intent: Intent?): IBinder? {
        TODO("Not yet implemented")
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when(intent?.action){
            ACTION_SAPPHIRE_TRAIN -> loadClassifier(intent)
        }
        return super.onStartCommand(intent, flags, startId)
    }

    fun requestFile(){
        var fileList = arrayListOf<String>("get.intent","set.intent")

        var intent = Intent()
        intent.putExtra(DATA_KEYS,fileList)
        startService(intent)
    }

    // FileProvider can only send a single file?
    fun receiveFile(intent: Intent?){
    }

    fun loadClassifier(intent: Intent?){
    }

    inner class SapphireFile(){

    }
}