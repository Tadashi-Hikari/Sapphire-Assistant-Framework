package com.example.calendarskill.HelperFiles

import android.app.Service
import android.content.Intent
import android.net.Uri
import android.os.IBinder
import androidx.core.content.FileProvider
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.io.InputStreamReader

abstract class InstallHelper: Service(){
    val ACTION_TRAIN_PARSER = "assistant.framework.parser.TRAIN"

    val ACTION_INSTALL_MODULE = "assistant.framework.module.INSTALL"

    // This seems ugly as shit
    fun attachTrainingFiles(files: Array<String>):Intent{
        var sharingIntent = Intent(ACTION_TRAIN_PARSER)
        var uriArray = arrayListOf<Uri>()
        for(filename in files) {
            var tempFile = convertStreamToFile(filename)
            var uri = Uri.parse(tempFile.absolutePath)
            uriArray.add(uri)
        }
        sharingIntent.putParcelableArrayListExtra("FILES",uriArray)
        sharingIntent.addFlags(Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION)

        return sharingIntent
    }

    fun convertStreamToFile(filename: String): File {
        var suffix = ".temp"
        // This file needs to be tab separated columns
        var asset = assets.open(filename)
        var fileReader = InputStreamReader(asset)

        var tempFile = File.createTempFile(filename, suffix)
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

    override fun onBind(intent: Intent?): IBinder? {
        TODO("Not yet implemented")
    }
}