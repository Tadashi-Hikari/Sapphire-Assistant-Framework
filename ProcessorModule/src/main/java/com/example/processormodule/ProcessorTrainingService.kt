package com.example.processormodule

import android.content.Intent
import android.net.Uri
import android.os.IBinder
import com.example.componentframework.SapphireFrameworkService
import edu.stanford.nlp.classify.ColumnDataClassifier
import java.io.File
import java.io.FileInputStream
import java.lang.Exception
import java.util.*
import kotlin.collections.ArrayList

class ProcessorTrainingServiceUpdated: SapphireFrameworkService(){
    override fun onBind(intent: Intent?): IBinder? {
        TODO("Not yet implemented")
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        try{
            Log.v(CLASS_NAME,"ProcessorTrainingService started")
            train(intent)
        }catch(exception: Exception){
            Log.d(CANONICAL_CLASS_NAME, "There was an error with the received intent. It was lacking some stuff, I suspect")
        }

        return super.onStartCommand(intent, flags, startId)
    }

    fun train(intent: Intent?){
        Log.i(CLASS_NAME,"Commencing training...")
        var trainingFiles = cacheTrainingFiles(intent)
        // Currently unused. It's for Mycroft style .intent files to meet Stanford CoreNLP standards
        var prepared = prepareTrainingData(intent!!)
        trainIntentClassifier(trainingFiles)
    }

    // Maybe I should move this to SapphireFrameworkService. It is optimized for Uris
    fun convertUriToFile(uri: Uri): String{
        Log.i(CLASS_NAME,"Converting ${uri.lastPathSegment} to cache file...")
        try {
            var parcelFileDescriptor = contentResolver.openFileDescriptor(uri,"rw")!!
            var fileDescriptor = parcelFileDescriptor.fileDescriptor
            var inputStream = FileInputStream(fileDescriptor)

            var cacheFile = File(cacheDir,uri.lastPathSegment)
            var cacheFileWriter = cacheFile.outputStream()

            var data = inputStream!!.read()
            while(data != -1){
                cacheFileWriter.write(data)
                data = inputStream.read()
            }
            cacheFileWriter.close()
            return cacheFile.name
        }catch (exception: Exception){
            Log.i(this.javaClass.name, exception.toString())
            return ""
        }
    }

    // This is required by the java library, unfortunately
    fun cacheTrainingFiles(intent: Intent?): List<String>{
        var trainingFiles = mutableListOf<String>()

        if(intent!!.data != null) {
            convertUriToFile(intent.data!!)
        }

        if(intent.clipData != null) {
            var clipData = intent.clipData!!
            for (clipIndex in 0..clipData.itemCount) {
                // This is ugly. i don't like it.
                trainingFiles.add(convertUriToFile(clipData.getItemAt(clipIndex).uri))
            }
        }
        Log.v(CANONICAL_CLASS_NAME,"Files transferred to ${CLASS_NAME}")
        return trainingFiles.toList()
    }

    // This is rough. I may want to touch this up
    fun combineFiles(files: List<String>): File{
        var combinedFile = File.createTempFile("trainingFile",".tmp", cacheDir)

        for(filename in files){
            var file = File(cacheDir,filename)
            for(line in file.readLines()){
                Log.i("ProcessorTrainingService","Line being added: ${line.trim()}")
                // I need to be careful. I could be adding unneeded white space
                combinedFile.appendText("${line.trim()}\n")
            }
        }
        //combinedFile.
        return combinedFile
    }

    // This is where saveClassifier is called
    fun trainIntentClassifier(trainingFiles: List<String>){
        var properties = createProperties()
        var classifier = ColumnDataClassifier(properties)
        var combinedFile = combineFiles(trainingFiles)

        classifier.trainClassifier(combinedFile.canonicalPath)
        Log.i("Parser","Intent classifier training done")
        saveClassifier(classifier)
    }

    fun saveClassifier(classifier: ColumnDataClassifier){
        val fileName = File(this.filesDir,"Intent.classifier")
        classifier.serializeClassifier(fileName.canonicalPath)
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

    // Gotta be careful here, cause the spaces fucked me up before
    /**
     * This is for taking a Mycroft style .intent and preparing it for Stanford CoreNLP
     */
    fun prepareTrainingData(intent: Intent){
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