package com.example.calendarskill

import android.content.Intent
import android.util.Log
import com.example.calendarskill.HelperFiles.InstallHelper

/**
 * This is Calendars install service. It needs to pass along essential information such as
 * any pipelines that it'd expect data to go down (as a default), and all the training data that is
 * needed for its respective parser service.
 *
 * Can I separate the preparation and training mechanism, so that its speech data can be tailored
 * to a different parser module?
 */

class SkillInstallService: InstallHelper(){
    // I don't like that this is hard installed
    var intentFiles = arrayListOf<String>("get.intent","set.intent")

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        // This should broadcast/ordered broadcast
        if(intent.action == ACTION_SAPPHIRE_MODULE_REGISTER) {
            Log.i("SkillInstallService","Register intent received. Installing CalendarSkill")
            var installIntent = Intent()
            // This should be in the sent intent. I can simplify this
            installIntent.setClassName("com.example.sapphireassistantframework","com.example.sapphireassistantframework.CoreService")
            installIntent.setAction(ACTION_SAPPHIRE_MODULE_REGISTER)
            startService(installIntent)
        // This will use a pre-prepared intent, to send it through the pipeline for the reuqesting module
        }else if(intent.action == ACTION_SAPPHIRE_MODULE_REQUEST_DATA){
            Log.i("SkillInstallService","Data request received. gathering data")
            // This is the same as outgoingIntent
            var dataRequestIntent = Intent(intent)
            dataRequestIntent.putStringArrayListExtra(DATA_KEYS,intentFiles)
            // This is poorly coded. It should retrieve the data per file, not in bulk
            var data = retrieveData()
            for(datum in data){
                Log.i("SkillInstallService","Inserting key: ${datum.key}, and value: ${datum.value}")
                dataRequestIntent.putExtra(datum.key,datum.value)
            }
            Log.i("SkillInstallService","Sending back the data")
            // I need to take this out of being hardcoded
            dataRequestIntent.setClassName(this,"com.example.multiprocessmodule.MultiprocessService")
            // Why did I hardcode this?
            dataRequestIntent.putExtra("core.conf.framework.multiprocess.protocol.SEQUENCE_NUMBER",2)
            Log.i("SkillInstallService","Keys being sent back are as follows: ${dataRequestIntent.getStringArrayListExtra(DATA_KEYS)!!}")
            startService(dataRequestIntent)
        }
        return super.onStartCommand(intent, flags, startId)
    }

    // This is where MultiprocessService is calling to. I should add the data keys
    fun retrieveData(): Map<String,ArrayList<String>> {
        var fileNames = loadFileNames()
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

    fun loadFileNames(): List<String>{
        // I need to get this info from an XML file, or some such thing
        var fileNames = intentFiles

        return fileNames
    }
}