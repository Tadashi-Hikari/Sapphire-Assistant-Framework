package com.example.processormodule

import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.util.Log
import edu.stanford.nlp.ie.crf.CRFClassifier
import edu.stanford.nlp.ling.CoreLabel
import org.json.JSONObject
import java.io.*
import java.util.*

class EntityProcessing: Service(){

    // All of the slowness of this takes place in loading the classifier, so strive to only do it once
    private lateinit var classifier: CRFClassifier<CoreLabel>

    override fun onBind(intent: Intent?): IBinder? {
        TODO("Not yet implemented")
    }

    override fun onCreate() {
        super.onCreate()

        // I guess it doesn't need the .gz file suffix?
        var serializedClassifierName = "english.all.3class.distsim.crf.ser"
        var inputStream = assets.open(serializedClassifierName)
        var objectStream = ObjectInputStream(inputStream)
        classifier = CRFClassifier.getClassifier<CoreLabel>(objectStream)
        Log.i("EntityProcessing","Classifier loaded")
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        if(intent.action == "ENTITY"){
            Log.i("EntityProcessing","NER intent received")
            var utterance = intent.getStringExtra("HYPOTHESIS")
            if(utterance != null) {
                processEntities(utterance)
            }
        }
        return super.onStartCommand(intent, flags, startId)
    }

    fun processEntities(utterance: String){
        var json = JSONObject(utterance)
        var text = json.getString("text")
        var result = classifier.classifyToString(text)
        Log.i("EntityProcessing",result)
    }

    fun trainNERClassifier(){
        Log.i("EntityProcessor","Not yet implemented")
    }

    fun getProperties(): Properties {
        var properties = Properties()
        properties.setProperty("useNext","true")
        properties.setProperty("useLast","true")
        properties.setProperty("usePosition","true")


        return properties
    }
}