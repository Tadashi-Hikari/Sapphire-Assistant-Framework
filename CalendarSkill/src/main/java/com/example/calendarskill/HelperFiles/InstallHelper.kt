package com.example.calendarskill.HelperFiles

import android.app.Service
import android.content.Intent
import android.net.Uri
import android.os.IBinder
import android.util.Log
import androidx.core.content.FileProvider
import com.example.componentframework.SAFService
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.io.InputStreamReader

abstract class InstallHelper: SAFService(){

    fun getSkillFiles(){
        var something = assets.list("")
        for(filename in something!!){
            Log.v("InstallHelper",filename)
        }
    }

    fun getProcessorIntent():Intent{
        var processorIntent = Intent()
        processorIntent.setAction(ACTION_SAPPHIRE_MODULE_REGISTER)
        processorIntent.setClassName(this,"com.example.sapphireassistantframework.CoreService")

        return processorIntent
    }

    override fun onBind(intent: Intent?): IBinder? {
        TODO("Not yet implemented")
    }
}