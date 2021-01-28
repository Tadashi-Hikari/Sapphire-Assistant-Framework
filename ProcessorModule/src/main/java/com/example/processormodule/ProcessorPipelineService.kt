package com.example.processormodule

import edu.stanford.nlp.pipeline.CoreDocument
import edu.stanford.nlp.pipeline.StanfordCoreNLP
import java.util.*

/**
 * I don't need to make a custom annotator, I just need to train a custom model, and upload it to the
 * NER model in the pipeline. This makes things a lot easier
 */

class ProcessorPipelineService {

	fun main(){
		var text = "This is test text"
		var properties = Properties()
		properties.setProperty("annotators","classify,ner")
		var pipeline = StanfordCoreNLP(properties)
		var document = pipeline.processToCoreDocument(text)
	}
}