package com.example.calendarskill

import android.content.Intent
import android.content.res.AssetManager
import android.util.Log
import com.example.calendarskill.HelperFiles.InstallHelper
import java.io.File

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
    var intentFiles = arrayListOf<String>("get.intent","alarm.intent")

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        // This should broadcast/ordered broadcast
        if(intent.action == ACTION_SAPPHIRE_MODULE_REGISTER) {
            Log.i("SkillInstallService","Register intent received. Installing CalendarSkill")
            var installIntent = Intent()
            // This should be in the sent intent. I can simplify this
            installIntent.setClassName("com.example.sapphireassistantframework","com.example.sapphireassistantframework.CoreService")
            installIntent.setAction(ACTION_SAPPHIRE_MODULE_REGISTER)
            //var keys = arrayListOf<String>()
            // Processor data may be a bad name, since this doesn't HAVE to send to processor
            //var processorData = retrieveData()
            //for(datum in processorData){
            //    keys.add(datum.key)
            //    installIntent.putExtra(datum.key,datum.value)
            //}
            //installIntent.putExtra("assistant.framework.DATA_KEYS",keys)
            startService(installIntent)
        // This will use a pre-prepared intent, to send it through the pipeline for the reuqesting module
        }else if(intent.action == ACTION_SAPPHIRE_MODULE_REQUEST_DATA){
            Log.i("SkillInstallService","Data request received. gathering data")
            // This is the same as outgoingIntent
            var dataRequestIntent = Intent(intent)
            // This could be reeeeeal wonky. Keep an eye out on it
            for(key in intent.getStringArrayListExtra(DATA_KEYS)!!){
                if(key == "intent") {
                    // This adds the file name as the key. i don't like how confusing the multi use is. Maybe just add some more protocol?
                    dataRequestIntent.putStringArrayListExtra(DATA_KEYS, intentFiles as ArrayList<String>)
                    var data = retrieveData()
                    for (datum in data) {
                        dataRequestIntent.putExtra(datum.key, datum.value)
                    }
                }
            }
            Log.i("SkillInstallService","Sending back the data")
            startService(dataRequestIntent)
        }
        return super.onStartCommand(intent, flags, startId)
    }

    fun retrieveData(): Map<String,ArrayList<String>> {
        var fileNames = loadFileNames()
        var processorData = mutableMapOf<String,ArrayList<String>>()
        //var something = convertFilesToSomething()

        for(fileName in fileNames){
            Log.i("SkillInstallService(Calendar)","Loading file: ${fileName}")
            // Processor cycles are cheap. Worry about optimization later
            var file = convertStreamToFile(fileName)

            var lines = ArrayList<String>()
            for(line in file.readLines()){
                lines.add(line)
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