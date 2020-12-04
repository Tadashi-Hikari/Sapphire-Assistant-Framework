package com.example.parsermodule

import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.util.Log
import edu.stanford.nlp.classify.ColumnDataClassifier
import edu.stanford.nlp.ie.crf.CRFClassifier
import edu.stanford.nlp.ling.CoreLabel
import edu.stanford.nlp.process.PTBTokenizer
import java.io.*
import java.lang.Exception
import java.util.*

class UtteranceProcessing: Service(){
    private lateinit var classifier: ColumnDataClassifier

    override fun onBind(intent: Intent?): IBinder? {
        TODO("Not yet implemented")
    }

    override fun onCreate() {
        super.onCreate()
        Log.i("UtteranceProcessing","Service created")
        trainIntentClassifier()
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        Log.i("UtteranceProcessing","intent received")
        if(intent.action == "PARSE"){
            Log.i("UtteranceProcessing","PARSE action received")
            if(intent.hasExtra("HYPOTHESIS")) {
                var utterance = intent.getStringExtra("HYPOTHESIS")
                if(utterance != null){
                    process(utterance)
                }
            }
        }

        return super.onStartCommand(intent, flags, startId)
    }

    fun process(text: String){
        if(text != "") {
            var temp = "none\t${text}"
            var datum = classifier.makeDatumFromLine(temp)
            var classified = classifier.classOf(datum)
            Log.i("CoreService", classified)
            var counter = classifier.scoresOf(datum)
            var score = counter.getCount(classified)
            Log.i("CoreService", "${counter.keySet()}")
            Log.i("CoreService", "${counter.values()}")
            if(classifier.scoresOf(datum).getCount(classified) > 1.0){
                Log.i("CoreService","${classified}, ${score}")
            }else{
                Log.i("CoreService", "Does not match a class")
            }
        }
    }

    // Uses a tab-delimited text
    fun trainIntentClassifier(){
        try{
            loadClassifier()
        }catch(exception: Exception){
            // I should probably make this a resource name
            var propFile = convertStreamToFile("test",".prop")
            var trainingFile = convertStreamToFile("training_data",".txt")
            Log.i("CoreService", "The conversion is done")

            //var classifier = ColumnDataClassifier(propFile.canonicalPath)
            var props = createProperties()
            classifier = ColumnDataClassifier(props)
            classifier.trainClassifier(trainingFile.canonicalPath)
            Log.i("CoreService", "The training is done")
        }
    }

    // This needs to be moved to creating a file
    fun createProperties(): Properties{
        var props = Properties()
        props.setProperty("goldAnswerColumn","0")
        props.setProperty("useNB","true")
        //props.setProperty("useClass","true")
        props.setProperty("useClassFeatures","true")
        //props.setProperty("1.splitWordsRegexp","false")
        //props.setProperty("1.splitWordsTokenizerRegexp","false")
        props.setProperty("1.splitWordsWithPTBTokenizer","true")
        // This is the line that was missing
        props.setProperty("1.useSplitWords","true")

        return props
    }

    // I hate that Android won't let me bundle files as files
    fun convertStreamToFile(prefix: String, suffix: String): File {
        // This file needs to be tab separated columns
        var asset = assets.open(prefix+suffix)
        var fileReader = InputStreamReader(asset)

        var tempFile = File.createTempFile(prefix, suffix)
        var tempFileWriter = FileOutputStream(tempFile)
        // This is ugly AF
        var data = fileReader.read()
        while (data != -1) {
            tempFileWriter.write(data)
            data = fileReader.read()
        }
        // Do a little clean up
        asset.close()
        tempFileWriter.close()

        return tempFile
    }

    fun saveClassifier(){
        val file = File(this.filesDir, "classifier")
        val objectOutputStream = ObjectOutputStream(file.outputStream())
        classifier.serializeClassifier(objectOutputStream)
    }

    fun loadClassifier(){
        val file = File(this.filesDir, "classifier")
        val objectInputStream = ObjectInputStream(file.inputStream())
        classifier = ColumnDataClassifier.getClassifier(objectInputStream)
    }
}