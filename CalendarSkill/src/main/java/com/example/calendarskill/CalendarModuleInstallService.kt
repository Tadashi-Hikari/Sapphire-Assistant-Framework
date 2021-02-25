package com.example.calendarskill

import android.content.Intent
import android.util.Log
import com.example.calendarskill.HelperFiles.InstallHelper
import com.example.componentframework.SAFInstallService

/**
 * This is Calendars install service. It needs to pass along essential information such as
 * any pipelines that it'd expect data to go down (as a default), and all the training data that is
 * needed for its respective parser service.
 *
 * Can I separate the preparation and training mechanism, so that its speech data can be tailored
 * to a different parser module?
 */

class CalendarModuleInstallService: SAFInstallService(){
    val VERSION = "0.0.1"

    // I don't like that this is hard installed
    var intentFiles = arrayListOf<String>("get.intent","set.intent")

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        if(intent.action == ACTION_SAPPHIRE_MODULE_REGISTER) {
            Log.i("SkillInstallService","Register intent received. Installing CalendarSkill")
            registerModule(intent)
        // This will use a pre-prepared intent, to send it through the pipeline for the reuqesting module
        }else if(intent.action == ACTION_SAPPHIRE_MODULE_REQUEST_DATA){
            requestData(intent)
        }
        return super.onStartCommand(intent, flags, startId)
    }

    fun requestData(intent: Intent){
        // This is filler for now, but should replace the hardcoding
        loadAssetNames()
        Log.i("SkillInstallService","Data request received. gathering data")
        // This is the same as outgoingIntent
        var dataRequestIntent = Intent(intent)
        dataRequestIntent.putStringArrayListExtra(DATA_KEYS,intentFiles)
        // This is poorly coded. It should retrieve the data per file, not in bulk
        var data = retrieveData(intentFiles)
        for(datum in data){
            dataRequestIntent.putExtra(datum.key,datum.value)
        }
        // I need to take this out of being hardcoded
        dataRequestIntent.setClassName(this,"${this.packageName}.MultiprocessService")
        // Why did I hardcode this? What does it mean. I think I was testing out MultiprocessIntent, which means there could be a bug here....
        dataRequestIntent.putExtra("core.conf.framework.multiprocess.protocol.SEQUENCE_NUMBER",2)
        startService(dataRequestIntent)
    }

    // I think I can touch this up a lot
    override fun registerModule(intent: Intent){
        var returnIntent = Intent(intent)
        returnIntent.putExtra(MODULE_PACKAGE,this.packageName)
        returnIntent.putExtra(MODULE_CLASS,"${this.packageName}.CalendarService")
        registerVersion(returnIntent, VERSION)
        registerData(returnIntent, intentFiles)

        super.registerModule(returnIntent)
    }
}