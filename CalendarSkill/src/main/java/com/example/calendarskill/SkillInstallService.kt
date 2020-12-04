package com.example.calendarskill

import android.app.Service
import android.content.Intent
import android.os.Bundle
import android.os.IBinder
import android.os.Parcel
import com.example.calendarskill.HelperFiles.InstallHelper
import java.io.File

class SkillInstallService: InstallHelper(){
    var parserFiles = arrayOf("date.entity","get.intent","NER.entity","time.entity")

    fun registerSkill(){
        var trainParser = Intent()
        trainParser.setAction(ACTION_TRAIN_PARSER)
        trainParser.setClassName(PARSER_MODULE_PACKAGE,PARSER_MODULE_INSTALL_CLASS)
        trainParser.putExtra("VERSION",PARSER_VERSION)
        dispatchParserFiles(parserFiles)

        startService(trainParser)
    }
}