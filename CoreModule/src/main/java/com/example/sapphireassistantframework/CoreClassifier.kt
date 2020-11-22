package com.example.sapphireassistantframework

import android.app.IntentService
import android.app.Service
import android.content.Intent
import android.os.IBinder
import edu.stanford.nlp.classify.ColumnDataClassifier
import edu.stanford.nlp.classify.LinearClassifier
import edu.stanford.nlp.classify.LinearClassifierFactory
import java.io.*
import java.util.*

// The methods here are currently copied in CoreService directly

class CoreClassifier: Service(){
    var classifier = train()

    override fun onBind(intent: Intent?): IBinder? {
        TODO("Not yet implemented")
    }

    // L is type of labels, F is features
    var typeOfLabels = "Work"
    var features = "Features"

    // Uses a tab-delimited text
    fun train(): ColumnDataClassifier{
        // I should probably make this a resource name
        var trainingFile = convertStreamToFile("training_data.txt")
        var classifier = ColumnDataClassifier(trainingFile.canonicalPath)

        return classifier
    }

    fun classify(utterance: String){
        var utteranceDatum = classifier.makeDatumFromLine(utterance)
        print(classifier.scoresOf(utteranceDatum))
    }

    // I hate that Android won't let me bundle files as files
    fun convertStreamToFile(filename: String): File{
        // This file needs to be tab separated columns
        var asset = assets.open(filename)

        var tempFile = createTempFile("training_data","txt")
        var tempFileWriter = FileOutputStream(tempFile)

        // This is ugly AF
        var data = asset.read()
        while(data != null){
            tempFileWriter.write(data)
            data = asset.read()
        }
        // Do a little clean up
        asset.close()
        tempFileWriter.close()

        return tempFile
    }
}