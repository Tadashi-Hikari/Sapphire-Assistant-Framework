package com.example.parsermodule

import android.app.Service
import android.content.Intent
import android.net.Uri
import android.os.IBinder
import android.os.Parcel
import android.os.Parcelable
import edu.stanford.nlp.ie.crf.CRFClassifier
import edu.stanford.nlp.ling.CoreLabel
import java.io.File
import java.io.FileOutputStream
import java.io.InputStreamReader
import java.io.ObjectOutputStream
import java.util.*

class ParserTrainService: Service() {

    private var files = mutableListOf<Uri>()

    // I expect Intents and Entities for skills. The rest doesn't apply to this parser
    override fun onBind(intent: Intent?): IBinder? {
        TODO("Not yet implemented")
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        if((intent.getStringExtra("VERSION") == "0.0.1") and
            (intent.action == "assistant.framework.module.INSTALL")){

            var intentFiles = intent.getParcelableExtra<Parcelable>("INTENTS")
            var entityFiles = intent.getParcelableExtra<Parcelable>("ENTITIES")
        }else if(intent.action == Intent.ACTION_SEND){
            intent.getParcelableExtra<Uri>(Intent.EXTRA_STREAM)
        }

        return super.onStartCommand(intent, flags, startId)
    }

    private lateinit var classifier: CRFClassifier<CoreLabel>

    fun trainNERClassifier(file: File){
        classifier.train()
    }

    fun trainRegExClassifier(file: File){

    }

    fun trainClassClassifier(file: File){

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
}