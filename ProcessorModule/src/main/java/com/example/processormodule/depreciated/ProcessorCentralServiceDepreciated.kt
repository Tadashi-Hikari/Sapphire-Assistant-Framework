package com.example.processormodule

import android.content.Intent
import android.os.IBinder
import com.example.componentframework.depreciated.SAFService
import edu.stanford.nlp.classify.ColumnDataClassifier
import java.io.File

class ProcessorCentralService: SAFService(){

    override fun onBind(intent: Intent?): IBinder? {
        TODO("Not yet implemented")
    }

    fun deleteClassifier(){
        var file = File(filesDir,"Intent.classifier")
        file.delete()
    }

    override fun onStartCommand(startIntent: Intent?, flags: Int, startId: Int): Int {
        try {
            var intent = startIntent!!
            Log.i("ProcessorCentralService", "Data processing intent received")
            if (intent.action == ACTION_SAPPHIRE_TRAIN) {
                intent.setClassName(this, "com.example.processormodule.ProcessorTrainingService")
                // Send it to the training service
                startService(intent)
            // This is a temporary hack
            }else if(intent.action == "DELETE_CLASSIFIER"){
                deleteClassifier()
            } else if (intent.hasExtra(MESSAGE)) {
                // Default to the purpose of the processor
                var text = intent.getStringExtra(MESSAGE)!!
                process(text)
            }
        }catch(exception: Exception){
            Log.e("ProcessorCentralService","Something went wrong receiving the intent")
        }

        return super.onStartCommand(startIntent, flags, startId)
    }

    fun process(text: String){
        var outgoingIntent = Intent()

        try{
            if(text != ""){
                Log.i("ProcessorCentralService","Loading the classifier")
                var classifier = loadClassifier()
                // This is specific to how CoreNLP works
                var datumToClassify = classifier.makeDatumFromLine("none\t${text}")
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
                outgoingIntent.putExtra(MESSAGE,text)
                startService(outgoingIntent)
            }
        }catch(exception: Exception){
            Log.e("ProcessorCentralService","There was an error trying to process the text")
        }
    }

    fun loadClassifier(): ColumnDataClassifier{
        var classifierFile = File(this.filesDir,"Intent.classifier")
        // I can't spontaneously train without requesting the data. Can this be overcome?
        if(classifierFile.exists() != true) {
            // This is the data type being requested by
            var requestedDataKeys = arrayListOf<String>("intent")

            Log.e("ProcessorCentralService","There is no saved classifier. Requesting training data from core")
            // I don't like how bulky this is
            //var core = getDefault(CORE).split(";")
            // var coreDataRequestIntent = Intent().setClassName(core.first(),core.second()
            var coreDataRequestIntent = Intent().setClassName(this,"com.example.sapphireassistantframework.CoreService")
            coreDataRequestIntent.action = ACTION_SAPPHIRE_CORE_REQUEST_DATA
            // coreDataRequestIntent.putExtra(ROUTE,"${this.packageName};com.example.processormodule.${this.javaClass.name}")
            coreDataRequestIntent.putExtra(ROUTE,"com.example.sapphireassistantframework;com.example.processormodule.ProcessorCentralService")
            coreDataRequestIntent.putStringArrayListExtra(DATA_KEYS,requestedDataKeys)
            Log.i("ProcessorCentralService","Requesting data keys ${coreDataRequestIntent.getStringArrayListExtra(DATA_KEYS)}" )
            startService(coreDataRequestIntent)
        }

        return ColumnDataClassifier.getClassifier(classifierFile.canonicalPath)
    }
}