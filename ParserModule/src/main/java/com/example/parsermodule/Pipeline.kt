package com.example.parsermodule

import android.app.Service
import android.content.Intent
import android.os.IBinder
import edu.stanford.nlp.ling.CoreAnnotations
import edu.stanford.nlp.ling.CoreLabel
import edu.stanford.nlp.pipeline.StanfordCoreNLP
import java.util.*

class Pipeline: Service(){

    override fun onBind(intent: Intent?): IBinder? {
        TODO("Not yet implemented")
    }

    fun checkForNewPackages(){
        if(true){
            var intent = Intent()
            startService(intent)
        }
    }

    // They can all be run together, but they all must be trained separately
    fun processText()
    {
        var text = "this is demo text"
        var properites = Properties()
        var annotators = "tokenizer,regexner,ner,classify"
        properites.setProperty("annotators",annotators)

        // Can I load the pipeline data from a file? settings?
        var pipeline = StanfordCoreNLP(properites)
        var document = pipeline.processToCoreDocument(text)
    }

    fun returnResults(coreLabel: CoreLabel){
        var resultIntent = Intent()
        resultIntent.putExtra("SKILL", coreLabel.get(CoreAnnotations.AnswerAnnotation::class.java))
        for(token in coreLabels){
            var entity = token.get((CoreAnnotations.AnswerAnnotation::class.java))
            if(entity != 'O'){
                // This isn't accounting for duplicate variables
                resultIntent.putExtra(entity,token as String)
            }
        }
        startService(resultIntent)
    }
}