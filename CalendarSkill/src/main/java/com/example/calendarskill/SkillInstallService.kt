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
    var parserFiles = listOf("date.entity","get.intent","alarm.intent")

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        // This should broadcast/ordered broadcast
        if(intent.action == ACTION_INSTALL_MODULE) {
            Log.i("SkillInstallService","Install intent received. Installing CalendarSkill")
            var installIntent = Intent()
            installIntent.setClassName(this,"com.example.sapphireassistantframework.CoreService")
            var keys = arrayListOf<String>()
            // Processor data may be a bad name, since this doesn't HAVE to send to processor
            var processorData = retrieveProcessorData()
            for(datum in processorData){
                keys.add(datum.key)
                installIntent.putExtra(datum.key,datum.value)
            }
            installIntent.putExtra("assistant.framework.DATA_KEYS",keys)
            startService(installIntent)
        // This will use a pre-prepared intent, to send it through the pipeline for the reuqesting module
        }else if(intent.action == ACTION_RETRIEVE_DATA){
            var processorIntent = getProcessorIntent()
            // This could be reeeeeal wonky. Keep an eye out on it
            processorIntent.putStringArrayListExtra("assistant.framework.process.DATA_FILENAMES",parserFiles as ArrayList<String>)

            var processorData = retrieveProcessorData()
            for(datum in processorData){
                processorIntent.putExtra(datum.key,datum.value)
            }
            startService(processorIntent)
        }
        return super.onStartCommand(intent, flags, startId)
    }

    fun retrieveProcessorData(): Map<String,ArrayList<String>> {
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
        var fileNames = parserFiles

        return fileNames
    }
}