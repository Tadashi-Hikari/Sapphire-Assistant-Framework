package com.example.processormodule

import android.content.Intent
import android.os.IBinder
import android.util.Log
import com.example.componentframework.SAFService
import edu.stanford.nlp.ie.crf.CRFClassifier
import edu.stanford.nlp.ling.CoreLabel
import edu.stanford.nlp.ling.tokensregex.SequenceMatchRules
import edu.stanford.nlp.sequences.DocumentReaderAndWriter
import edu.stanford.nlp.util.ArrayCoreMap
import edu.stanford.nlp.util.CoreMap
import org.json.JSONObject
import java.util.*

// Padatious known entities = Stanford RegexNER
// Padatious wildcard entities = CRFClassifier
// --- See: https://stanfordnlp.github.io/CoreNLP/ner.html
/**
 * The purpose here is to train
 */

class EntityTrainingService: SAFService(){
	lateinit var classifier: CRFClassifier<CoreLabel>
	var trainingFile = "Not yet implemented"

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
		Log.i("EntityTrainingService",result)
	}

	fun trainNERClassifier(){
		var properties = knownEntityProperties()
		// It is adding a label to each thing, I think... This is based on how
		// I'd load a pre-trained model
		var classifier = CRFClassifier<CoreLabel>(properties)
		classifier.train(trainingFile) //.canonicalPath)

	}

	fun wildcardProperties(): Properties{
		var properties = Properties()

		properties.setProperty("useNext","true")
		properties.setProperty("useLast","true")
		properties.setProperty("usePosition","true")

		return properties
	}

	// This may want to incorporate wildcard properties, for mixtures of know entities
	fun knownEntityProperties(): Properties{
		var properties = Properties()

		properties.setProperty("useNext","true")
		properties.setProperty("useLast","true")
		properties.setProperty("usePosition","true")

		return properties
	}

	override fun onBind(intent: Intent?): IBinder? {
		TODO("Not yet implemented")
	}
}