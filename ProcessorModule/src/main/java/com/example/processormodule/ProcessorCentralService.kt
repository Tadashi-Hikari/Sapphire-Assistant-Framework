package com.example.processormodule

import android.content.Intent
import android.os.IBinder
import com.example.componentframework.SapphireFrameworkService
import edu.stanford.nlp.classify.ColumnDataClassifier
import java.io.File

class ProcessorCentralService: SapphireFrameworkService(){

    override fun onBind(intent: Intent?): IBinder? {
        TODO("Not yet implemented")
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        try{
            when {
                // This is just a quick action for me, will be changed
                intent!!.action == "DELETE_CLASSIFIER" -> deleteClassifier()
                // This is just temporary
                else -> process(intent)
            }
            return super.onStartCommand(intent, flags, startId)
        }catch (exception: Exception){
            Log.d(CLASS_NAME,"There was an intent error w/ the processor")
           exception.printStackTrace()
        }
        return super.onStartCommand(intent, flags, startId)
    }

    // This should be renamed, definitely
    fun process(intent: Intent?){
        var utterance = intent!!.getStringExtra(MESSAGE)
        var outgoingIntent = Intent()

        try{
            if(utterance != ""){
                Log.i("ProcessorCentralService","Loading the classifier")
                var classifier = loadClassifier()
                // This is specific to how CoreNLP works
                var datumToClassify = classifier.makeDatumFromLine("none\t${utterance}")
                // Can these two be combined, or done at the same time?
                var classifiedDatum = classifier.classOf(datumToClassify)
                var classifiedScores = classifier.scoresOf(datumToClassify)
                Log.v("ProcessorCentralService","Datum classification: ${classifiedDatum}")
                // This is an arbitrary number, and should probably be a configurable variable
                if(classifiedScores.getCount(classifiedDatum) >= .04){
                    Log.i("ProcessorCentralService","Text matches class ${classifiedDatum}")
                    // This could be an issue with the new design
                    outgoingIntent.putExtra(ROUTE,classifiedDatum)
                }else {
                    Log.i("ProcessorCentralService","Text does not match a class. Using default")
                    // This could be an issue with the new design
                    outgoingIntent.putExtra(ROUTE,"DEFAULT")
                }

                /**
                 * I actually may not need to send out unformatted text. This filter is transforming it,
                 * so the next module probably doesn't need the unformatted text. I can just log a reference
                 * for text & binary sources, so that if a module needs it then a request can be made for
                 * the base data along the pipeline. This prevents overcomplicating the protocol
                 */
                outgoingIntent.putExtra(MESSAGE,utterance)
                startService(outgoingIntent)
            }
        }catch(exception: Exception){
            Log.e("ProcessorCentralService","There was an error trying to process the text")
        }
    }

    fun loadClassifier(): ColumnDataClassifier{
        var classifierFile = File(filesDir,"intent.classifier")
        if(classifierFile.exists() != true) {
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
        // I want the requested files to go to the training service. I am just injecting this in to the ROUTE. It's injected here...
        intent.putExtra("PROCESSOR_EXTRA","ProcessorTrainingService")
        intent.action = ACTION_REQUEST_FILE_DATA
        intent.putExtra(DATA_KEYS, requestedDataKeys)
        // This is where it is off to next, yeah?
        intent.putExtra(FROM,"${PACKAGE_NAME};${CANONICAL_CLASS_NAME}")
        Log.i(CLASS_NAME,"Requesting ${requestedDataKeys} files")
        startService(intent)
        // This is temporary
        stopSelf()
    }
}