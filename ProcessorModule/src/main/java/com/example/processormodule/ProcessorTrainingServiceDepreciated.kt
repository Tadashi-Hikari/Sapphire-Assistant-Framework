package com.example.processormodule

/**
 * This module handles the training of information from other installed modules
 */

import android.content.Intent
import android.os.IBinder
import android.util.Log
import com.example.componentframework.SAFService
import edu.stanford.nlp.classify.ColumnDataClassifier
import java.io.File
import java.io.ObjectOutputStream
import java.util.*

class ProcessorTrainingService: SAFService(){

    override fun onBind(intent: Intent?): IBinder? {
        TODO("Not yet implemented")
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.i("ProcessorTrainingService","Training intent received")
        Log.i("ProcessorTrainingService","Data keys are as follows: ${intent!!.getStringArrayListExtra(DATA_KEYS)}")
        // This is a terribly named function. It is converting the strings to files, supposedly
        var processorFiles = getProcessorFilesFromString(intent!!)
        var intentFiles = mutableListOf<File>()

        // Training is expecting differently formatted information, in the form of
        // Android intent bundles. Should this be the case?
        for(name in processorFiles.keys){
            if(name.endsWith(".intent")){
                Log.i("ProcessorTrainingService","Key: ${name}, Value: ${processorFiles.get(name)}")
                intentFiles.add(processorFiles.get(name)!!)
            }else{
                // I don't like this way of handling it... Maybe fix it later? More generic?
                Log.v("Temporary","This processor doesnt use this type of file")
            }
        }

        Log.i("ProcessorTrainingService","All of the relevant files were loaded. Combining")
        // This is where the combination happens
        var trainingFile = combineFiles(intentFiles)
        trainIntentClassifier(trainingFile)
        return super.onStartCommand(intent, flags, startId)
    }

    // Can I adapt this to work w/ non-text files? Or should I use the socket for that
    // This is actually getting strings
    fun getProcessorFilesFromString(intent: Intent): Map<String,File>{
        var files = mutableMapOf<String,File>()

        try{
            var fileNames = intent.getStringArrayListExtra(DATA_KEYS)!!
            Log.i("ProcessorTrainingService","Filenames are as follows: ${fileNames}")
            for(fileName in fileNames){
                var lineString = intent.getStringExtra(fileName)!!
                lineString = lineString.removeSurrounding("[","]")
                var lines = lineString.split(",")
                // This is for testing the BracketExpander. Probably not the best spot for long term
                var bracketIntent = Intent().setClassName(this,"com.example.processormodule.BracketExpander")
                var bracketSentences = arrayListOf<String>()
                for(line in lines){
                    bracketSentences.add(line)
                }
                bracketIntent.putStringArrayListExtra("BRACKET_TEST",bracketSentences)
                startService(bracketIntent)
                // This seems poorly constructed
                //files.put(fileName,convertStringsToFile(fileName, lines))
            }
        }catch(exception: Exception){
            Log.e("Temporary","Some kind of error")
        }

        return files
    }


    // Currently, this isn't run because of the keys situation
    fun combineFiles(files: List<File>): File{
        var combinedFile = File.createTempFile("trainingFile",".tmp", cacheDir)

        for(file in files){
            for(line in file.readLines()){
                Log.i("ProcessorTrainingService","Line being added: ${line.trim()}")
                // I need to be careful. I could be adding unneeded white space
                combinedFile.appendText("${line.trim()}\n")
            }
        }
        //combinedFile.
        return combinedFile
    }

    fun convertStringsToFile(fileName: String, lines: List<String>): File{
        var tempFile = File.createTempFile(fileName,".tmp", cacheDir)
        for(line in lines){
            tempFile.appendText("${line.trim()}\n")
        }
        return tempFile
    }

    // This is where saveClassifier is called
    fun trainIntentClassifier(trainingFile: File){
        var properties = createProperties()
        var classifier = ColumnDataClassifier(properties)

        classifier.trainClassifier(trainingFile.canonicalPath)
        Log.i("Parser","Intent classifier training done")
        saveClassifier(classifier)
    }

    fun createEntityProperties(): Properties{
        var properties = Properties()

        //properties.setProperty()

        return properties
    }

    // This shouldn't be hardcoded, and should be moved to a file
    fun createProperties(): Properties{
        var properties = Properties()
        properties.setProperty("goldAnswerColumn","0")
        properties.setProperty("useNB","true")
        //props.setProperty("useClass","true")
        properties.setProperty("useClassFeature","true")
        //props.setProperty("1.splitWordsRegexp","false")
        //props.setProperty("1.splitWordsTokenizerRegexp","false")
        properties.setProperty("1.splitWordsWithPTBTokenizer","true")
        // This is the line that was missing
        properties.setProperty("1.useSplitWords","true")

        return properties
    }

    fun saveClassifier(classifier: ColumnDataClassifier){
        val fileName = File(this.filesDir,"Intent.classifier")
        classifier.serializeClassifier(fileName.canonicalPath)
    }

    /**
     * This is for taking a Mycroft style .intent and preparing it for Stanford CoreNLP
     */
    fun prepareTrainingData(){
        var moduleName = "com.example.calendarmodule"
        var filename = "get.intent"
        var trainingData = listOf<String>()
        var formattedTrainingData = mutableListOf<String>()

        for(line in trainingData){
            /**
             * This does the tab-separation as required by Stanford CoreNLP.
             * Should I change how it's named? I am not expecting SAF to do the direct entry,
             * the filename might be an -action as well, or it could be a separate path... decisions
             */
            formattedTrainingData.add("${moduleName}.${filename}\t${line}")
        }
    }
}