package com.example.calendarskill.HelperFiles

import android.app.Service
import android.content.Intent
import android.net.Uri
import android.os.IBinder
import androidx.core.content.FileProvider
import com.example.componentframework.SAFService
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.io.InputStreamReader

abstract class InstallHelper: SAFService(){

    fun getProcessorIntent():Intent{
        var processorIntent = Intent()
        processorIntent.setAction(ACTION_SAPPHIRE_MODULE_REGISTER)
        processorIntent.setClassName(this,"com.example.sapphireassistantframework.CoreService")

        return processorIntent
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