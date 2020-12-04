package com.example.calendarskill

import android.content.Intent
import com.example.calendarskill.HelperFiles.InstallHelper

class SkillInstallService: InstallHelper(){
    var parserFiles = arrayOf("date.entity","get.intent","NER.entity","time.entity")

    fun registerSkill(){
        var trainParser = Intent()
        trainParser.setAction(ACTION_TRAIN_PARSER)
        trainParser.setClassName(PARSER_MODULE_PACKAGE,PARSER_MODULE_INSTALL_CLASS)
        trainParser.putExtra("VERSION",PARSER_VERSION)
        trainParser.fillIn(attachTrainingFiles(parserFiles),0)
        attachTrainingFiles(parserFiles)

        startService(trainParser)
    }
}