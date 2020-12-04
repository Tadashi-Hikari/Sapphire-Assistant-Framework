package com.example.calendarskill.HelperFiles

import android.app.Service
import android.content.Intent
import android.net.Uri
import android.os.IBinder
import androidx.core.content.FileProvider
import java.io.File
import java.io.InputStream

abstract class InstallHelper: Service(){
    val ACTION_TRAIN_PARSER = "assistant.framework.parser.TRAIN"

    val PARSER_MODULE_PACKAGE = "com.example.sapphireassistantframework"
    val PARSER_MODULE_INSTALL_CLASS = "com.example.parsermodule.ParserTrainService"
    val PARSER_VERSION = "0.0.1"

    fun dispatchParserFiles(files: Array<String>){
        var sharingIntent = Intent(Intent.ACTION_SEND)
        for(file in files) {
            var uri = Uri.parse(file)
            sharingIntent.setType("*/*")
            // I can name this the filename, if I want to
            sharingIntent.putExtra(Intent.EXTRA_STREAM, uri)
            sharingIntent.setClassName(PARSER_MODULE_PACKAGE, PARSER_MODULE_INSTALL_CLASS)
            startService(sharingIntent)
        }
    }

    override fun onBind(intent: Intent?): IBinder? {
        TODO("Not yet implemented")
    }
}