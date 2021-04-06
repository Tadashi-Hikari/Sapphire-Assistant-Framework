package com.example.calendarskill.HelperFiles

import android.content.Intent
import android.os.IBinder
import com.example.componentframework.depreciated.SAFService

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