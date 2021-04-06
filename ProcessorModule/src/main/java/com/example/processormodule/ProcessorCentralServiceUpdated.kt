package com.example.processormodule

import android.content.Intent
import android.os.IBinder
import com.example.componentframework.SapphireFrameworkService
import edu.stanford.nlp.classify.ColumnDataClassifier
import java.io.File

class ProcessorCentralServiceUpdated: SapphireFrameworkService(){

    override fun onBind(intent: Intent?): IBinder? {
        TODO("Not yet implemented")
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when(intent?.action){
            ACTION_SAPPHIRE_TRAIN -> loadClassifier(intent)
            "DELETE_CLASSIFIER" -> deleteClassifier()
            // This should be changed
            ACTION_REQUEST_FILE_DATA -> trainClassifier(intent)
        }
        return super.onStartCommand(intent, flags, startId)
    }

    fun loadClassifier(intent: Intent?): ColumnDataClassifier{
        var classifierFile = File(filesDir,"intent.classifier")
        if(classifierFile.exists() != true){
            requestFiles()
        }
        return ColumnDataClassifier.getClassifier(classifierFile.canonicalPath)
    }

    fun deleteClassifier(){
        var file = File(filesDir,"Intent.classifier")
        file.delete()
    }

    // I might be able to move this to the SapphireFrameworkService class
    fun requestFiles(){
        var intent = Intent()
        var requestedDataKeys = arrayListOf<String>("intent")

        intent.setClassName("com.example.sapphireassistantframework","com.example.sapphireassistantframework.CoreService")
        // I want the requested files to go to the training service.
        intent.putExtra(ROUTE,"com.example.sapphireassistantframework;com.example.processormodule.ProcessorTrainingService")
        intent.action = ACTION_REQUEST_FILE_DATA
        intent.putExtra(DATA_KEYS, requestedDataKeys)
        intent.putExtra(FROM,"${PACKAGE_NAME};${CANONICAL_CLASS_NAME}")
        startService(intent)
    }
}